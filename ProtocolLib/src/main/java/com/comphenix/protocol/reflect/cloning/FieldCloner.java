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

package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.comphenix.protocol.reflect.instances.NotConstructableException;

/**
 * Represents a class capable of cloning objects by deeply copying its fields.
 * 
 * @author Kristian
 */
public class FieldCloner implements Cloner {
	protected Cloner defaultCloner;
	protected InstanceProvider instanceProvider;
	
	// Used to clone objects
	protected ObjectWriter writer;
	
	/**
	 * Constructs a field cloner that copies objects by reading and writing the internal fields directly.
	 * @param defaultCloner - the default cloner used while copying fields.
	 * @param instanceProvider - used to construct new, empty copies of a given type.
	 */
	public FieldCloner(Cloner defaultCloner, InstanceProvider instanceProvider) {
		this.defaultCloner = defaultCloner;
		this.instanceProvider = instanceProvider;
		
		// Remember to clone the value too
		this.writer = new ObjectWriter() {
			@Override
			protected void transformField(StructureModifier<Object> modifierSource,
					StructureModifier<Object> modifierDest, int fieldIndex) {
				defaultTransform(modifierDest, modifierDest, getDefaultCloner(), fieldIndex);
			}
		};
	}

	/**
	 * Default implementation of the field transform. Applies a clone operation before a field value is written.
	 * @param modifierSource - modifier for the original object.
	 * @param modifierDest - modifier for the new cloned object.
	 * @param defaultCloner - cloner to use.
	 * @param fieldIndex - the current field index.
	 */
	protected void defaultTransform(StructureModifier<Object> modifierSource, 
									StructureModifier<Object> modifierDest, Cloner defaultCloner, int fieldIndex) {
		
		Object value = modifierSource.read(fieldIndex);
		modifierDest.write(fieldIndex, defaultCloner.clone(value));
	}
	
	@Override
	public boolean canClone(Object source) {
		if (source == null)
			return false;
		
		// Attempt to create the type
		try {
			return instanceProvider.create(source.getClass()) != null;
		} catch (NotConstructableException e) {
			return false;
		}
	}

	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalArgumentException("source cannot be NULL.");
		
		Object copy = instanceProvider.create(source.getClass());
		
		// Copy public and private fields alike. Skip static fields.
		writer.copyTo(source, copy, source.getClass());
		return copy;
	}
	
	/**
	 * Retrieve the default cloner used to clone the content of each field.
	 * @return Cloner used to clone fields.
	 */
	public Cloner getDefaultCloner() {
		return defaultCloner;
	}

	/**
	 * Retrieve the instance provider this cloner is using to create new, empty classes.
	 * @return The instance provider in use.
	 */
	public InstanceProvider getInstanceProvider() {
		return instanceProvider;
	}
}
