package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Map;
import java.util.Objects;

/**
 * Wrapper for {@code ClientboundSetTimePacket} (Play phase, clientbound).
 *
 * <p>In Minecraft 1.21.4 (26.1) the packet carries {@code gameTime} (total world
 * age in ticks) and a {@code clockUpdates} map of per-clock states.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code long gameTime} – total elapsed in-game ticks since world creation</li>
 *   <li>{@code Map<Holder<WorldClock>, ClockNetworkState> clockUpdates} – per-clock state updates</li>
 * </ul>
 */
public class WrappedClientboundSetTimePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UPDATE_TIME;

    private static final EquivalentConverter<MinecraftKey> WORLD_CLOCK_HOLDER_CONVERTER =
            new EquivalentConverter<>() {
                @Override
                public Object getGeneric(MinecraftKey specific) {
                    return worldClockHolderConverter().getGeneric(specific);
                }

                @Override
                public MinecraftKey getSpecific(Object generic) {
                    return worldClockHolderConverter().getSpecific(generic);
                }

                @Override
                public Class<MinecraftKey> getSpecificType() {
                    return MinecraftKey.class;
                }
            };

    private static final AutoWrapper<ClockNetworkState> CLOCK_NETWORK_STATE =
            AutoWrapper.wrap(ClockNetworkState.class, "world.clock.ClockNetworkState");

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(long.class)
            .withParam(Map.class, BukkitConverters.getMapConverter(
                    WORLD_CLOCK_HOLDER_CONVERTER,
                    CLOCK_NETWORK_STATE));

    public WrappedClientboundSetTimePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetTimePacket(long worldAge) {
        this(worldAge, Map.of());
    }

    public WrappedClientboundSetTimePacket(long worldAge, Map<MinecraftKey, ClockNetworkState> clockUpdates) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(worldAge, clockUpdates)));
    }

    public WrappedClientboundSetTimePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getWorldAge() {
        return handle.getLongs().read(0);
    }

    public void setWorldAge(long worldAge) {
        handle.getLongs().write(0, worldAge);
    }

    public Map<MinecraftKey, ClockNetworkState> getClockUpdates() {
        return handle.getMaps(WORLD_CLOCK_HOLDER_CONVERTER, CLOCK_NETWORK_STATE).read(0);
    }

    public void setClockUpdates(Map<MinecraftKey, ClockNetworkState> clockUpdates) {
        handle.getMaps(WORLD_CLOCK_HOLDER_CONVERTER, CLOCK_NETWORK_STATE).write(0, clockUpdates);
    }

    private static EquivalentConverter<MinecraftKey> worldClockHolderConverter() {
        WrappedRegistry registry = WrappedRegistry.getRegistryByNmsKey("core.registries.Registries", "WORLD_CLOCK");
        if (registry == null) {
            throw new IllegalStateException("WorldClock registry is not available");
        }
        return Converters.holder(registry.valueConverter(), registry);
    }

    /** Mirror of {@code record ClockNetworkState(long totalTicks, float partialTick, float rate)}. */
    public static final class ClockNetworkState {
        public long totalTicks;
        public float partialTick;
        public float rate;

        public ClockNetworkState() {}

        public ClockNetworkState(long totalTicks, float partialTick, float rate) {
            this.totalTicks = totalTicks;
            this.partialTick = partialTick;
            this.rate = rate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ClockNetworkState that)) {
                return false;
            }
            return totalTicks == that.totalTicks
                    && Float.compare(partialTick, that.partialTick) == 0
                    && Float.compare(rate, that.rate) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(totalTicks, partialTick, rate);
        }
    }
}
