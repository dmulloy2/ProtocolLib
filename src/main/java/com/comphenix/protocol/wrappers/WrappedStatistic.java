package com.comphenix.protocol.wrappers;

import java.util.Map;
import java.util.stream.Collectors;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a Minecraft statistics.
 * @author Kristian
 */
public class WrappedStatistic extends AbstractWrapper {
	private static final Class<?> STATISTIC = MinecraftReflection.getStatisticClass();
	private static final Class<?> STATISTIC_LIST = MinecraftReflection.getStatisticListClass();


	static {
		try {
			FIND_STATISTICS = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(STATISTIC_LIST).getMethodByReturnTypeAndParameters(
							"findStatistic", STATISTIC, new Class<?>[]{String.class}));
			MAP_ACCESSOR = Accessors.getFieldAccessor(STATISTIC_LIST, Map.class, true);
			GET_NAME = Accessors.getFieldAccessor(STATISTIC, String.class, true);
		} catch (Exception ex) {
			// TODO - find an alternative
		}
	}

	private static MethodAccessor FIND_STATISTICS;
	private static FieldAccessor MAP_ACCESSOR;
	private static FieldAccessor GET_NAME;
	
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
	 * @param name - statistic name.
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
		
		return map.values().stream().map(WrappedStatistic::fromHandle).collect(Collectors.toList());
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
}
