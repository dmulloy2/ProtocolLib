package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.ArrayList;

/**
 * Wrapper for {@code ClientboundBundlePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Iterable<Packet<?>> subPackets} - bundled packets sent together</li>
 * </ul>
 */
public class WrappedClientboundBundlePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BUNDLE;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(Iterable.class, Converters.iterable(
                    BukkitConverters.getPacketContainerConverter(), ArrayList::new, ArrayList::new));

    public WrappedClientboundBundlePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundBundlePacket(Iterable<PacketContainer> subPackets) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(subPackets)));
    }

    public WrappedClientboundBundlePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Iterable<PacketContainer> getSubPackets() {
        return handle.getPacketBundles().read(0);
    }

    public void setSubPackets(Iterable<PacketContainer> subPackets) {
        handle.getPacketBundles().write(0, subPackets);
    }
}
