/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.compat.netty.shaded;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandler;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelInitializer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.compat.netty.ChannelInjector;
import com.comphenix.protocol.compat.netty.ProtocolInjector;
import com.comphenix.protocol.compat.netty.WrappedChannel;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.NettyNetworkMarker;
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

public class ShadedProtocolInjector implements ProtocolInjector {
	public static final ReportType REPORT_CANNOT_INJECT_INCOMING_CHANNEL = new ReportType("Unable to inject incoming channel %s.");
	
    private volatile boolean injected;
    private volatile boolean closed;
    
    // The temporary player factory
    private TemporaryPlayerFactory playerFactory = new TemporaryPlayerFactory();
    private List<VolatileField> bootstrapFields = Lists.newArrayList();
    
    // The channel injector factory
    private ShadedInjectionFactory injectionFactory;
    
    // List of network managers
    private volatile List<Object> networkManagers;
    
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
    private boolean debug;
    
    public ShadedProtocolInjector(Plugin plugin, ListenerInvoker invoker, ErrorReporter reporter) {
    	this.injectionFactory = new ShadedInjectionFactory(plugin);
		this.invoker = invoker;
		this.reporter = reporter;
	}

    @Override
    public boolean isDebug() {
		return debug;
	}
    
    /**
     * Set whether or not the debug mode is enabled.
     * @param debug - TRUE if it is, FALSE otherwise.
     */
	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
    
	/**
     * Inject into the spigot connection class.
     */
    @Override
	@SuppressWarnings("unchecked")
	public synchronized void inject() {
        if (injected)
            throw new IllegalStateException("Cannot inject twice.");
        try {
        	FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
            List<Method> serverConnectionMethods = fuzzyServer.getMethodListByParameters(MinecraftReflection.getServerConnectionClass(), new Class[] {});
            
            // Get the server connection
            Object server = fuzzyServer.getSingleton();
            Object serverConnection = null;
            
            for (Method method : serverConnectionMethods) {
            	try {
            		serverConnection = method.invoke(server);
            		
            		// Continue until we get a server connection
            		if (serverConnection != null) {
            			break;
            		}
            	} catch (Exception e) {
            		// Try the next though
            		e.printStackTrace();
            	}
            }
            
            // Handle connected channels
            final ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                	try {
	                    // This can take a while, so we need to stop the main thread from interfering
	                    synchronized (networkManagers) {
	                    	injectionFactory.fromChannel(channel, ShadedProtocolInjector.this, playerFactory).inject();
						}
                	} catch (Exception e) {
                		reporter.reportDetailed(ShadedProtocolInjector.this, Report.newBuilder(REPORT_CANNOT_INJECT_INCOMING_CHANNEL).
                				messageParam(channel).error(e));
                	}
                }
            };
            
