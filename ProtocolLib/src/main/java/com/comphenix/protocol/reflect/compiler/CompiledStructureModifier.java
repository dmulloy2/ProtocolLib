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

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;

/**
 * Represents a compiled structure modifier.
 * 
 * @author Kristian
 * @param <TField> Field type.
 */
public abstract class CompiledStructureModifier<TField> extends StructureModifier<TField> {
	// Used to compile instances of structure modifiers
	protected StructureCompiler compiler;
	
	public CompiledStructureModifier() {
		super();
		customConvertHandling = true;
	}
	
	// Speed up the default writer
	@SuppressWarnings("unchecked")
	@Override
	public StructureModifier<TField> writeDefaults() throws FieldAccessException {
		
		DefaultInstances generator = DefaultInstances.DEFAULT;
		
		// Write a default instance to every field
		for (Map.Entry<Field, Integer> entry  : defaultFields.entrySet()) {
			Integer index = entry.getValue();
			Field field = entry.getKey();
			
			write(index, (TField) generator.getDefault(field.getType()));
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final TField read(int fieldIndex) throws FieldAccessException {
		Object result = readGenerated(fieldIndex);
		
		if (converter != null)
			return converter.getSpecific(result);
		else
			return (TField) result;
	}
	
	protected abstract Object readGenerated(int fieldIndex) throws FieldAccessException;

	@SuppressWarnings("unchecked")
	@Override
	public StructureModifier<TField> write(int index, Object value) throws FieldAccessException {
		if (converter != null)
			value = converter.getGeneric((TField) value);
		return writeGenerated(index, value);
	}
	
	protected abstract StructureModifier<TField> writeGenerated(int index, Object value) throws FieldAccessException;
	
	@Override
	public StructureModifier<TField> withTarget(Object target) {
		if (compiler != null)
			return compiler.compile(super.withTarget(target));
		else
			return super.withTarget(target);
	}
}
