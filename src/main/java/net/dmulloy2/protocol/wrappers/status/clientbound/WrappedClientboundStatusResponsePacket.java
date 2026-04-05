package net.dmulloy2.protocol.wrappers.status.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundStatusResponsePacket} (status phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code ServerStatus status} – the server ping/status payload</li>
 * </ul>
 */
public class WrappedClientboundStatusResponsePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Status.Server.SERVER_INFO;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getServerPingClass(), BukkitConverters.getWrappedServerPingConverter());

    public WrappedClientboundStatusResponsePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundStatusResponsePacket(WrappedServerPing status) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(status)));
    }

    public WrappedClientboundStatusResponsePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedServerPing getStatus() {
        return handle.getServerPings().read(0);
    }

    public void setStatus(WrappedServerPing status) {
        handle.getServerPings().write(0, status);
    }
}
