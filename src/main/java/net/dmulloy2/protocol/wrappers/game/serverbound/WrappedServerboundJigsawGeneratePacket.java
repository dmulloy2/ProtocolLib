package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundJigsawGeneratePacket} (game phase, serverbound).
 */
public class WrappedServerboundJigsawGeneratePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.JIGSAW_GENERATE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(int.class)
            .withParam(boolean.class);

    public WrappedServerboundJigsawGeneratePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundJigsawGeneratePacket(BlockPosition pos, int levels, boolean keepJigsaws) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(pos, levels, keepJigsaws)));
    }

    public WrappedServerboundJigsawGeneratePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public int getLevels() {
        return handle.getIntegers().read(0);
    }

    public void setLevels(int levels) {
        handle.getIntegers().write(0, levels);
    }

    public boolean isKeepJigsaws() {
        return handle.getBooleans().read(0);
    }

    public void setKeepJigsaws(boolean keepJigsaws) {
        handle.getBooleans().write(0, keepJigsaws);
    }
}
