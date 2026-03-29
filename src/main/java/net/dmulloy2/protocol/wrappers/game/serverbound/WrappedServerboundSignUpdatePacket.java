package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
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

    public WrappedServerboundSignUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundSignUpdatePacket(BlockPosition pos, boolean isFrontText, String[] lines) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] {
                MinecraftReflection.getBlockPositionClass(), boolean.class,
                String.class, String.class, String.class, String.class
        }).createPacket(BlockPosition.getConverter().getGeneric(pos), isFrontText,
                lines[0], lines[1], lines[2], lines[3]));
    }

    public WrappedServerboundSignUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the position of the sign block.
     */
    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    /**
     * Sets the position of the sign block.
     */
    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    /**
     * Returns {@code true} if editing the front face of the sign.
     */
    public boolean isFrontText() {
        return handle.getBooleans().read(0);
    }

    /**
     * Sets whether the front face of the sign is being edited.
     */
    public void setFrontText(boolean frontText) {
        handle.getBooleans().write(0, frontText);
    }

    /**
     * Returns the four lines of text on the sign.
     */
    public String[] getLines() {
        return handle.getStringArrays().read(0);
    }

    /**
     * Sets the four lines of text on the sign.
     */
    public void setLines(String[] lines) {
        handle.getStringArrays().write(0, lines);
    }
}
