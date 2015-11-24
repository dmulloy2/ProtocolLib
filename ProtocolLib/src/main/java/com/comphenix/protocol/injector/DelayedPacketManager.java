package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A protocol manager that delays all packet listener registrations and unregistrations until
 * an underlying protocol manager can be constructed.
 * 
 * @author Kristian
 */
public class DelayedPacketManager implements InternalManager {
	// Registering packet IDs that are not supported
	public static final ReportType REPORT_CANNOT_SEND_QUEUED_PACKET = new ReportType("Cannot send queued packet %s.");
	public static final ReportType REPORT_CANNOT_SEND_QUEUED_WIRE_PACKET = new ReportType("Cannot send queued wire packet %s.");
	public static final ReportType REPORT_CANNOT_REGISTER_QUEUED_LISTENER = new ReportType("Cannot register queued listener %s.");
	
	private volatile InternalManager delegate;

	// Queued actions
	private final List<Runnable> queuedActions = Collections.synchronizedList(Lists.<Runnable>newArrayList());
	private final List<PacketListener> queuedListeners = Collections.synchronizedList(Lists.<PacketListener>newArrayList());
	
	private AsynchronousManager asyncManager;
	private ErrorReporter reporter;
	
	// The current hook
	private PlayerInjectHooks hook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	
	// If we have been closed
	private boolean closed;
	private boolean debug;
	
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
	public int getProtocolVersion(Player player) {
		if (delegate != null)
			return delegate.getProtocolVersion(player);
		else
			return Integer.MIN_VALUE;
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
			// And update the debug mode
			delegate.setDebug(debug);
			
			// Add any pending listeners
			synchronized (queuedListeners) {
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
			}
			
			// Execute any delayed actions
			synchronized (queuedActions) {
				for (Runnable action : queuedActions) {
					action.run();
				}
			}
			
			// Don't keep this around anymore
			queuedListeners.clear();
			queuedActions.clear();
		}
	}

	private Runnable queuedAddPacket(final ConnectionSide side, final Player player, final PacketContainer packet,
									 final NetworkMarker marker, final boolean filtered) {
		
		return new Runnable() {
			@Override
			public void run() {
				try {
					// Attempt to send it now
					switch (side) {
						case CLIENT_SIDE:
							delegate.recieveClientPacket(player, packet, marker, filtered);
							break;
						case SERVER_SIDE:
							delegate.sendServerPacket(player, packet, marker, filtered);
							break;
						default:
							throw new IllegalArgumentException("side cannot be " + side);
					}
				} catch (Exception e) {
					// Inform about this plugin error
					reporter.reportWarning(this,
						Report.newBuilder(REPORT_CANNOT_SEND_QUEUED_PACKET).
							callerParam(delegate).messageParam(packet).error(e));
				}
			}
		};
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
	public void sendServerPacket(Player receiver, PacketContainer packet) throws InvocationTargetException {
		sendServerPacket(receiver, packet, null, true);
	}
	
	@Override
	public void sendServerPacket(Player receiver, PacketContainer packet, boolean filters) throws InvocationTargetException {
		sendServerPacket(receiver, packet, null, filters);
	}
	
	@Override
	public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
		if (delegate != null) {
			delegate.sendServerPacket(receiver, packet, marker, filters);
		} else {
			queuedActions.add(queuedAddPacket(ConnectionSide.SERVER_SIDE, receiver, packet, marker, filters));
		}
	}

	@Override
	public void sendWirePacket(Player receiver, int id, byte[] bytes) throws InvocationTargetException {
		WirePacket packet = new WirePacket(id, bytes);
		sendWirePacket(receiver, packet);
	}

	@Override
	public void sendWirePacket(final Player receiver, final WirePacket packet) throws InvocationTargetException {
		if (delegate != null) {
			delegate.sendWirePacket(receiver, packet);
		} else {
			queuedActions.add(new Runnable() {

				@Override
				public void run() {
					try {
						delegate.sendWirePacket(receiver, packet);
					} catch (Throwable ex) {
						// Inform about this plugin error
						reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_SEND_QUEUED_WIRE_PACKET)
								.callerParam(delegate)
								.messageParam(packet)
								.error(ex));
					}
				}

			});
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
			queuedActions.add(queuedAddPacket(ConnectionSide.CLIENT_SIDE, sender, packet, marker, filters));
		}
	}
	
	@Override
	public void broadcastServerPacket(final PacketContainer packet, final Entity entity, final boolean includeTracker) {
		if (delegate != null) {
			delegate.broadcastServerPacket(packet, entity, includeTracker);
		} else {
			queuedActions.add(new Runnable() {
				@Override
				public void run() {
					delegate.broadcastServerPacket(packet, entity, includeTracker);
				}
			});
		}
	}

	@Override
	public void broadcastServerPacket(final PacketContainer packet, final Location origin, final int maxObserverDistance) {
		if (delegate != null) {
			delegate.broadcastServerPacket(packet, origin, maxObserverDistance);
		} else {
			queuedActions.add(new Runnable() {
				@Override
				public void run() {
					delegate.broadcastServerPacket(packet, origin, maxObserverDistance);
				}
			});
		}
	}
	
	@Override
	public void broadcastServerPacket(final PacketContainer packet) {
		if (delegate != null) {
			delegate.broadcastServerPacket(packet);
		} else {
			queuedActions.add(new Runnable() {
				@Override
				public void run() {
					delegate.broadcastServerPacket(packet);
				}
			});
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
	@Deprecated
	public PacketContainer createPacket(int id) {
		if (delegate != null)
			return delegate.createPacket(id);
		return createPacket(id, true);
	}
	
	@Override
	@Deprecated
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
	
	@SuppressWarnings("deprecation")
	@Override
	public PacketConstructor createPacketConstructor(int id, Object... arguments) {
		if (delegate != null)
			return delegate.createPacketConstructor(id, arguments);
		else
			return PacketConstructor.DEFAULT.withPacket(id, arguments);
	}
	
	@Override
	public PacketConstructor createPacketConstructor(PacketType type, Object... arguments) {
		if (delegate != null)
			return delegate.createPacketConstructor(type, arguments);
		else
			return PacketConstructor.DEFAULT.withPacket(type, arguments);
	}

	@Override
	@Deprecated
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
	@Deprecated
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
	public PacketContainer createPacket(PacketType type) {
		return createPacket(type.getLegacyId());
	}

	@Override
	public PacketContainer createPacket(PacketType type, boolean forceDefaults) {
		return createPacket(type.getLegacyId(), forceDefaults);
	}

	@Override
	public Set<PacketType> getSendingFilterTypes() {
		return PacketRegistry.toPacketTypes(getSendingFilters(), Sender.SERVER);
	}

	@Override
	public Set<PacketType> getReceivingFilterTypes() {
		return PacketRegistry.toPacketTypes(getReceivingFilters(), Sender.CLIENT);
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
	
	@Override
	public boolean isDebug() {
		return debug;
	}

	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;
		
		if (delegate != null) {
			delegate.setDebug(debug);
		}
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
