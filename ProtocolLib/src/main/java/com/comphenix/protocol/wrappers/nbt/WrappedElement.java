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

package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;
import java.lang.reflect.Method;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.common.base.Objects;

/**
 * Represents a wrapped NBT tag element, composite or not.
 *
 * @author Kristian
 * @param <TType> - type of the value field.
 */
class WrappedElement<TType> implements NbtWrapper<TType> {	
	// For retrieving the current type ID
	private static volatile Method methodGetTypeID;
	// For handling cloning
	private static volatile Method methodClone;
	
	// Which name property to use
	private static volatile Boolean hasNbtName;
	
	// Structure modifiers for the different NBT elements
	private static StructureModifier<?>[] modifiers = new StructureModifier<?>[NbtType.values().length];
	
	// The underlying NBT object
	private Object handle;
	
	// Saved type
	private NbtType type;
	
	// Saved name
	private NameProperty nameProperty;
	
	/**
	 * Initialize a NBT wrapper for a generic element.
	 * @param handle - the NBT element to wrap.
	 */
	public WrappedElement(Object handle) {
		this.handle = handle;
		initializeProperty();
	}
		
	/**
	 * Initialize a NBT wrapper for a generic element.
	 * @param handle - the NBT element to wrap.
	 */
	public WrappedElement(Object handle, String name) {
		this.handle = handle;
		initializeProperty();
		setName(name);
	}
	
	private void initializeProperty() {
		if (nameProperty == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Determine if we have a NBT string field
			if (hasNbtName == null) {
				hasNbtName = NameProperty.hasStringIndex(base, 0);
			}
			
			// Now initialize the name property
			if (hasNbtName)
				this.nameProperty = NameProperty.fromStringIndex(base, handle, 0);
			else
				this.nameProperty = NameProperty.fromBean();
		}
	}
	
	/**
	 * Retrieve a modifier (with no target) that is used to read and write the NBT value.
	 * @return The value modifier.
	 */
	protected StructureModifier<TType> getCurrentModifier() {
		NbtType type = getType();
		
		return getCurrentBaseModifier().withType(type.getValueType());
	}
	
	/**
	 * Get the object modifier (with no target) for the current underlying NBT object.
	 * @return The generic modifier.
	 */
	@SuppressWarnings("unchecked")
	protected StructureModifier<Object> getCurrentBaseModifier() {
		int index = getType().ordinal();
		StructureModifier<Object> modifier = (StructureModifier<Object>) modifiers[index];
		
		// Double checked locking
		if (modifier == null) {
			synchronized (this) {
				if (modifiers[index] == null) {
					modifiers[index] = new StructureModifier<Object>(handle.getClass(), MinecraftReflection.getNBTBaseClass(), false);
				}
				modifier = (StructureModifier<Object>) modifiers[index];
			}
		}
		
		return modifier;
	}
	
	@Override
	public boolean accept(NbtVisitor visitor) {
		return visitor.visit(this);
	}
	
	/**
	 * Retrieve the underlying NBT tag object.
	 * @return The underlying Minecraft tag object.
	 */
	@Override
	public Object getHandle() {
		return handle;
	}
	
	@Override
	public NbtType getType() {
		if (methodGetTypeID == null) {
			// Use the base class
			methodGetTypeID = FuzzyReflection.fromClass(MinecraftReflection.getNBTBaseClass()).
				getMethodByParameters("getTypeID", byte.class, new Class<?>[0]);
		}
		if (type == null) {
			try {
				type = NbtType.getTypeFromID((Byte) methodGetTypeID.invoke(handle));
			} catch (Exception e) {
				throw new FieldAccessException("Cannot get NBT type of " + handle, e);
			}
		}
		
		return type;
	}
	
	/**
	 * Retrieve the sub element type of the underlying NMS NBT list.
	 * @return The NBT sub type.
	 */
	public NbtType getSubType() {
		int subID = getCurrentBaseModifier().<Byte>withType(byte.class).withTarget(handle).read(0);
		return NbtType.getTypeFromID(subID);
	}
	
	/**
	 * Set the sub element type of the underlying NMS NBT list.
	 * @param type - the new sub element type.
	 */
	public void setSubType(NbtType type) {
		byte subID = (byte) type.getRawID();
		getCurrentBaseModifier().<Byte>withType(byte.class).withTarget(handle).write(0, subID);
	}
	
	@Override
	public String getName() {
		return nameProperty.getName();
	}
	
	@Override
	public void setName(String name) {
		nameProperty.setName(name);
	}
	
	@Override
	public TType getValue() {
		return getCurrentModifier().withTarget(handle).read(0);
	}
	
	@Override
	public void setValue(TType newValue) {
		getCurrentModifier().withTarget(handle).write(0, newValue);
	}
	
	@Override
	public void write(DataOutput destination) {
		// No need to cache this object
		NbtBinarySerializer.DEFAULT.serialize(this, destination);
	}
	
	@Override
	public NbtBase<TType> deepClone() {
		if (methodClone == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodClone = FuzzyReflection.fromClass(base).
					getMethodByParameters("clone", base, new Class<?>[0]);
		}
		
		try {
			return NbtFactory.fromNMS(methodClone.invoke(handle), getName());
		} catch (Exception e) {
			throw new FieldAccessException("Unable to clone " + handle, e);
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getType(), getValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NbtBase) {
			NbtBase<?> other = (NbtBase<?>) obj;
			
			// Make sure we're dealing with the same type
			if (other.getType().equals(getType())) {
				return Objects.equal(getValue(), other.getValue());
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String name = getName();
		
		result.append("{");
		
		if (name != null && name.length() > 0)
			result.append("name: '" + name + "', ");
		
		result.append("value: ");
		
		// Wrap quotation marks
		if (getType() == NbtType.TAG_STRING)
			result.append("'" + getValue() + "'");
		else
			result.append(getValue());
		
		result.append("}");
		return result.toString();
	}
}
