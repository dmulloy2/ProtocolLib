package com.comphenix.protocol.injector.collection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.concurrent.PacketTypeMultiMap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.timing.TimingListenerType;
import com.comphenix.protocol.timing.TimingTrackerManager;
import com.google.common.collect.ImmutableSet;

public abstract class PacketListenerSet {

    private static final ReportType REPORT_UNSUPPORTED_PACKET = new ReportType(
            "Plugin %s tried to register listener for unknown packet %s [direction: from %s]");
    private static final ReportType REPORT_NULL_PACKET = new ReportType(
            "Plugin %s tried to set a packet or packet handle to null [type: %s, direction: %s]");

    protected final PacketTypeMultiMap<PacketListener> map = new PacketTypeMultiMap<>();

    protected final PacketTypeListenerSet mainThreadPacketTypes;
    protected final ErrorReporter errorReporter;

    /**
     * Immutable snapshot of every packet class that may resolve to a packet type with listeners, rebuilt whenever
     * listeners change or the packet registry is rebuilt. Lets the injector skip packets by class without resolving
     * the protocol state first.
     */
    private volatile PacketClassSnapshot packetClasses = PacketClassSnapshot.EMPTY;

    public PacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes, ErrorReporter errorReporter) {
        this.mainThreadPacketTypes = mainThreadPacketTypes;
        this.errorReporter = errorReporter;
    }

    protected abstract ListeningWhitelist getListeningWhitelist(PacketListener packetListener);

    public void addListener(PacketListener packetListener) {
        ListeningWhitelist listeningWhitelist = getListeningWhitelist(packetListener);

        for (Iterator<PacketType> packet = listeningWhitelist.getTypes().iterator(); packet.hasNext();) {
            PacketType packetType = packet.next();

            Set<PacketType> supportedPacketTypes = (packetType.getSender() == Sender.SERVER)
                    ? PacketRegistry.getServerPacketTypes()
                    : PacketRegistry.getClientPacketTypes();

            if (!supportedPacketTypes.contains(packetType)) {
                this.errorReporter.reportWarning(this, Report.newBuilder(REPORT_UNSUPPORTED_PACKET)
                        .messageParam(PacketAdapter.getPluginName(packetListener), packetType, packetType.getSender())
                        .build());
                
                // remove unknown packet types
                packet.remove();
            }
        }

        this.map.put(listeningWhitelist, packetListener);
        this.invalidatePacketClasses();

        Set<ListenerOptions> options = listeningWhitelist.getOptions();
        for (PacketType packetType : listeningWhitelist.getTypes()) {
            if (this.mainThreadPacketTypes != null && !packetType.isAsyncForced()) {
                boolean isOutboundSync = packetType.getSender() == Sender.SERVER
                        && !options.contains(ListenerOptions.ASYNC);
                boolean isInboundSync = packetType.getSender() == Sender.CLIENT
                        && options.contains(ListenerOptions.SYNC);
                if (isOutboundSync || isInboundSync) {
                    this.mainThreadPacketTypes.add(packetType, packetListener);
                }
            }
        }
    }

    public void removeListener(PacketListener packetListener) {
        ListeningWhitelist listeningWhitelist = getListeningWhitelist(packetListener);
        this.map.remove(listeningWhitelist, packetListener);
        this.invalidatePacketClasses();

        if (this.mainThreadPacketTypes != null) {
            for (PacketType packetType : listeningWhitelist.getTypes()) {
                this.mainThreadPacketTypes.remove(packetType, packetListener);
            }
        }
    }

    public final boolean containsPacketType(PacketType packetType) {
        return this.map.contains(packetType);
    }

    /**
     * Checks whether a packet of the given class could have listeners. This is a superset check: a {@code true}
     * result still requires resolving the packet type, but a {@code false} result guarantees that no listener is
     * registered for any packet type the class can resolve to.
     *
     * @param packetClass the class of the packet.
     * @return false if no listener can be interested in the packet class, true otherwise.
     */
    public final boolean mayContainPacketClass(Class<?> packetClass) {
        PacketClassSnapshot snapshot = this.packetClasses;
        if (snapshot.epoch != PacketRegistry.getRegisterEpoch()) {
            snapshot = this.rebuildPacketClasses();
        }
        return snapshot.classes.contains(packetClass);
    }

    // synchronized with rebuildPacketClasses so that a rebuild running concurrently with a listener change can never
    // publish a snapshot that misses the change
    private synchronized void invalidatePacketClasses() {
        this.packetClasses = PacketClassSnapshot.EMPTY;
    }

    private synchronized PacketClassSnapshot rebuildPacketClasses() {
        PacketClassSnapshot snapshot = this.packetClasses;
        int epoch = PacketRegistry.getRegisterEpoch();
        if (snapshot.epoch == epoch) {
            return snapshot;
        }

        Set<Class<?>> classes = new HashSet<>();
        for (PacketType packetType : this.map.getPacketTypes()) {
            classes.addAll(PacketRegistry.getPacketClasses(packetType));
        }

        // re-read the epoch: the lookups above may have initialized the registry
        snapshot = new PacketClassSnapshot(PacketRegistry.getRegisterEpoch(), Collections.unmodifiableSet(classes));
        this.packetClasses = snapshot;
        return snapshot;
    }

    private static final class PacketClassSnapshot {

        // epoch -1 never matches the registry, so the empty snapshot always triggers a rebuild
        static final PacketClassSnapshot EMPTY = new PacketClassSnapshot(-1, Collections.emptySet());

        final int epoch;
        final Set<Class<?>> classes;

        PacketClassSnapshot(int epoch, Set<Class<?>> classes) {
            this.epoch = epoch;
            this.classes = classes;
        }
    }

    public final ImmutableSet<PacketType> getPacketTypes() {
        return this.map.getPacketTypes();
    }

    public void invoke(PacketEvent event) {
        this.invoke(event, null);
    }

    public void invoke(PacketEvent event, @Nullable ListenerPriority priorityFilter) {
        Iterable<PacketListener> listeners = this.map.get(event.getPacketType());

        for (PacketListener listener : listeners) {
            // use the whitelist of this direction, outbound MONITOR listeners were filtered by their receiving whitelist
            ListeningWhitelist listeningWhitelist = this.getListeningWhitelist(listener);
            if (priorityFilter != null && listeningWhitelist.getPriority() != priorityFilter) {
                continue;
            }

            PacketContainer originalPacket = event.getPacket();
            if (originalPacket == null || originalPacket.getHandle() == null) {
                // ignore null packets, they are evil and shouldn't exist
                break;
            }

            // invoke packet listener, only pay for the timing wrapper while timings are recorded
            if (TimingTrackerManager.isTracking()) {
                TimingTrackerManager
                        .get(listener, event.isServerPacket() ? TimingListenerType.SYNC_OUTBOUND : TimingListenerType.SYNC_INBOUND)
                        .track(event.getPacketType(), () -> invokeListener(event, listener));
            } else {
                invokeListener(event, listener);
            }
            
            // check for new null packets
            PacketContainer newPacket = event.getPacket();
            if (newPacket == null || newPacket.getHandle() == null) {
                errorReporter.reportWarning(this, Report.newBuilder(REPORT_NULL_PACKET)
                        .messageParam(PacketAdapter.getPluginName(listener), originalPacket.getType(), originalPacket.getType().getSender())
                        .build());
                // reset packet to previous packet
                event.setPacket(originalPacket);
            }
        }
    }

    protected abstract void invokeListener(PacketEvent event, PacketListener listener);

    public void clear() {
        this.map.clear();
        this.invalidatePacketClasses();
    }
}
