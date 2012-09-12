package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;


/**
 * Represents a field that will revert to its original state when this class is garbaged collected.
 * 
 * @author Kristian
 */
public class VolatileField {

	private Field field;
	private Object container;
	
	// The current and previous values
	private Object previous;
	private Object current;
	
	// Whether or not we must reset or load
	private boolean previousLoaded;
	private boolean currentSet;

	// Whether or not to break access restrictions
	private boolean forceAccess;
	
	/**
	 * Initializes a volatile field with an associated object.
	 * @param field - the field.
	 * @param container - the object this field belongs to.
	 */
	public VolatileField(Field field, Object container) {
		this.field = field;
		this.container = container;
	}
	
	/**
	 * Initializes a volatile field with an associated object.
	 * @param field - the field.
	 * @param container - the object this field belongs to.
	 * @param forceAccess - whether or not to override any scope restrictions.
	 */
	public VolatileField(Field field, Object container, boolean forceAccess) {
		this.field = field;
		this.container = container;
		this.forceAccess = forceAccess;
	}
	
	/**
	 * Retrieves the current field.
	 * @return The stored field.
	 */
	public Field getField() {
		return field;
	}
	
	/**
	 * Retrieves the object the field is stored.
	 * @return The reference object.
	 */
	public Object getContainer() {
		return container;
	}
	
	/**
	 * Retrieves whether or not not to override any scope restrictions.
	 * @return TRUE if we override scope, FALSE otherwise.
	 */
	public boolean isForceAccess() {
		return forceAccess;
	}

	/**
	 * Sets whether or not not to override any scope restrictions.
	 * @param forceAccess - TRUE if we override scope, FALSE otherwise.
	 */
	public void setForceAccess(boolean forceAccess) {
		this.forceAccess = forceAccess;
	}

	/**
	 * Retrieves the current field value.
	 * @return The current field value.
	 */
	public Object getValue() {
		// Retrieve the correct value
		if (!currentSet) {
			ensureLoaded();
			return previous;
		} else {
			return current;
		}
	}
	
	/**
	 * Retrieves the field value before the previous setValue(), unless saveValue() has been called.
	 * @return Previous value.
	 */
	public Object getOldValue() {
		ensureLoaded();
		return previous;
	}
	
	/**
	 * Sets the current value. This will be reverted unless saveValue() is called.
	 * @param newValue - new field value.
	 */
	public void setValue(Object newValue) {
		
		// Remember to safe the previous value
		ensureLoaded();
		
		try {
			FieldUtils.writeField(field, container, newValue, forceAccess);
			current = newValue;
			currentSet = true;
			
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to read field " + field.getName(), e);
		}
	}
	
	/**
	 * Ensure that the current value is still set after this class has been garbaged collected.
	 */
	public void saveValue() {
		previous = current;
		currentSet = false;
	}
	
	/**
	 * Revert to the previously set value.
	 */
	public void revertValue() {
		// Reset value.
		if (currentSet) {
			setValue(previous);
			currentSet = false;
		}
	}
	
	private void ensureLoaded() {
		// Load the value if we haven't already
		if (!previousLoaded) {
			try {
				previous = FieldUtils.readField(field, container, forceAccess);
				previousLoaded = true;
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to read field " + field.getName(), e);
			}
		}
	}
		
	@Override
	protected void finalize() throws Throwable {
		revertValue();
	}
}
