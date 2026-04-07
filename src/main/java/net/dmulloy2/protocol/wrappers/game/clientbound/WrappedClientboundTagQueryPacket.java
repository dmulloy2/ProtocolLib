package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTagQueryPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int transactionId} – transaction ID matching the original query</li>
 *   <li>{@code NbtBase<?> tag} – NBT tag data from the queried block/entity</li>
 * </ul>
 */
public class WrappedClientboundTagQueryPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.NBT_QUERY;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(MinecraftReflection.getNBTCompoundClass(), BukkitConverters.getNbtConverter());

    public WrappedClientboundTagQueryPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTagQueryPacket(int transactionId, NbtBase<?> tag) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(transactionId, tag)));
    }

    public WrappedClientboundTagQueryPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setTransactionId(int transactionId) {
        handle.getIntegers().writeSafely(0, transactionId);
    }

    public NbtBase<?> getTag() {
        return handle.getNbtModifier().readSafely(0);
    }

    public void setTag(NbtBase<?> tag) {
        handle.getNbtModifier().writeSafely(0, tag);
    }
}
