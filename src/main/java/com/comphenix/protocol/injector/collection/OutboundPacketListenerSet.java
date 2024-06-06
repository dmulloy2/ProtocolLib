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
            Iterable<PacketContainer> packets = event.getPacket().getPacketBundles().read(0);
            List<PacketContainer> outPackets = new ArrayList<>();
            for (PacketContainer subPacket : packets) {
                // ignore null packets as the will throw an error in the packet encoder
                if (subPacket == null) {
                    continue;
                }

                PacketEvent subPacketEvent = PacketEvent.fromServer(this, subPacket, event.getNetworkMarker(),
                        event.getPlayer());
                super.invoke(subPacketEvent, priorityFilter);

                
                // if the packet has been cancelled, the packet will not be add to the bundle
                if (subPacketEvent.isCancelled()) {
                    continue;
                }

                PacketContainer packet = subPacketEvent.getPacket();
                if (packet == null || packet.getHandle() == null) {
                    // super.invoke() should prevent us from getting new null packet so we just ignore it here
                    continue;
                } else {
                    outPackets.add(packet);
                }
            }

            if (packets.iterator().hasNext()) {
                event.getPacket().getPacketBundles().write(0, outPackets);
            } else {
                // cancel entire packet if each individual packet has been cancelled
                event.setCancelled(true);
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
