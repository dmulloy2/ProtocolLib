package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundGameRuleValuesPacket} (game phase, clientbound).
 * Sends game rule values. The map field has no ProtocolLib accessor.
 */
public class WrappedClientboundGameRuleValuesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.GAME_RULE_VALUES;

    public WrappedClientboundGameRuleValuesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundGameRuleValuesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'values' (NMS type: Map<ResourceKey<GameRule<?>>, String>)
    //   No standard ProtocolLib getMaps() accessor covers ResourceKey<GameRule<?>> keys.
    //   Use handle.getModifier().read(0) to get the raw Map, or add a getMaps() call with a custom
    //   converter for ResourceKey<GameRule<?>>.
}
