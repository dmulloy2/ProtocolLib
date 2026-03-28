package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerCombatEndPacket} (Play phase, clientbound).
 *
 * <p>Sent when the player exits combat. In Minecraft 1.21.4 (26.1) the killer
 * entity field was removed; only the duration remains.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int duration} – duration of combat in ticks</li>
 * </ul>
 */
public class WrapperGameClientboundPlayerCombatEnd extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_COMBAT_END;

    public WrapperGameClientboundPlayerCombatEnd() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundPlayerCombatEnd(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getDuration() {
        return handle.getIntegers().read(0);
    }

    public void setDuration(int duration) {
        handle.getIntegers().write(0, duration);
    }
}
