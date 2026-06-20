package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedKnownPack;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.List;

/**
 * Wrapper for {@code ServerboundSelectKnownPacks} (configuration phase, serverbound).
 * Selects known packs.
 */
public class WrappedServerboundSelectKnownPacksPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.SELECT_KNOWN_PACKS;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(List.class, BukkitConverters.getListConverter(WrappedKnownPack.CONVERTER));

    public WrappedServerboundSelectKnownPacksPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSelectKnownPacksPacket(List<WrappedKnownPack> knownPacks) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(knownPacks)));
    }

    public WrappedServerboundSelectKnownPacksPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public List<WrappedKnownPack> getKnownPacks() {
        return handle.getLists(WrappedKnownPack.CONVERTER).read(0);
    }

    public void setKnownPacks(List<WrappedKnownPack> knownPacks) {
        handle.getLists(WrappedKnownPack.CONVERTER).write(0, knownPacks);
    }
}
