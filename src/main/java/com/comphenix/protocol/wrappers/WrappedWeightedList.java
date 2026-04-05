package com.comphenix.protocol.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Wraps the NMS {@code WeightedList<E>} type, representing a list of weighted
 * entries where each entry has a value and an integer weight.
 *
 * <p>The element type {@code T} is the <em>Bukkit / ProtocolLib</em> representation
 * of the NMS element type, converted via the supplied {@link EquivalentConverter}.
 *
 * @param <T> the Bukkit-side element type
 */
public class WrappedWeightedList<T> {

    // ── NMS reflection handles (lazily initialised) ──────────────────────

    private static Class<?> WEIGHTED_LIST_CLASS;
    private static Class<?> WEIGHTED_CLASS;

    private static MethodAccessor WEIGHTED_LIST_OF;   // static WeightedList.of(List<Weighted>)
    private static MethodAccessor WEIGHTED_LIST_UNWRAP; // WeightedList.unwrap() → List<Weighted>
    private static ConstructorAccessor WEIGHTED_CTOR; // new Weighted(Object value, int weight)
    private static MethodAccessor WEIGHTED_VALUE;     // Weighted.value()
    private static MethodAccessor WEIGHTED_WEIGHT;    // Weighted.weight()

    private static synchronized void ensureReflection() {
        if (WEIGHTED_LIST_CLASS != null) {
            return;
        }
        WEIGHTED_LIST_CLASS = MinecraftReflection.getMinecraftClass("util.random.WeightedList");
        WEIGHTED_CLASS = MinecraftReflection.getMinecraftClass("util.random.Weighted");

        WEIGHTED_LIST_OF = Accessors.getMethodAccessor(WEIGHTED_LIST_CLASS, "of", List.class);
        WEIGHTED_LIST_UNWRAP = Accessors.getMethodAccessor(WEIGHTED_LIST_CLASS, "unwrap");

        WEIGHTED_CTOR = Accessors.getConstructorAccessor(WEIGHTED_CLASS, Object.class, int.class);
        WEIGHTED_VALUE = Accessors.getMethodAccessor(WEIGHTED_CLASS, "value");
        WEIGHTED_WEIGHT = Accessors.getMethodAccessor(WEIGHTED_CLASS, "weight");
    }

    /**
     * Returns the NMS {@code WeightedList} class.
     */
    public static Class<?> getNmsClass() {
        ensureReflection();
        return WEIGHTED_LIST_CLASS;
    }

    // ── Instance data ────────────────────────────────────────────────────

    private final List<Entry<T>> entries;

    public WrappedWeightedList() {
        this.entries = new ArrayList<>();
    }

    public WrappedWeightedList(List<Entry<T>> entries) {
        this.entries = new ArrayList<>(entries);
    }

    /**
     * Returns a mutable view of the entries in this weighted list.
     */
    public List<Entry<T>> getEntries() {
        return entries;
    }

    // ── Entry record ─────────────────────────────────────────────────────

    /**
     * A single weighted entry pairing a value with an integer weight.
     *
     * @param <T> the Bukkit-side element type
     */
    public static class Entry<T> {
        private final T value;
        private final int weight;

        public Entry(T value, int weight) {
            this.value = value;
            this.weight = weight;
        }

        public T getValue() {
            return value;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry<?> entry)) return false;
            return weight == entry.weight && Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, weight);
        }

        @Override
        public String toString() {
            return "Entry{value=" + value + ", weight=" + weight + "}";
        }
    }

    // ── Converter factory ────────────────────────────────────────────────

    /**
     * Creates an {@link EquivalentConverter} that converts between
     * {@code WrappedWeightedList<T>} and the NMS {@code WeightedList<E>}.
     *
     * @param elementConverter converter between the Bukkit type {@code T}
     *                         and the NMS element type {@code E}
     * @param <T> Bukkit-side element type
     * @return the converter
     */
    public static <T> EquivalentConverter<WrappedWeightedList<T>> getConverter(
            EquivalentConverter<T> elementConverter) {
        return new EquivalentConverter<>() {

            @Override
            @SuppressWarnings("unchecked")
            public WrappedWeightedList<T> getSpecific(Object generic) {
                ensureReflection();

                List<Object> nmsWeightedEntries = (List<Object>) WEIGHTED_LIST_UNWRAP.invoke(generic);
                List<Entry<T>> entries = new ArrayList<>(nmsWeightedEntries.size());
                for (Object nmsWeighted : nmsWeightedEntries) {
                    Object nmsValue = WEIGHTED_VALUE.invoke(nmsWeighted);
                    int weight = (int) WEIGHTED_WEIGHT.invoke(nmsWeighted);
                    T value = elementConverter.getSpecific(nmsValue);
                    entries.add(new Entry<>(value, weight));
                }
                return new WrappedWeightedList<>(entries);
            }

            @Override
            public Object getGeneric(WrappedWeightedList<T> specific) {
                ensureReflection();

                List<Object> nmsWeightedEntries = new ArrayList<>(specific.entries.size());
                for (Entry<T> entry : specific.entries) {
                    Object nmsValue = elementConverter.getGeneric(entry.getValue());
                    Object nmsWeighted = WEIGHTED_CTOR.invoke(nmsValue, entry.getWeight());
                    nmsWeightedEntries.add(nmsWeighted);
                }
                return WEIGHTED_LIST_OF.invoke(null, nmsWeightedEntries);
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<WrappedWeightedList<T>> getSpecificType() {
                return (Class<WrappedWeightedList<T>>) (Class<?>) WrappedWeightedList.class;
            }
        };
    }

    // ── Convenience factories ────────────────────────────────────────────

    /**
     * Creates an empty weighted list.
     */
    public static <T> WrappedWeightedList<T> empty() {
        return new WrappedWeightedList<>(Collections.emptyList());
    }

    // ── Object methods ───────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WrappedWeightedList<?> that)) return false;
        return Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entries);
    }

    @Override
    public String toString() {
        return "WrappedWeightedList{entries=" + entries + "}";
    }
}

