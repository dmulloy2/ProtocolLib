package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedStatistic;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundAwardStatsPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Object2IntMap<Stat<?>> stats} – statistic → value map to award to the player</li>
 * </ul>
 */
public class WrappedClientboundAwardStatsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.STATISTIC;

    /** The NMS field type is {@code Object2IntMap} – obtained lazily from the modifier. */
    private static final Class<?> STATS_FIELD_TYPE;
    /** NMS constructor accessor: {@code ClientboundAwardStatsPacket(Object2IntMap)}. */
    private static final EquivalentConstructor CONSTRUCTOR;

    static {
        Class<?> fieldType = new PacketContainer(TYPE).getModifier().getField(0).getType();
        STATS_FIELD_TYPE = fieldType;
        CONSTRUCTOR = new EquivalentConstructor(TYPE)
                .withParam(fieldType, createStatsConverter(fieldType));
    }

    /**
     * Creates a converter that produces an Object2IntOpenHashMap (implementing Object2IntMap)
     * from a {@code Map<WrappedStatistic, Integer>}, so it can be passed to the NMS constructor.
     */
    @SuppressWarnings("unchecked")
    private static EquivalentConverter<Map<WrappedStatistic, Integer>> createStatsConverter(Class<?> nmsMapClass) {
        // reuse the ProtocolLib statistic converter for individual entries
        EquivalentConverter<WrappedStatistic> statConverter =
                com.comphenix.protocol.wrappers.BukkitConverters.getWrappedStatisticConverter();

        return new EquivalentConverter<Map<WrappedStatistic, Integer>>() {
            @Override
            public Object getGeneric(Map<WrappedStatistic, Integer> specific) {
                try {
                    // Create an Object2IntOpenHashMap (implements Object2IntMap)
                    Class<?> openHashMapClass = Class.forName(
                            "it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap");
                    Object nmsMap = openHashMapClass.getDeclaredConstructor().newInstance();
                    java.lang.reflect.Method put = openHashMapClass.getMethod(
                            "put", Object.class, int.class);
                    for (Map.Entry<WrappedStatistic, Integer> entry : specific.entrySet()) {
                        put.invoke(nmsMap, statConverter.getGeneric(entry.getKey()), (int) entry.getValue());
                    }
                    return nmsMap;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert stats map", e);
                }
            }

            @Override
            public Map<WrappedStatistic, Integer> getSpecific(Object generic) {
                Map<Object, Object> nmsMap = (Map<Object, Object>) generic;
                Map<WrappedStatistic, Integer> result = new HashMap<>();
                for (Map.Entry<Object, Object> entry : nmsMap.entrySet()) {
                    result.put(statConverter.getSpecific(entry.getKey()),
                            ((Number) entry.getValue()).intValue());
                }
                return result;
            }

            @Override
            public Class<Map<WrappedStatistic, Integer>> getSpecificType() {
                return (Class) Map.class;
            }
        };
    }

    public WrappedClientboundAwardStatsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundAwardStatsPacket(Map<WrappedStatistic, Integer> stats) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(stats)));
    }

    public WrappedClientboundAwardStatsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Map<WrappedStatistic, Integer> getStats() {
        return handle.getStatisticMaps().read(0);
    }

    public void setStats(Map<WrappedStatistic, Integer> stats) {
        // ProtocolLib's getStatisticMaps() converter writes a plain HashMap back to the NMS field,
        // but the field type is Object2IntMap (a fastutil interface) – the cast fails.
        // Create the correct Object2IntOpenHashMap via our custom converter and write to the raw modifier.
        Object nmsMap = createStatsConverter(STATS_FIELD_TYPE).getGeneric(stats);
        handle.getModifier().write(0, nmsMap);
    }
}
