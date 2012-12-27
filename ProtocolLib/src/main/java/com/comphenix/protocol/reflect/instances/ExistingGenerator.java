/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Nullable;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.collect.Lists;

/**
 * Provides instance constructors using a list of existing values.
 * <p>
 * Only one instance per individual class.
 * @author Kristian
 */
public class ExistingGenerator implements InstanceProvider {
	/**
	 * Represents a single node in the tree of possible values.
	 * 
	 * @author Kristian
	 */
	private static final class Node {
		private Map<Class<?>, Node> children;
		private Class<?> key;
		private Object value;
		private int level;
		
		public Node(Class<?> key, Object value, int level) {
			this.children = new HashMap<Class<?>, Node>();
			this.key = key;
			this.value = value;
			this.level = level;
		}

		public Node addChild(Node node) {
			children.put(node.key, node);
			return node;
		}
		
		public int getLevel() {
			return level;
		}

		public Collection<Node> getChildren() {
			return children.values();
		}
		
		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
		
		public Node getChild(Class<?> clazz) {
			return children.get(clazz);
		}
	}
	
	// Represents the root node
	private Node root = new Node(null, null, 0);
	
	private ExistingGenerator() {
		// Only accessible to the constructors
	}
	
	/**
	 * Automatically create an instance provider from a objects public and private fields.
	 * <p>
	 * If two or more fields share the same type, the last declared non-null field will take
	 * precedent.
	 * @param object - object to create an instance generator from.
	 * @return The instance generator.
	 */
	public static ExistingGenerator fromObjectFields(Object object) {
		if (object == null)
			throw new IllegalArgumentException("Object cannot be NULL.");
		
		return fromObjectFields(object, object.getClass());
	}
	
	/**
	 * Automatically create an instance provider from a objects public and private fields.
	 * <p>
	 * If two or more fields share the same type, the last declared non-null field will take
	 * precedent.
	 * @param object - object to create an instance generator from.
	 * @param type - the type to cast the object.
	 * @return The instance generator.
	 */
	public static ExistingGenerator fromObjectFields(Object object, Class<?> type) {
		ExistingGenerator generator = new ExistingGenerator();
		
		// Possible errors
		if (object == null)
			throw new IllegalArgumentException("Object cannot be NULL.");
		if (type == null)
			throw new IllegalArgumentException("Type cannot be NULL.");
		if (!type.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("Type must be a superclass or be the same type.");
		
		// Read instances from every field.
		for (Field field : FuzzyReflection.fromClass(type, true).getFields()) {
			try {
				Object value = FieldUtils.readField(field, object, true);

				// Use the type of the field, not the object itself
				if (value != null)
					generator.addObject(field.getType(), value);
				
			} catch (Exception e) {
				// Yes, swallow it. No, really.
			}
		}

		return generator;
	}
	
	/**
	 * Create an instance generator from a pre-defined array of values.
	 * @param values - values to provide.
	 * @return An instance provider that uses these values.
	 */
	public static ExistingGenerator fromObjectArray(Object[] values) {
		ExistingGenerator generator = new ExistingGenerator();
		
		for (Object value : values)
			 generator.addObject(value);
		
		return generator;
	}
	
	private void addObject(Object value) {
		if (value == null)
			throw new IllegalArgumentException("Value cannot be NULL.");
		
		addObject(value.getClass(), value);
	}
	
	private void addObject(Class<?> type, Object value) {
		Node node = getLeafNode(root, type, false);
		
		// Set the value
		node.setValue(value);
	}
	
	private Node getLeafNode(final Node start, Class<?> type, boolean readOnly) {
		Class<?>[] path = getHierachy(type);
		Node current = start;
		
		for (int i = 0; i < path.length; i++) {
			Node next = getNext(current, path[i], readOnly);
			
			// Try every interface too
			if (next == null && readOnly) {
				current = null;
				break;
			}

			current = next;
		}
		
		// And we're done
		return current;
	}

	private Node getNext(Node current, Class<?> clazz, boolean readOnly) {
		Node next = current.getChild(clazz);
		
		// Add a new node if needed
		if (next == null && !readOnly) {
			next = current.addChild(new Node(clazz, null, current.getLevel() + 1));
		}
		
		// Add interfaces
		if (next != null && !readOnly && !clazz.isInterface()) {
			for (Class<?> clazzInterface : clazz.getInterfaces()) {
				getLeafNode(root, clazzInterface, readOnly).addChild(next);
			}
		}
		return next;
	}
	
	private Node getLowestLeaf(Node current) {
		Node candidate = current;
		
		// Depth-first search
		for (Node child : current.getChildren()) {
			Node subtree = getLowestLeaf(child);
			
			// Get the lowest node
			if (subtree.getValue() != null && candidate.getLevel() < subtree.getLevel()) {
				candidate = subtree;
			}
		}
		
		return candidate;
	}
	
	private Class<?>[] getHierachy(Class<?> type) {
		LinkedList<Class<?>> levels = Lists.newLinkedList();
		
		// Add each class from the hierachy
		for (; type != null; type = type.getSuperclass()) {
			levels.addFirst(type);
		}
		
		return levels.toArray(new Class<?>[0]);
	}

	@Override
	public Object create(@Nullable Class<?> type) {
		// Locate the type in the hierachy
		Node node = getLeafNode(root, type, true);
		
		// Next, get the lowest leaf node
		if (node != null) {
			node = getLowestLeaf(node);
		}
			
		// NULL values indicate that the generator failed
		if (node != null)
			return node.getValue();
		else
			return null;
	}
}
