package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Map;

/**
 * Wrapper for {@code ClientboundGameRuleValuesPacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code Map<ResourceKey<GameRule<?>>, String> values} – current game-rule values by key</li>
 * </ul>
 */
public class WrappedClientboundGameRuleValuesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.GAME_RULE_VALUES;

    /** Converter between NMS {@code ResourceKey<GameRule<?>>} and {@link MinecraftKey}. */
    private static final EquivalentConverter<MinecraftKey> GAME_RULE_KEY_CONVERTER =
            WrappedRegistry.getGameRuleRegistry().resourceKeyConverter();

    public WrappedClientboundGameRuleValuesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundGameRuleValuesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the game-rule values as a {@code Map<MinecraftKey, String>}.
     * Each key is the game rule's identifier (e.g. {@code minecraft:doFireTick}).
     */
    public Map<MinecraftKey, String> getValues() {
        Map<MinecraftKey, String> values = handle.getMaps(GAME_RULE_KEY_CONVERTER, Converters.passthrough(String.class)).read(0);
        return values != null ? values : Map.of();
    }

    /**
     * Sets the game-rule values from a {@code Map<MinecraftKey, String>}.
     * Each key is converted to a {@code ResourceKey<GameRule<?>>} in the {@code game_rule} registry.
     */
    public void setValues(Map<MinecraftKey, String> values) {
        handle.getMaps(GAME_RULE_KEY_CONVERTER, Converters.passthrough(String.class)).write(0, values);
    }
}
