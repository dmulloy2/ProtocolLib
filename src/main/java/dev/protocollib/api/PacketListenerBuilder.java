package dev.protocollib.api;

import java.util.Collection;

public interface PacketListenerBuilder {

    PacketListenerBuilder types(PacketType...packetTypes);

    PacketListenerBuilder types(Collection<PacketType> packetTypes);

    PacketListenerBuilder priority(PacketListenerPriority priority);

    PacketListenerBuilder listener(PacketListener listener);

    PacketListenerRegistration register();
}
