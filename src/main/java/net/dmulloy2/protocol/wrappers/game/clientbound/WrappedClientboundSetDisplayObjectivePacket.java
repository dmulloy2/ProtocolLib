package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetDisplayObjectivePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code DisplaySlot slot} – scoreboard display slot</li>
 *   <li>{@code String objectiveName} – name of the objective to display, or empty string to clear</li>
 * </ul>
 */
public class WrappedClientboundSetDisplayObjectivePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE;

    public WrappedClientboundSetDisplayObjectivePacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundSetDisplayObjectivePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public EnumWrappers.DisplaySlot getSlot() {
        return handle.getDisplaySlots().read(0);
    }

    public void setSlot(EnumWrappers.DisplaySlot slot) {
        handle.getDisplaySlots().write(0, slot);
    }

    public String getObjectiveName() {
        return handle.getStrings().read(0);
    }

    public void setObjectiveName(String objectiveName) {
        handle.getStrings().write(0, objectiveName);
    }
}
