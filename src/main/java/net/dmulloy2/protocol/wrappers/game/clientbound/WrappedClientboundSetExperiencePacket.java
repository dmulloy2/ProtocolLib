package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetExperiencePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code float experienceProgress} – XP bar progress within the current level (0.0–1.0)</li>
 *   <li>{@code int totalExperience} – total accumulated XP points</li>
 *   <li>{@code int experienceLevel} – current level</li>
 * </ul>
 */
public class WrappedClientboundSetExperiencePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.EXPERIENCE;

    public WrappedClientboundSetExperiencePacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundSetExperiencePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // Experience bar progress
    // -------------------------------------------------------------------------

    /** XP bar fill within the current level, in the range [0.0, 1.0]. */
    public float getExperienceProgress() {
        return handle.getFloat().read(0);
    }

    public void setExperienceProgress(float progress) {
        handle.getFloat().write(0, progress);
    }

    // -------------------------------------------------------------------------
    // Total experience
    // -------------------------------------------------------------------------

    /** Total accumulated XP points across all levels. */
    public int getTotalExperience() {
        return handle.getIntegers().read(0);
    }

    public void setTotalExperience(int total) {
        handle.getIntegers().write(0, total);
    }

    // -------------------------------------------------------------------------
    // Experience level
    // -------------------------------------------------------------------------

    public int getExperienceLevel() {
        return handle.getIntegers().read(1);
    }

    public void setExperienceLevel(int level) {
        handle.getIntegers().write(1, level);
    }
}
