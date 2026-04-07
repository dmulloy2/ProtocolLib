package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSignUpdatePacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code BlockPos pos} – position of the sign block</li>
 *   <li>{@code boolean isFrontText} – {@code true} if editing the front face of the sign</li>
 *   <li>{@code String[] lines} – the four lines of text on the sign</li>
 * </ul>
 */
public class WrappedServerboundSignUpdatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.UPDATE_SIGN;

    // NMS: (BlockPos pos, boolean isFrontText, String line0, String line1, String line2, String line3)
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(boolean.class)
            .withParam(String.class)
            .withParam(String.class)
            .withParam(String.class)
            .withParam(String.class);

    public WrappedServerboundSignUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    /**
     * Constructs the packet via EC. {@code lines} must contain exactly 4 elements.
     */
    public WrappedServerboundSignUpdatePacket(BlockPosition pos, boolean frontText, String line1,
                                              String line2, String line3, String line4) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(pos, frontText, line1, line2, line3, line4)));
    }

    public WrappedServerboundSignUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().readSafely(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().writeSafely(0, pos);
    }

    public boolean isFrontText() {
        return handle.getBooleans().readSafely(0);
    }

    public void setFrontText(boolean frontText) {
        handle.getBooleans().writeSafely(0, frontText);
    }

    public String[] getLines() {
        return handle.getStringArrays().readSafely(0);
    }

    public void setLines(String[] lines) {
        handle.getStringArrays().writeSafely(0, lines);
    }
}
