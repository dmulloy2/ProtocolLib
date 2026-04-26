package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.List;

/**
 * Wrapper for {@code ServerboundSetGameRulePacket} (game phase, serverbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code List<Entry> entries} – list of game-rule key/value pairs</li>
 * </ul>
 *
 * <p>NMS: {@code record Entry(ResourceKey<GameRule<?>> gameRuleKey, String value)}
 */
public class WrappedServerboundSetGameRulePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_GAME_RULE;

    /** Converter between NMS {@code ResourceKey<GameRule<?>>} and {@link MinecraftKey}. */
    private static final EquivalentConverter<MinecraftKey> GAME_RULE_KEY_CONVERTER =
            WrappedRegistry.getGameRuleRegistry().resourceKeyConverter();

    private static final AutoWrapper<WrappedEntry> ENTRY_WRAPPER =
            AutoWrapper.wrap(WrappedEntry.class,
                            "network.protocol.game.ServerboundSetGameRulePacket$Entry")
                    .field(0, GAME_RULE_KEY_CONVERTER);

    public WrappedServerboundSetGameRulePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetGameRulePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the list of game-rule entries.
     * Each {@link WrappedEntry} has a {@link MinecraftKey} {@code gameRuleKey} and a {@code String} {@code value}.
     */
    public List<WrappedEntry> getEntries() {
        List<WrappedEntry> entries = handle.getLists(ENTRY_WRAPPER).read(0);
        return entries != null ? entries : List.of();
    }

    public void setEntries(List<WrappedEntry> entries) {
        handle.getLists(ENTRY_WRAPPER).write(0, entries);
    }

    /**
     * POJO mirroring {@code record ServerboundSetGameRulePacket.Entry(ResourceKey<GameRule<?>> gameRuleKey, String value)}.
     * {@link #gameRuleKey} is exposed as a {@link MinecraftKey}.
     */
    public static final class WrappedEntry {
        /** The game rule identifier (e.g. {@code minecraft:doFireTick}). */
        public MinecraftKey gameRuleKey;
        /** The new value as a string. */
        public String value;

        public WrappedEntry() {}

        public WrappedEntry(MinecraftKey gameRuleKey, String value) {
            this.gameRuleKey = gameRuleKey;
            this.value = value;
        }
    }
}
