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

    // ── NMS reflection handles (eagerly resolved at class load) ──────────

    private static final Class<?> WEIGHTED_LIST_CLASS;
    private static final Class<?> WEIGHTED_CLASS;

    private static final MethodAccessor WEIGHTED_LIST_OF;     // static WeightedList.of(List<Weighted>)
    private static final MethodAccessor WEIGHTED_LIST_UNWRAP; // WeightedList.unwrap() → List<Weighted>
    private static final ConstructorAccessor WEIGHTED_CTOR;   // new Weighted(Object value, int weight)
    private static final MethodAccessor WEIGHTED_VALUE;       // Weighted.value()
    private static final MethodAccessor WEIGHTED_WEIGHT;      // Weighted.weight()

    static {
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
     * Returns a mutable view of the entries in this weighted list. Mutations are
     * reflected on this wrapper.
     */
    public List<Entry<T>> getEntries() {
        return entries;
    }

    /**
     * Replaces all entries with the given list. The argument is copied; subsequent
     * modifications to it are not reflected on this wrapper.
     */
    public void setEntries(List<Entry<T>> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }

    /**
     * Appends a single entry.
     */
    public void addEntry(Entry<T> entry) {
        this.entries.add(entry);
    }

    /**
     * Appends a single value/weight entry.
     */
    public void addEntry(T value, int weight) {
        this.entries.add(new Entry<>(value, weight));
    }

    // ── Entry record ─────────────────────────────────────────────────────

    /**
     * A single weighted entry pairing a value with an integer weight.
     *
     * @param <T> the Bukkit-side element type
     */
    public record Entry<T>(T value, int weight) {
        /** Backwards-compatible accessor for callers that used {@code getValue()}. */
        public T getValue() {
            return value;
        }

        /** Backwards-compatible accessor for callers that used {@code getWeight()}. */
        public int getWeight() {
            return weight;
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

