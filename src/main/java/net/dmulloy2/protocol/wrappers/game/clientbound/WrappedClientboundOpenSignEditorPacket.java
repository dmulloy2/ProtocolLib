package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundOpenSignEditorPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code BlockPosition pos} – position of the sign to edit</li>
 *   <li>{@code boolean isFrontText} – {@code true} to edit the front face, {@code false} for back</li>
 * </ul>
 */
public class WrappedClientboundOpenSignEditorPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.OPEN_SIGN_EDITOR;

    public WrappedClientboundOpenSignEditorPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundOpenSignEditorPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public boolean isFrontText() {
        return handle.getBooleans().read(0);
    }

    public void setFrontText(boolean isFrontText) {
        handle.getBooleans().write(0, isFrontText);
    }
}
