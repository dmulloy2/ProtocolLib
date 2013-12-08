package com.comphenix.protocol.injector.netty;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandler;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelInitializer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.injector.spigot.AbstractPacketInjector;
import com.comphenix.protocol.injector.spigot.AbstractPlayerHandler;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Lists;

public class NettyProtocolInjector implements ChannelListener {   
    private volatile boolean injected;
    private volatile boolean closed;
    
    // The temporary player factory
    private TemporaryPlayerFactory playerFactory = new TemporaryPlayerFactory();
    private List<VolatileField> bootstrapFields = Lists.newArrayList();
    
	// Different sending filters
	private PacketTypeSet sendingFilters = new PacketTypeSet();
	private PacketTypeSet reveivedFilters = new PacketTypeSet();
	// Packets that must be executed on the main thread
	private PacketTypeSet mainThreadFilters = new PacketTypeSet();
	
	// Which packets are buffered
    private PacketTypeSet bufferedPackets = new PacketTypeSet();
    private ListenerInvoker invoker;
    
    // Handle errors
    private ErrorReporter reporter;
    
    public NettyProtocolInjector(ListenerInvoker invoker, ErrorReporter reporter) {
		this.invoker = invoker;
		this.reporter = reporter;
	}

	/**
     * Inject into the spigot connection class.
     */
    @SuppressWarnings("unchecked")
	public synchronized void inject() {
        if (injected)
            throw new IllegalStateException("Cannot inject twice.");
        try {
        	FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
            Method serverConnectionMethod = fuzzyServer.getMethodByParameters("getServerConnection", MinecraftReflection.getServerConnectionClass(), new Class[] {});
            
            // Get the server connection
            Object server = fuzzyServer.getSingleton();
            Object serverConnection = serverConnectionMethod.invoke(server);
            
            // Handle connected channels
            final ChannelInboundHandler initProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    // Check and see if the injector has closed
                    synchronized (this) {
                        if (closed)
                            return;
                    }
                    ChannelInjector.fromChannel(channel, NettyProtocolInjector.this, playerFactory).inject();
                }
            };
            
            // Add our handler to newly created channels
            final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
            	@Override
            	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Channel channel = (Channel) msg;

