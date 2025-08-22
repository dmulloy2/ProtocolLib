package com.comphenix.protocol.injector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PluginVerifier.VerificationResult;
import com.comphenix.protocol.injector.collection.InboundPacketListenerSet;
import com.comphenix.protocol.injector.collection.OutboundPacketListenerSet;
import com.comphenix.protocol.injector.collection.PacketListenerSet;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.netty.manager.NetworkManagerInjector;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftVersion;

import com.google.common.collect.ImmutableSet;
import io.netty.channel.Channel;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketFilterManager implements ListenerManager, InternalManager {

    // plugin verifier reports
    private static final ReportType PLUGIN_VERIFIER_ERROR = new ReportType("Plugin verifier error: %s");
    private static final ReportType INVALID_PLUGIN_VERIFY = new ReportType("Plugin %s does not %s on ProtocolLib");

    // bukkit references
    private final Plugin plugin;
    private final Server server;

    // protocol lib references
    private final ErrorReporter reporter;
    private final MinecraftVersion minecraftVersion;
    private final AsyncFilterManager asyncFilterManager;

    private final PluginVerifier pluginVerifier;

    // packet listeners
    private final PacketTypeListenerSet mainThreadPacketTypes;
    private final InboundPacketListenerSet inboundListeners;
    private final OutboundPacketListenerSet outboundListeners;

    // only for api lookups
    private final Set<PacketListener> registeredListeners;

    // injectors
    private final NetworkManagerInjector networkManagerInjector;

    // status of this manager
    private boolean debug = false;
    private boolean closed = false;
    private boolean injected = false;

    public PacketFilterManager(PacketFilterBuilder builder) {
        // bukkit references
        this.plugin = builder.getLibrary();
        this.server = builder.getServer();

        // protocol lib references
        this.reporter = builder.getReporter();
        this.minecraftVersion = builder.getMinecraftVersion();
        this.asyncFilterManager = builder.getAsyncManager();

        // other stuff
        this.asyncFilterManager.setManager(this);
        this.pluginVerifier = initializePluginVerifier(this, builder.getLibrary(), builder.getReporter());

        // packet listeners
        this.registeredListeners = new HashSet<>();
        this.mainThreadPacketTypes = new PacketTypeListenerSet();
        this.inboundListeners = new InboundPacketListenerSet(mainThreadPacketTypes, this.reporter);
        this.outboundListeners = new OutboundPacketListenerSet(mainThreadPacketTypes, this.reporter);

        // injectors
        this.networkManagerInjector = new NetworkManagerInjector(
                builder.getLibrary(),
                this,
                builder.getReporter());

        // ensure that all packet types are loaded and synced
        PacketRegistry.getClientPacketTypes();
        PacketRegistry.getServerPacketTypes();

        // hook into all connected players
        for (Player player : this.server.getOnlinePlayers()) {
        	this.networkManagerInjector.getInjector(player).inject();
        }
    }

    public static PacketFilterBuilder newBuilder() {
        return new PacketFilterBuilder();
    }

    private static PluginVerifier initializePluginVerifier(
            PacketFilterManager requester,
            Plugin plugin,
            ErrorReporter reporter
    ) {
        try {
            // do this here to prevent us from exploding just because the verifier fails
            return new PluginVerifier(plugin);
        } catch (Exception exception) {
            reporter.reportWarning(requester, Report.newBuilder(PLUGIN_VERIFIER_ERROR)
                    .error(exception)
                    .messageParam(exception.getMessage())
                    .build());
            return null;
        }
    }

    @Override
    public void sendServerPacket(Player receiver, PacketContainer packet) {
        this.sendServerPacket(receiver, packet, true);
    }

    @Override
    public void sendServerPacket(Player receiver, PacketContainer packet, boolean filters) {
        this.sendServerPacket(receiver, packet, null, filters);
    }

    @Override
    public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) {
        if (!this.closed) {
            // if we skip the packet events later when actually writing into the pipeline we at least notify all
            // monitor listeners before doing so - they will not be able to change the event tho
            if (!filters) {
                // ensure we are on the main thread if any listener requires that
                if (this.hasMainThreadListener(packet.getType()) && !this.server.isPrimaryThread()) {
                    NetworkMarker copy = marker; // okay fine
                    ProtocolLibrary.getScheduler().scheduleSyncDelayedTask(
                            () -> this.sendServerPacket(receiver, packet, copy, false), 1L);
                    return;
                }

                // construct the event and post to all monitor listeners
                PacketEvent event = PacketEvent.fromServer(this, packet, marker, receiver, false);
                this.outboundListeners.invoke(event, ListenerPriority.MONITOR);

                // update the marker of the event without accidentally constructing it
                marker = NetworkMarker.getNetworkMarker(event);
            }

            // process outbound
            this.networkManagerInjector.getInjector(receiver).sendClientboundPacket(packet.getHandle(), marker, filters);
        }
    }

    @Override
    public void sendWirePacket(Player receiver, int id, byte[] bytes) {
        this.sendWirePacket(receiver, new WirePacket(id, bytes));
    }

    @Override
    public void sendWirePacket(Player receiver, WirePacket packet) {
        if (!this.closed) {
        	this.networkManagerInjector.getInjector(receiver).sendWirePacket(packet);
        }
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet) {
        this.receiveClientPacket(sender, packet, true);
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet, boolean filters) {
        this.receiveClientPacket(sender, packet, null, filters);
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters) {
        if (!this.closed) {
            // make sure we are on the main thread if any listener of the packet needs it
            if (this.hasMainThreadListener(packet.getType()) && !this.server.isPrimaryThread()) {
                ProtocolLibrary.getScheduler().runTask(
                        () -> this.receiveClientPacket(sender, packet, marker, filters));
                return;
            }

            Object nmsPacket = packet.getHandle();
            // check to which listeners we need to post the packet
            if (filters) {
                // post to all listeners
            	PacketEvent event = PacketEvent.fromClient(this.networkManagerInjector, packet, null, sender);
                this.invokeInboundPacketListeners(event);
                if (event.isCancelled()) {
                    return;
                }

                // prevent possible de-sync
                nmsPacket = event.getPacket().getHandle();
            } else {
                PacketEvent event = PacketEvent.fromClient(this, packet, marker, sender, false);
                this.inboundListeners.invoke(event, ListenerPriority.MONITOR);
            }

            // post to the player inject, reset our cancel state change
            this.networkManagerInjector.getInjector(sender).readServerboundPacket(nmsPacket);
        }
    }

    @Override
    public int getProtocolVersion(Player player) {
        return this.networkManagerInjector.getInjector(player).getProtocolVersion();
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet) {
        this.broadcastServerPacket(packet, this.server.getOnlinePlayers());
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) {
        if (!this.closed) {
            Collection<Player> trackers = this.getEntityTrackers(entity);
            if (includeTracker && entity instanceof Player) {
                trackers.add((Player) entity);
            }

            this.broadcastServerPacket(packet, trackers);
        }
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance) {
        if (!this.closed) {
            World world = origin.getWorld();
            if (world == null) {
                throw new IllegalArgumentException("The given location " + origin + " has no world associated!");
            }

            Location copy = origin.clone();
            int maxDistance = maxObserverDistance * maxObserverDistance;

            // filter out all target players
            Collection<Player> targetPlayers = new HashSet<>();
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(copy) <= maxDistance) {
                    targetPlayers.add(player);
                }
            }

            this.broadcastServerPacket(packet, targetPlayers);
        }
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet, Collection<? extends Player> targetPlayers) {
        for (Player player : targetPlayers) {
            this.sendServerPacket(player, packet);
        }
    }

    @Override
    public ImmutableSet<PacketListener> getPacketListeners() {
        return ImmutableSet.copyOf(this.registeredListeners);
    }

    @Override
    public void addPacketListener(PacketListener listener) {
        if (!this.closed && !this.registeredListeners.contains(listener)) {
            // get the packet types which we should actually send
            ListeningWhitelist outbound = listener.getSendingWhitelist();
            ListeningWhitelist inbound = listener.getReceivingWhitelist();

            // Remove packets from the lists if they are not supposed to be in those lists
            inbound.getTypes().removeIf(type -> !type.isClient());
            outbound.getTypes().removeIf(type -> !type.isServer());

            // verify plugin if needed
            if (this.shouldVerifyPlugin(outbound, inbound)) {
                this.printPluginWarnings(listener.getPlugin());
            }

            // register as outbound listener if anything outbound is handled
            if (outbound != null && outbound.isEnabled()) {
                // verification
                this.verifyWhitelist(listener, outbound);

                // registration
                this.registeredListeners.add(listener);
                this.outboundListeners.addListener(listener);
            }

            // register as inbound listener if anything outbound is handled
            if (inbound != null && inbound.isEnabled()) {
                // verification
                this.verifyWhitelist(listener, inbound);

                // registration
                this.registeredListeners.add(listener);
                this.inboundListeners.addListener(listener);
            }
        }
    }

    @Override
    public void removePacketListener(PacketListener listener) {
        if (this.closed || !this.registeredListeners.remove(listener)) {
            return;
        }

        ListeningWhitelist outbound = listener.getSendingWhitelist();
        ListeningWhitelist inbound = listener.getReceivingWhitelist();

        // remove outbound listeners (if any)
        if (outbound != null && outbound.isEnabled()) {
            this.outboundListeners.removeListener(listener);
        }

        // remove inbound listeners (if any)
        if (inbound != null && inbound.isEnabled()) {
            this.inboundListeners.removeListener(listener);
        }
    }

    @Override
    public void removePacketListeners(Plugin plugin) {
        if (this.closed || this.plugin.equals(plugin)) {
            return;
        }

        for (PacketListener listener : this.getPacketListeners()) {
            if (Objects.equals(listener.getPlugin(), plugin)) {
                this.removePacketListener(listener);
            }
        }
    }

    @Override
    public PacketContainer createPacket(PacketType type) {
        return this.createPacket(type, true);
    }

    @Override
    public PacketContainer createPacket(PacketType type, boolean forceDefaults) {
        PacketContainer container = new PacketContainer(type);
        if (forceDefaults) {
            container.getModifier().writeDefaults();
        }

        return container;
    }

    @Override
    public PacketConstructor createPacketConstructor(PacketType type, Object... arguments) {
        return PacketConstructor.DEFAULT.withPacket(type, arguments);
    }

    @Override
    public void updateEntity(Entity entity, List<Player> observers) {
        EntityUtilities.getInstance().updateEntity(entity, observers);
    }

    @Override
    public Entity getEntityFromID(World container, int id) {
        return EntityUtilities.getInstance().getEntity(container, id);
    }

    @Override
    public List<Player> getEntityTrackers(Entity entity) {
        return EntityUtilities.getInstance().getEntityTrackers(entity);
    }

    @Override
    public Set<PacketType> getSendingFilterTypes() {
        return Collections.unmodifiableSet(this.outboundListeners.getPacketTypes());
    }

    @Override
    public Set<PacketType> getReceivingFilterTypes() {
        return Collections.unmodifiableSet(this.inboundListeners.getPacketTypes());
    }

    @Override
    public MinecraftVersion getMinecraftVersion() {
        return this.minecraftVersion;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public AsynchronousManager getAsynchronousManager() {
        return this.asyncFilterManager;
    }

    @Override
    public void verifyWhitelist(PacketListener listener, ListeningWhitelist whitelist) {
        for (PacketType type : whitelist.getTypes()) {
            if (type == null) {
                throw new IllegalArgumentException("Null packet type in listener of " + PacketAdapter.getPluginName(listener));
            }
        }
    }

    public void removePlayer(Player player) {
        asyncFilterManager.removePlayer(player);
    }

    public void injectPlayer(Player player) {
        networkManagerInjector.getInjector(player).inject();
    }

    public void injectChannel(Channel channel) {
        networkManagerInjector.getInjector(channel).inject();
    }

    public void inject() {
        if (this.closed || this.injected) {
            return;
        }

        // prevent duplicate event registrations / injections
        this.injected = true;
        this.networkManagerInjector.inject();
    }

    @Override
    public void close() {
        if (!this.closed) {
            // mark as closed directly to prevent duplicate calls
            this.closed = true;
            this.injected = false;

            // uninject all clutter
            this.networkManagerInjector.close();

            // clear listener collections
            this.mainThreadPacketTypes.clear();
            this.inboundListeners.clear();
            this.outboundListeners.clear();

            // cleanup
            this.registeredListeners.clear();
            this.asyncFilterManager.cleanupAll();
        }
    }

    @Override
    public boolean isDebug() {
        return this.debug;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean hasInboundListener(PacketType packetType) {
        return this.inboundListeners.containsPacketType(packetType);
    }

    @Override
    public boolean hasOutboundListener(PacketType packetType) {
        return this.outboundListeners.containsPacketType(packetType);
    }

    @Override
    public boolean hasMainThreadListener(PacketType packetType) {
        return this.mainThreadPacketTypes.contains(packetType);
    }

    @Override
    public void invokeInboundPacketListeners(PacketEvent event) {
        if (!this.closed) {
            this.postPacketToListeners(this.inboundListeners, event, false);
        }
    }

    @Override
    public void invokeOutboundPacketListeners(PacketEvent event) {
        if (!this.closed) {
            this.postPacketToListeners(this.outboundListeners, event, true);
        }
    }

    private void postPacketToListeners(PacketListenerSet listeners, PacketEvent event, boolean outbound) {
        try {
            // append async marker if any async listener for the packet was registered
            if (this.asyncFilterManager.hasAsynchronousListeners(event)) {
                event.setAsyncMarker(this.asyncFilterManager.createAsyncMarker());
            }

            // post to sync listeners
            listeners.invoke(event);

            // check if we need to post the packet to the async handler
            if (!event.isCancelled() && event.getAsyncMarker() != null && !event.getAsyncMarker().isAsyncCancelled()) {
                this.asyncFilterManager.enqueueSyncPacket(event, event.getAsyncMarker());

                // cancel the packet here for async processing (enqueueSyncPacket will create a copy of the event)
                event.setReadOnly(false);
                event.setCancelled(true);
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to process " + (outbound ? "outbound" : "inbound") + " packet event: " + event, t);
        }
    }

    private boolean shouldVerifyPlugin(ListeningWhitelist out, ListeningWhitelist in) {
        if (out != null && out.isEnabled() && !out.getOptions().contains(ListenerOptions.SKIP_PLUGIN_VERIFIER)) {
            return true;
        }

        return in != null && in.isEnabled() && !in.getOptions().contains(ListenerOptions.SKIP_PLUGIN_VERIFIER);
    }

    private void printPluginWarnings(Plugin plugin) {
        // check if we were able to initialize the plugin verifier
        if (this.pluginVerifier != null) {
            VerificationResult result = this.pluginVerifier.verify(plugin);
            if (result != VerificationResult.VALID) {
                this.reporter.reportWarning(this, Report.newBuilder(INVALID_PLUGIN_VERIFY)
                        .messageParam(plugin.getName(), result)
                        .build());
            }
        }
    }
}
