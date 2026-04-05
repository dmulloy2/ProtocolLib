package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundBlockEntityTagQueryPacket} (game phase, serverbound).
 */
public class WrappedServerboundBlockEntityTagQueryPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TILE_NBT_QUERY;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter());

    public WrappedServerboundBlockEntityTagQueryPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundBlockEntityTagQueryPacket(int transactionId, BlockPosition pos) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(transactionId, pos)));
    }

    public WrappedServerboundBlockEntityTagQueryPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().read(0);
    }

    public void setTransactionId(int transactionId) {
        handle.getIntegers().write(0, transactionId);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }
}
