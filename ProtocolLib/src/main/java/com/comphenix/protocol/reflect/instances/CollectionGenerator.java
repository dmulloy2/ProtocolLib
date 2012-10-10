package com.comphenix.protocol.reflect.instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * Provides simple constructors for collection interfaces.
 * @author Kristian
 */
public class CollectionGenerator implements InstanceProvider {

	/**
	 * Shared instance of this generator.
	 */
	public static CollectionGenerator INSTANCE = new CollectionGenerator();
	
	@Override
	public Object create(@Nullable Class<?> type) {
		// Standard collection types
		if (type.isInterface()) {
			if (type.equals(Collection.class) || type.equals(List.class))
				return new ArrayList<Object>();
			else if (type.equals(Set.class))
				return new HashSet<Object>();
			else if (type.equals(Map.class))
				return new HashMap<Object, Object>();
			else if (type.equals(SortedSet.class))
				return new TreeSet<Object>();
			else if (type.equals(SortedMap.class))
				return new TreeMap<Object, Object>();
			else if (type.equals(Queue.class))
				return new LinkedList<Object>();
		}
		
		// Cannot provide an instance
		return null;
	}
}