package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundChangeDifficultyPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Difficulty difficulty} – the current difficulty level</li>
 *   <li>{@code boolean locked} – whether the difficulty is locked by the server</li>
 * </ul>
 */
public class WrappedClientboundChangeDifficultyPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SERVER_DIFFICULTY;

    public WrappedClientboundChangeDifficultyPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundChangeDifficultyPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public EnumWrappers.Difficulty getDifficulty() {
        return handle.getDifficulties().read(0);
    }

    public void setDifficulty(EnumWrappers.Difficulty difficulty) {
        handle.getDifficulties().write(0, difficulty);
    }

    public boolean isLocked() {
        return handle.getBooleans().read(0);
    }

    public void setLocked(boolean locked) {
        handle.getBooleans().write(0, locked);
    }
}
