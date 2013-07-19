package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A protocol manager that delays all packet listener registrations and unregistrations until
 * an underlying protocol manager can be constructed.
 * 
 * @author Kristian
 */
public class DelayedPacketManager implements ProtocolManager, InternalManager {
	// Registering packet IDs that are not supported
	public static final ReportType REPORT_CANNOT_SEND_QUEUED_PACKET = new ReportType("Cannot send queued packet %s.");
	public static final ReportType REPORT_CANNOT_REGISTER_QUEUED_LISTENER = new ReportType("Cannot register queued listener %s.");

	/**
	 * Represents a packet that will be transmitted later.
	 * @author Kristian
	 *
	 */
	private static class QueuedPacket {
		private final Player player;
		private final PacketContainer packet;
		private final NetworkMarker marker;
		
		private final boolean filtered;
		private final ConnectionSide side;
		
		public QueuedPacket(Player player, PacketContainer packet, NetworkMarker marker, boolean filtered, ConnectionSide side) {
			this.player = player;
			this.packet = packet;
			this.marker = marker;
			this.filtered = filtered;
			this.side = side;
		}

		/**
		 * Retrieve the packet that will be transmitted or receieved.
		 * @return The packet.
		 */
		public PacketContainer getPacket() {
			return packet;
		}
		
		/**
		 * Retrieve the player that will send or recieve the packet.
		 * @return The source.
		 */
		public Player getPlayer() {
			return player;
		}
		
		/**
		 * Retrieve whether or not the packet will the sent or received.
		 * @return The connection side.
		 */
		public ConnectionSide getSide() {
			return side;
		}
		
		/**
		 * Retrieve the associated network marker used to serialize packets on the network stream.
		 * @return The associated marker.
		 */
		public NetworkMarker getMarker() {
			return marker;
		}
		
		/**
		 * Determine if the packet should be intercepted by packet listeners.
		 * @return TRUE if it should, FALSE otherwise.
		 */
		public boolean isFiltered() {
			return filtered;
		}
	}
	
	private volatile InternalManager delegate;

	// Packet listeners that will be registered
	private final Set<PacketListener> queuedListeners = Sets.newSetFromMap(Maps.<PacketListener, Boolean>newConcurrentMap());
	private final List<QueuedPacket> queuedPackets = Collections.synchronizedList(Lists.<QueuedPacket>newArrayList());
	
	private AsynchronousManager asyncManager;
	private ErrorReporter reporter;
	
	// The current hook
	private PlayerInjectHooks hook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	
	// If we have been closed
	private boolean closed;
	
	// Queued registration
	private PluginManager queuedManager;
	private Plugin queuedPlugin;
	
	private MinecraftVersion version;
	
	public DelayedPacketManager(@Nonnull ErrorReporter reporter, @Nonnull MinecraftVersion version) {
		Preconditions.checkNotNull(reporter, "reporter cannot be NULL.");
		Preconditions.checkNotNull(version, "version cannot be NULL.");
		
		this.reporter = reporter;
		this.version = version;
	}
	
	/**
	 * Retrieve the underlying protocol manager.
	 * @return The underlying manager.
	 */
	public InternalManager getDelegate() {
		return delegate;
	}

	@Override
	public MinecraftVersion getMinecraftVersion() {
		if (delegate != null)
			return delegate.getMinecraftVersion();
		else
			return version;
	}
	
	/**
	 * Update the delegate to the underlying manager.
	 * <p>
	 * This will prompt this packet manager to immediately transmit and 
	 * register all queued packets an listeners.
	 * @param delegate - delegate to the new manager.
	 */
	protected void setDelegate(InternalManager delegate) {
		this.delegate = delegate;

		if (delegate != null) {
			// Update the hook if needed
			if (!Objects.equal(delegate.getPlayerHook(), hook)) {
				delegate.setPlayerHook(hook);
			}
			// Register events as well
			if (queuedManager != null && queuedPlugin != null) {
				delegate.registerEvents(queuedManager, queuedPlugin);
			}
			
			for (PacketListener listener : queuedListeners) {
				try {
					delegate.addPacketListener(listener);
				} catch (IllegalArgumentException e) {
					// Inform about this plugin error
					reporter.reportWarning(this, 
						Report.newBuilder(REPORT_CANNOT_REGISTER_QUEUED_LISTENER).
							callerParam(delegate).messageParam(listener).error(e));
				}
			}
			
			synchronized (queuedPackets) {
				for (QueuedPacket packet : queuedPackets) {
					try {
						// Attempt to send it now
						switch (packet.getSide()) {
							case CLIENT_SIDE:
								delegate.recieveClientPacket(packet.getPlayer(), packet.getPacket(), packet.getMarker(), packet.isFiltered());
								break;
							case SERVER_SIDE:
								delegate.sendServerPacket(packet.getPlayer(), packet.getPacket(), packet.getMarker(), packet.isFiltered());
								break;
							default:
								
						}
					} catch (Exception e) {
						// Inform about this plugin error
						reporter.reportWarning(this, 
							Report.newBuilder(REPORT_CANNOT_SEND_QUEUED_PACKET).
								callerParam(delegate).messageParam(packet).error(e));
					} 
				}
			}
			
			// Don't keep this around anymore
			queuedListeners.clear();
			queuedPackets.clear();
		}
	}

