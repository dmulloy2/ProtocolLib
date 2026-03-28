package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerCombatEnterPacket} (Play phase, clientbound).
 *
 * <p>This is an empty packet with no fields. It signals that the player has entered combat.
 */
public class WrapperGameClientboundPlayerCombatEnter extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_COMBAT_ENTER;

    public WrapperGameClientboundPlayerCombatEnter() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundPlayerCombatEnter(PacketContainer packet) {
        super(packet, TYPE);
    }
}