                    // Execute the other handlers before adding our own
                    ctx.fireChannelRead(msg);
                    channel.pipeline().addLast(initProtocol);
            	}
            };
            
            // Insert ProtocolLib's connection interceptor
            bootstrapFields = getBootstrapFields(serverConnection);
            
            for (VolatileField field : bootstrapFields) {
            	final List<Object> list = (List<Object>) field.getValue();

            	// Synchronize with each list before we attempt to replace them.
            	// We also reapply the synchronized list wrapper.
				field.setValue(Collections.synchronizedList(new BootstrapList(
	            	list, connectionHandler
	            )));
            }

            injected = true;
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject channel futures.", e);
        }
    }
    
	@Override
	public boolean hasListener(Class<?> packetClass) {
		return reveivedFilters.contains(packetClass) || sendingFilters.contains(packetClass);
	}
    
	@Override
	public boolean hasMainThreadListener(Class<?> packetClass) {
		return mainThreadFilters.contains(packetClass);
	}
	
	@Override
	public ErrorReporter getReporter() {
		return reporter;
	}
    
    /**
     * Inject our packet handling into a specific player.
     * @param player
     */
    public void injectPlayer(Player player) {
    	ChannelInjector.fromPlayer(player, this).inject();
    }
    
    private List<VolatileField> getBootstrapFields(Object serverConnection) {
    	List<VolatileField> result = Lists.newArrayList();
    	
    	// Find and (possibly) proxy every list
    	for (Field field : FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(List.class)) {
    		VolatileField volatileField = new VolatileField(field, serverConnection, true).toSynchronized();
    		
    		@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) volatileField.getValue();
    		
    		if (list.size() == 0 || list.get(0) instanceof ChannelFuture) {
    			result.add(volatileField);
    		}
    	}
    	return result;
    }
    
    /**
     * Clean up any remaning injections.
     */
    public synchronized void close() {
        if (!closed) {
            closed = true;

            for (VolatileField field : bootstrapFields) {
            	Object value = field.getValue();
            	
            	// Undo the processed channels, if any 
            	if (value instanceof BootstrapList) {
            		((BootstrapList) value).close();
            	}
            	field.revertValue();
            }
            
            // Uninject all the players
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            	ChannelInjector.fromPlayer(player, this).close();
            }
        }
    }
    
	@Override
	public Object onPacketSending(ChannelInjector injector, Object packet, NetworkMarker marker) {
		Class<?> clazz = packet.getClass();
		
		if (sendingFilters.contains(clazz)) {
			// Check for ignored packets
			if (injector.unignorePacket(packet)) {
				return packet;
			}
			PacketContainer container = new PacketContainer(PacketRegistry.getPacketType(clazz), packet);
			PacketEvent event = packetQueued(container, injector.getPlayer());
			
			if (!event.isCancelled()) {
				injector.saveEvent(marker, event);
				return event.getPacket().getHandle();
			} else {
				return null; // Cancel
			}
		}
		// Don't change anything
		return packet;
	}

	@Override
	public Object onPacketReceiving(ChannelInjector injector, Object packet, NetworkMarker marker) {
		Class<?> clazz = packet.getClass();
		
		if (reveivedFilters.contains(clazz)) {
			// Check for ignored packets
			if (injector.unignorePacket(packet)) {
				return packet;
			}
			PacketContainer container = new PacketContainer(PacketRegistry.getPacketType(clazz), packet);
			PacketEvent event = packetReceived(container, injector.getPlayer(), marker);
			
			if (!event.isCancelled()) {
				return event.getPacket().getHandle();
			} else {
				return null; // Cancel
			}
		}
		// Don't change anything
		return packet;
	}

	@Override
	public boolean includeBuffer(Class<?> packetClass) {
		return bufferedPackets.contains(packetClass);
	}
 
	/**
	 * Called to inform the event listeners of a queued packet.
	 * @param packet - the packet that is to be sent.
	 * @param receiver - the receiver of this packet.
	 * @return The packet event that was used.
	 */
	private PacketEvent packetQueued(PacketContainer packet, Player receiver) {
		PacketEvent event = PacketEvent.fromServer(this, packet, receiver);
		
		invoker.invokePacketSending(event);
		return event;
	}
	
	/**
	 * Called to inform the event listeners of a received packet.
	 * @param packet - the packet that has been receieved.
	 * @param sender - the client packet.
	 * @param marker - the network marker.
	 * @return The packet event that was used.
	 */
	private PacketEvent packetReceived(PacketContainer packet, Player sender, NetworkMarker marker) {
		PacketEvent event = PacketEvent.fromClient(this, packet, marker, sender);
		
		invoker.invokePacketRecieving(event);
		return event;
	}
    
	// Server side
	public PlayerInjectionHandler getPlayerInjector() {
		return new AbstractPlayerHandler(sendingFilters) {
			private ChannelListener listener = NettyProtocolInjector.this;
			
			@Override
			public void updatePlayer(Player player) {
				ChannelInjector.fromPlayer(player, listener).inject();
			
			}
			@Override
			public void injectPlayer(Player player, ConflictStrategy strategy) {
				ChannelInjector.fromPlayer(player, listener).inject();
			}
			
			@Override
			public boolean uninjectPlayer(InetSocketAddress address) {
				// Ignore this too
				return true;
			}
			
			@Override
			public void addPacketHandler(PacketType type, Set<ListenerOptions> options) {
				if (options != null && !options.contains(ListenerOptions.ASYNC))
					mainThreadFilters.addType(type);
				super.addPacketHandler(type, options);
			}
			
			@Override
			public void removePacketHandler(PacketType type) {
				mainThreadFilters.removeType(type);
				super.removePacketHandler(type);
			}
			
			@Override
			public boolean uninjectPlayer(Player player) {
				ChannelInjector.fromPlayer(player, listener).close();
				return true;
			}
			
			@Override
			public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
				ChannelInjector.fromPlayer(receiver, listener).
					sendServerPacket(packet.getHandle(), marker, filters);
			}
			
			@Override
			public void recieveClientPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
				ChannelInjector.fromPlayer(player, listener).
					recieveClientPacket(mcPacket, null, true);
			}
			
			@Override
			public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
				// Ignore this
				return null;
			}
			
			@Override
			public void handleDisconnect(Player player) {
				ChannelInjector.fromPlayer(player, listener).close();
			}
		};
	}
	
	/**
	 * Retrieve a view of this protocol injector as a packet injector.
	 * @return The packet injector.
	 */
	// Client side
	public PacketInjector getPacketInjector() {
		return new AbstractPacketInjector(reveivedFilters) {
			@Override
			public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
				NetworkMarker marker = buffered != null ? new NettyNetworkMarker(ConnectionSide.CLIENT_SIDE, buffered) : null;
				ChannelInjector.fromPlayer(client, NettyProtocolInjector.this).
					saveMarker(packet.getHandle(), marker);
				return packetReceived(packet, client, marker);
			}
			
			@Override
			public void inputBuffersChanged(Set<PacketType> set) {
				bufferedPackets = new PacketTypeSet(set);
			}
		};
	}
}
