package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPickItemFromBlockPacket} (game phase, serverbound).
 */
public class WrappedServerboundPickItemFromBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.PICK_ITEM_FROM_BLOCK;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(boolean.class);

    public WrappedServerboundPickItemFromBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPickItemFromBlockPacket(BlockPosition pos, boolean includeData) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(pos, includeData)));
    }

    public WrappedServerboundPickItemFromBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public boolean isIncludeData() {
        return handle.getBooleans().read(0);
    }

    public void setIncludeData(boolean includeData) {
        handle.getBooleans().write(0, includeData);
    }
}
