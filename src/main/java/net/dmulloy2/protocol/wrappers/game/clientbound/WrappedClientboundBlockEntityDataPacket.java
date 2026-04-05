package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedRegistrable;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundBlockEntityDataPacket} (game phase, clientbound).
 */
public class WrappedClientboundBlockEntityDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TILE_ENTITY_DATA;

    public WrappedClientboundBlockEntityDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundBlockEntityDataPacket(BlockPosition pos, WrappedRegistrable blockEntityType, NbtBase tag) {
        this();
        setPos(pos);
        setBlockEntityType(blockEntityType);
        setTag(tag);
    }


    public WrappedClientboundBlockEntityDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public WrappedRegistrable getBlockEntityType() {
        return handle.getBlockEntityTypeModifier().read(0);
    }

    public void setBlockEntityType(WrappedRegistrable type) {
        handle.getBlockEntityTypeModifier().write(0, type);
    }

    public NbtBase getTag() {
        return handle.getNbtModifier().read(0);
    }

    public void setTag(NbtBase tag) {
        handle.getNbtModifier().write(0, tag);
    }
}