	@Override
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		this.hook = playerHook;
	}
	
	@Override
	public PlayerInjectHooks getPlayerHook() {
		return hook;
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet) throws InvocationTargetException {
		sendServerPacket(reciever, packet, null, true);
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
		sendServerPacket(reciever, packet, null, filters);
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
		if (delegate != null) {
			delegate.sendServerPacket(reciever, packet, marker, filters);
		} else {
			queuedPackets.add(new QueuedPacket(reciever, packet, marker, filters, ConnectionSide.SERVER_SIDE));
		}
	}

	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException {
		recieveClientPacket(sender, packet, null, true);
	}

	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException {
		recieveClientPacket(sender, packet, null, filters);
	}
	
	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters) throws IllegalAccessException, InvocationTargetException {
		if (delegate != null) {
			delegate.recieveClientPacket(sender, packet, marker, filters);
		} else {
			queuedPackets.add(new QueuedPacket(sender, packet, marker, filters, ConnectionSide.CLIENT_SIDE));
		}
	}

	@Override
	public ImmutableSet<PacketListener> getPacketListeners() {
		if (delegate != null)
			return delegate.getPacketListeners();
		else
			return ImmutableSet.copyOf(queuedListeners);
	}

	@Override
	public void addPacketListener(PacketListener listener) {
		if (delegate != null)
			delegate.addPacketListener(listener);
		else
			queuedListeners.add(listener);
	}

	@Override
	public void removePacketListener(PacketListener listener) {
		if (delegate != null)
			delegate.removePacketListener(listener);
		else
			queuedListeners.remove(listener);
	}

	@Override
	public void removePacketListeners(Plugin plugin) {
		if (delegate != null) {
			delegate.removePacketListeners(plugin);
		} else {
			for (Iterator<PacketListener> it = queuedListeners.iterator(); it.hasNext(); ) {
				// Remove listeners of the same plugin
				if (Objects.equal(it.next().getPlugin(), plugin)) {
					it.remove();
				}
 			}
		}
	}

	@Override
	public PacketContainer createPacket(int id) {
		if (delegate != null)
			return delegate.createPacket(id);
		return createPacket(id, true);
	}
	
	@Override
	public PacketContainer createPacket(int id, boolean forceDefaults) {
		if (delegate != null) {
			return delegate.createPacket(id);
		} else {
			// Fallback implementation
			PacketContainer packet = new PacketContainer(id);
			
			// Use any default values if possible
			if (forceDefaults) {
				try {
					packet.getModifier().writeDefaults();
				} catch (FieldAccessException e) {
					throw new RuntimeException("Security exception.", e);
				}
			}
			return packet;
		}
	}
	
	@Override
	public PacketConstructor createPacketConstructor(int id, Object... arguments) {
		if (delegate != null)
			return delegate.createPacketConstructor(id, arguments);
		else
			return PacketConstructor.DEFAULT.withPacket(id, arguments);
	}

	@Override
	public Set<Integer> getSendingFilters() {
		if (delegate != null) {
			return delegate.getSendingFilters();
		} else {
			// Linear scan is fast enough here
			Set<Integer> sending = Sets.newHashSet();
			
			for (PacketListener listener : queuedListeners) {
				sending.addAll(listener.getSendingWhitelist().getWhitelist());
			}
			return sending;
		}
	}
	
	@Override
	public Set<Integer> getReceivingFilters() {
		if (delegate != null) {
			return delegate.getReceivingFilters();
		} else {
			Set<Integer> recieving = Sets.newHashSet();
			
			for (PacketListener listener : queuedListeners) {
				recieving.addAll(listener.getReceivingWhitelist().getWhitelist());
			}
			return recieving;
		}
	}
	
	@Override
	public void updateEntity(Entity entity, List<Player> observers) throws FieldAccessException {
		if (delegate != null) 
			delegate.updateEntity(entity, observers);
		else
			EntityUtilities.updateEntity(entity, observers);
	}
	
	@Override
	public Entity getEntityFromID(World container, int id) throws FieldAccessException {
		if (delegate != null)
			return delegate.getEntityFromID(container, id);
		else
			return EntityUtilities.getEntityFromID(container, id);
	}
	
	@Override
	public List<Player> getEntityTrackers(Entity entity) throws FieldAccessException {
		if (delegate != null)
			return delegate.getEntityTrackers(entity);
		else
			return EntityUtilities.getEntityTrackers(entity);
	}

	@Override
	public boolean isClosed() {
		return closed || (delegate != null && delegate.isClosed());
	}

	@Override
	public AsynchronousManager getAsynchronousManager() {
		if (delegate != null)
			return delegate.getAsynchronousManager();
		else
			return asyncManager;
	}
	
	/**
	 * Update the asynchronous manager. This must be set.
	 * @param asyncManager - the asynchronous manager.
	 */
	public void setAsynchronousManager(AsynchronousManager asyncManager) {
		this.asyncManager = asyncManager;
	}

	@Override
	public void registerEvents(PluginManager manager, Plugin plugin) {
		if (delegate != null) {
			delegate.registerEvents(manager, plugin);
		} else {
			queuedManager = manager;
			queuedPlugin = plugin;
		}
	}

	@Override
	public void close() {
		if (delegate != null)
			delegate.close();
		closed = true;
	}
}
