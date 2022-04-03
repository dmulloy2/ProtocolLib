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

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents a compiled structure modifier.
 *
 * @author Kristian
 */
public abstract class CompiledStructureModifier extends StructureModifier<Object> {

	// Used to compile instances of structure modifiers
	protected final StructureCompiler compiler;

	public CompiledStructureModifier(StructureCompiler compiler, StructureModifier<Object> other) {
		this.compiler = compiler;
		this.customConvertHandling = true;
		this.initialize(other);
	}

	// Speed up the default writer
	@Override
	public StructureModifier<Object> writeDefaults() throws FieldAccessException {
		DefaultInstances generator = DefaultInstances.DEFAULT;

		// Write a default instance to every field
		for (Map.Entry<FieldAccessor, Integer> entry : this.defaultFields.entrySet()) {
			Integer index = entry.getValue();
			Field field = entry.getKey().getField();

			// Special case for Spigot's custom chat components
			// They must be null or messages will be blank
			if (field.getType().getCanonicalName().equals("net.md_5.bungee.api.chat.BaseComponent[]")) {
				this.write(index, null);
				continue;
			}

			this.write(index, generator.getDefault(field.getType()));
		}

		return this;
	}

	@Override
	public final Object read(int fieldIndex) throws FieldAccessException {
		Object result = this.readGenerated(this.target, fieldIndex);
		if (this.converter != null) {
			return this.converter.getSpecific(result);
		} else {
			return result;
		}
	}

	protected abstract Object readGenerated(Object target, int fieldIndex);

	@Override
	public StructureModifier<Object> write(int index, Object value) throws FieldAccessException {
		if (this.converter != null) {
			value = this.converter.getGeneric(value);
		}
		return this.writeGenerated(this.target, index, value);
	}

	protected abstract StructureModifier<Object> writeGenerated(Object target, int index, Object value);

	@Override
	public StructureModifier<Object> withTarget(Object target) {
		if (this.compiler != null) {
			return this.compiler.compile(super.withTarget(target));
		} else {
			return super.withTarget(target);
		}
	}
}