            // This is executed before Minecraft's channel handler
            final ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
            	@Override
            	protected void initChannel(Channel channel) throws Exception {
            		// Our only job is to add init protocol
            		channel.pipeline().addLast(endInitProtocol);
            	}
            };
            
            // Add our handler to newly created channels
            final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
            	@Override
            	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Channel channel = (Channel) msg;

                    // Prepare to initialize ths channel
                    channel.pipeline().addFirst(beginInitProtocol);
                    ctx.fireChannelRead(msg);
            	}
            };
            
            // Get the current NetworkMananger list
            networkManagers = (List<Object>) FuzzyReflection.fromObject(serverConnection, true).
            	invokeMethod(null, "getNetworkManagers", List.class, serverConnection);
            
            // Insert ProtocolLib's connection interceptor
            bootstrapFields = getBootstrapFields(serverConnection);
            
            for (VolatileField field : bootstrapFields) {
            	final List<Object> list = (List<Object>) field.getValue();
     
            	// We don't have to override this list
            	if (list == networkManagers) {
            		continue;
            	}
            	
            	// Synchronize with each list before we attempt to replace them.
				field.setValue(new ShadedBootstrapList(list, connectionHandler));
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
     * @param player Player to inject into
     */
    public void injectPlayer(Player player) {
    	injectionFactory.fromPlayer(player, this).inject();
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
    @Override
	public synchronized void close() {
        if (!closed) {
            closed = true;

            for (VolatileField field : bootstrapFields) {
            	Object value = field.getValue();

            	// Undo the processed channels, if any
            	if (value instanceof ShadedBootstrapList) {
            		((ShadedBootstrapList) value).close();
            	}
            	field.revertValue();
            }
            // Uninject all the players
            injectionFactory.close();
        }
    }
    
    @Override
    public PacketEvent onPacketSending(Injector injector, Object packet, NetworkMarker marker) {
		Class<?> clazz = packet.getClass();
		
		if (sendingFilters.contains(clazz) || marker != null) {
			PacketContainer container = new PacketContainer(PacketRegistry.getPacketType(clazz), packet);
			return packetQueued(container, injector.getPlayer(), marker);
		}

		// Don't change anything
		return null;
    }

	@Override
	public PacketEvent onPacketReceiving(Injector injector, Object packet, NetworkMarker marker) {
		Class<?> clazz = packet.getClass();
		
		if (reveivedFilters.contains(clazz) || marker != null) {
			PacketContainer container = new PacketContainer(PacketRegistry.getPacketType(clazz), packet);
			return packetReceived(container, injector.getPlayer(), marker);
		}

		// Don't change anything
		return null;
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
	private PacketEvent packetQueued(PacketContainer packet, Player receiver, NetworkMarker marker) {
		PacketEvent event = PacketEvent.fromServer(this, packet, marker, receiver);
		
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
	@Override
	public PlayerInjectionHandler getPlayerInjector() {
		return new AbstractPlayerHandler(sendingFilters) {
			private ChannelListener listener = ShadedProtocolInjector.this;
			
			@Override
			public int getProtocolVersion(Player player) {
				return injectionFactory.fromPlayer(player, listener).getProtocolVersion();
			}
			
			@Override
			public void updatePlayer(Player player) {
				injectionFactory.fromPlayer(player, listener).inject();
			}

			@Override
			public void injectPlayer(Player player, ConflictStrategy strategy) {
				injectionFactory.fromPlayer(player, listener).inject();
			}
			
			@Override
			public boolean uninjectPlayer(InetSocketAddress address) {
				// Ignore this too
				return true;
			}
			
			@Override
			public void addPacketHandler(PacketType type, Set<ListenerOptions> options) {
				if (options != null && !type.forceAsync() && !options.contains(ListenerOptions.ASYNC))
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
				// Just let Netty clean this up
				return true;
			}

			@Override
			public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
				injectionFactory.fromPlayer(receiver, listener).
					sendServerPacket(packet.getHandle(), marker, filters);
			}
			
			@Override
			public boolean hasMainThreadListener(PacketType type) {
				return mainThreadFilters.contains(type);
			}
			
			@Override
			public void recieveClientPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
				injectionFactory.fromPlayer(player, listener).
					recieveClientPacket(mcPacket);
			}
			
			@Override
			public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
				// Ignore this
				return null;
			}
			
			@Override
			public void handleDisconnect(Player player) {
				injectionFactory.fromPlayer(player, listener).close();
			}

			@Override
			public WrappedChannel getChannel(Player player) {
				Injector injector = injectionFactory.fromPlayer(player, listener);
				if (injector instanceof ChannelInjector) {
					return ((ChannelInjector) injector).getChannel();
				}

				return null;
			}
		};
	}
	
	/**
	 * Retrieve a view of this protocol injector as a packet injector.
	 * @return The packet injector.
	 */
	// Client side
	@Override
	public PacketInjector getPacketInjector() {
		return new AbstractPacketInjector(reveivedFilters) {
			@Override
			public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
				NetworkMarker marker = buffered != null ? new NettyNetworkMarker(ConnectionSide.CLIENT_SIDE, buffered) : null;
				injectionFactory.fromPlayer(client, ShadedProtocolInjector.this).
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
