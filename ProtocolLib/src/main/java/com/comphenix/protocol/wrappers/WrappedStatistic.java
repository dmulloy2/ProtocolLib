package com.comphenix.protocol.wrappers;

import java.util.Map;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Represents a Minecraft statistics.
 * @author Kristian
 */
public class WrappedStatistic extends AbstractWrapper {
	private static final Class<?> STATISTIC = MinecraftReflection.getStatisticClass();
	private static final Class<?> STATISTIC_LIST = MinecraftReflection.getStatisticListClass();

	private static final MethodAccessor FIND_STATISTICS = Accessors.getMethodAccessor(
		FuzzyReflection.fromClass(STATISTIC_LIST).getMethodByParameters(
			"findStatistic", STATISTIC, new Class<?>[] { String.class }
		)
	);
	private static final FieldAccessor MAP_ACCESSOR = Accessors.getFieldAccessor(STATISTIC_LIST, Map.class, true);
	private static final FieldAccessor GET_NAME = Accessors.getFieldAccessor(STATISTIC, String.class, true);
	
	private final String name;
	
	private WrappedStatistic(Object handle) {
		super(STATISTIC);
		setHandle(handle);
		
		this.name = (String) GET_NAME.get(handle);
	}
	
	/**
	 * Construct a new wrapper from a given underlying statistics. 
	 * @param handle - the statistics.
	 * @return The wrapped statistics.
	 */
	public static WrappedStatistic fromHandle(Object handle) {
		return new WrappedStatistic(handle);
	}
	
	/**
	 * Construct a wrapper around an existing game profile.
	 * @param profile - the underlying profile.
	 * @return The wrapped statistics, or NULL if not found.
	 */
	public static WrappedStatistic fromName(String name) {
		Object handle = FIND_STATISTICS.invoke(null, name);
		return handle != null ? fromHandle(handle) : null;
	}
	
	/**
	 * Retrieve every known statistics.
	 * @return Every statistics.
	 */
	public static Iterable<WrappedStatistic> values() {
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) MAP_ACCESSOR.get(null); 
		
		return Iterables.transform(map.values(), new Function<Object, WrappedStatistic>() {
			public WrappedStatistic apply(Object handle) {
				return fromHandle(handle);
			};
		});
	}
	
	/**
	 * Retrieve the unique name of this statistic.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return String.valueOf(handle);
	}
	
	@Override
	public int hashCode() {
		return handle.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof WrappedGameProfile) {
			WrappedStatistic other = (WrappedStatistic) obj;
			return handle.equals(other.handle);
		}
		return false;
	}
}
