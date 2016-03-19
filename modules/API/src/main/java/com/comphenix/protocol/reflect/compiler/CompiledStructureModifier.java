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

package com.comphenix.protocol.reflect.compiler;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.google.common.collect.Sets;

/**
 * Represents a compiled structure modifier.
 * 
 * @author Kristian
 */
public abstract class CompiledStructureModifier extends StructureModifier<Object> {
	// Used to compile instances of structure modifiers
	protected StructureCompiler compiler;
	
	// Fields that originally were read only
	private Set<Integer> exempted;
	
	public CompiledStructureModifier() {
		super();
		customConvertHandling = true;
	}
	
	@Override
	public void setReadOnly(int fieldIndex, boolean value) throws FieldAccessException {
		// We can remove the read-only status
		if (isReadOnly(fieldIndex) && !value) {
			if (exempted == null)
				exempted = Sets.newHashSet();
			exempted.add(fieldIndex);
		}
		
		// We can only make a certain kind of field read only
		if (!isReadOnly(fieldIndex) && value) {
			if (exempted == null || !exempted.contains(fieldIndex)) {
				throw new IllegalStateException("Cannot make compiled field " + fieldIndex + " read only.");
			}
		}
		
		super.setReadOnly(fieldIndex, value);
	}
	
	// Speed up the default writer
	@Override
	public StructureModifier<Object> writeDefaults() throws FieldAccessException {
		DefaultInstances generator = DefaultInstances.DEFAULT;

		// Write a default instance to every field
		for (Map.Entry<Field, Integer> entry : defaultFields.entrySet()) {
			Integer index = entry.getValue();
			Field field = entry.getKey();

			// Special case for Spigot's custom chat components
			// They must be null or messages will be blank
			if (field.getType().getCanonicalName().equals("net.md_5.bungee.api.chat.BaseComponent[]")) {
				write(index, null);
				continue;
			}

			write(index, generator.getDefault(field.getType()));
		}

		return this;
	}
	
	@Override
	public final Object read(int fieldIndex) throws FieldAccessException {
		Object result = readGenerated(fieldIndex);
		
		if (converter != null)
			return converter.getSpecific(result);
		else
			return result;
	}

	/**
	 * Read the given field index using reflection.
	 * @param index - index of field.
	 * @return Resulting value.
	 * @throws FieldAccessException The field doesn't exist, or it cannot be accessed under the current security contraints.
	 */
	protected Object readReflected(int index) throws FieldAccessException {
		return super.read(index);
	}
	
	protected abstract Object readGenerated(int fieldIndex) throws FieldAccessException;

	@Override
	public StructureModifier<Object> write(int index, Object value) throws FieldAccessException {
		if (converter != null)
			value = converter.getGeneric(getFieldType(index), value);
		return writeGenerated(index, value);
	}
	
	/**
	 * Write the given field using reflection.
	 * @param index - index of field.
	 * @param value - new value.
	 * @throws FieldAccessException The field doesn't exist, or it cannot be accessed under the current security contraints.
	 */ 
	protected void writeReflected(int index, Object value) throws FieldAccessException {
		super.write(index, value);
	}
	
	protected abstract StructureModifier<Object> writeGenerated(int index, Object value) throws FieldAccessException;
	
	@Override
	public StructureModifier<Object> withTarget(Object target) {
		if (compiler != null)
			return compiler.compile(super.withTarget(target));
		else
			return super.withTarget(target);
	}
}
