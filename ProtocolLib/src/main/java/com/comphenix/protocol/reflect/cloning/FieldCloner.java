package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.InstanceProvider;

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
				Object value = modifierSource.read(fieldIndex);
				modifierDest.write(fieldIndex, getDefaultCloner().clone(value));
			}
		};
	}

	@Override
	public boolean canClone(Object source) {
		if (source == null)
			return false;
		
		// Attempt to create the type
		return instanceProvider.create(source.getClass()) != null;
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
