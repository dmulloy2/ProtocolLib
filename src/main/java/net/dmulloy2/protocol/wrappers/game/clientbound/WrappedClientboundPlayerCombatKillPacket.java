package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerCombatKillPacket} (Play phase, clientbound).
 *
 * <p>Sent when the player is killed. In Minecraft 1.21.4 (26.1) the killer
 * entity ID field was removed; only the player ID and death message remain.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int playerId} – entity ID of the killed player</li>
 *   <li>{@code Component message} – death message displayed to the player</li>
 * </ul>
 */
public class WrappedClientboundPlayerCombatKillPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_COMBAT_KILL;

    public WrappedClientboundPlayerCombatKillPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundPlayerCombatKillPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getPlayerId() {
        return handle.getIntegers().read(0);
    }

    public void setPlayerId(int playerId) {
        handle.getIntegers().write(0, playerId);
    }

    public WrappedChatComponent getMessage() {
        return handle.getChatComponents().read(0);
    }

    public void setMessage(WrappedChatComponent message) {
        handle.getChatComponents().write(0, message);
    }
}
