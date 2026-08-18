package com.comphenix.protocol.injector.collection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.Converters;

public class OutboundPacketListenerSet extends PacketListenerSet {

    public OutboundPacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes, ErrorReporter errorReporter) {
        super(mainThreadPacketTypes, errorReporter);
    }

    @Override
    protected ListeningWhitelist getListeningWhitelist(PacketListener packetListener) {
        return packetListener.getSendingWhitelist();
    }

    @Override
    public void invoke(PacketEvent event, @Nullable ListenerPriority priorityFilter) {
        super.invoke(event, priorityFilter);

        if (event.getPacketType() == PacketType.Play.Server.BUNDLE && !event.isCancelled()) {
            // unpack the bundle and invoke for each packet in the bundle
            List<PacketContainer> packets = Converters.toList(event.getPacket().getPacketBundles().read(0));
            // lazily allocated when the first packet is removed or replaced
            List<PacketContainer> outPackets = null;
            for (int index = 0; index < packets.size(); index++) {
                PacketContainer subPacket = packets.get(index);
                PacketContainer packet = null;

                if (subPacket != null) {
                    PacketEvent subPacketEvent = PacketEvent.fromServer(this, subPacket, event.getNetworkMarker(),
                            event.getPlayer(), event.isFiltered(), event);
                    super.invoke(subPacketEvent, priorityFilter);

                    if (!subPacketEvent.isCancelled()) {
                        PacketContainer result = subPacketEvent.getPacket();
                        if (result != null && result.getHandle() != null) {
                            packet = result;
                        }
                    }
                }

                boolean changed = packet == null || packet.getHandle() != subPacket.getHandle();
                if (outPackets == null && changed) {
                    outPackets = new ArrayList<>(packets.size());
                    outPackets.addAll(packets.subList(0, index));
                }
                if (outPackets != null && packet != null) {
                    outPackets.add(packet);
                }
            }

            if (!event.isReadOnly() && outPackets != null) {
                if (!outPackets.isEmpty()) {
                    // the original bundle may be shared between multiple recipients
                    PacketContainer bundle = event.getPacket().shallowClone();
                    bundle.getPacketBundles().write(0, outPackets);
                    event.setPacket(bundle);
                } else {
                    // cancel entire packet if each individual packet has been cancelled
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    protected void invokeListener(PacketEvent event, PacketListener listener) {
        try {
            event.setReadOnly(listener.getSendingWhitelist().getPriority() == ListenerPriority.MONITOR);
            listener.onPacketSending(event);
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (Throwable e) {
            errorReporter.reportMinimal(listener.getPlugin(), "onPacketSending(PacketEvent)", e,
                    event.getPacket().getHandle());
        }
    }
}
