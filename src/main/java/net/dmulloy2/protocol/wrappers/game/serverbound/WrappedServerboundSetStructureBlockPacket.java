package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetStructureBlockPacket} (game phase, serverbound).
 */
public class WrappedServerboundSetStructureBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.STRUCT;

    public WrappedServerboundSetStructureBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetStructureBlockPacket(String name, String data, boolean ignoreEntities, boolean strict, boolean showAir, boolean showBoundingBox, float integrity, long seed, BlockPosition pos, BlockPosition offset) {
        this();
        setName(name);
        setData(data);
        setIgnoreEntities(ignoreEntities);
        setStrict(strict);
        setShowAir(showAir);
        setShowBoundingBox(showBoundingBox);
        setIntegrity(integrity);
        setSeed(seed);
        setPos(pos);
        setOffset(offset);
    }

    public WrappedServerboundSetStructureBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getName() {
        return handle.getStrings().read(0);
    }

    public void setName(String name) {
        handle.getStrings().write(0, name);
    }

    public String getData() {
        return handle.getStrings().read(1);
    }

    public void setData(String data) {
        handle.getStrings().write(1, data);
    }

    public boolean isIgnoreEntities() {
        return handle.getBooleans().read(0);
    }

    public void setIgnoreEntities(boolean ignoreEntities) {
        handle.getBooleans().write(0, ignoreEntities);
    }

    public boolean isStrict() {
        return handle.getBooleans().read(1);
    }

    public void setStrict(boolean strict) {
        handle.getBooleans().write(1, strict);
    }

    public boolean isShowAir() {
        return handle.getBooleans().read(2);
    }

    public void setShowAir(boolean showAir) {
        handle.getBooleans().write(2, showAir);
    }

    public boolean isShowBoundingBox() {
        return handle.getBooleans().read(3);
    }

    public void setShowBoundingBox(boolean showBoundingBox) {
        handle.getBooleans().write(3, showBoundingBox);
    }

    public float getIntegrity() {
        return handle.getFloat().read(0);
    }

    public void setIntegrity(float integrity) {
        handle.getFloat().write(0, integrity);
    }

    public long getSeed() {
        return handle.getLongs().read(0);
    }

    public void setSeed(long seed) {
        handle.getLongs().write(0, seed);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public BlockPosition getOffset() {
        return handle.getBlockPositionModifier().read(1);
    }

    public void setOffset(BlockPosition offset) {
        handle.getBlockPositionModifier().write(1, offset);
    }
}
