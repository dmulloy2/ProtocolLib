package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
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
public class WrapperGameClientboundNbtQuery extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.NBT_QUERY;

    public WrapperGameClientboundNbtQuery() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundNbtQuery(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().read(0);
    }

    public void setTransactionId(int transactionId) {
        handle.getIntegers().write(0, transactionId);
    }

    public NbtBase<?> getTag() {
        return handle.getNbtModifier().read(0);
    }

    public void setTag(NbtBase<?> tag) {
        handle.getNbtModifier().write(0, tag);
    }
}
