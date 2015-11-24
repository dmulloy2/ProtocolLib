package com.comphenix.protocol.reflect.cloning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a cloner that can clone any class that implements Serializable.
 * @author Kristian Stangeland
 */
public class SerializableCloner implements Cloner {

    @Override
    public boolean canClone(Object source) {
        if (source == null)
            return false;
        return source instanceof Serializable;
    }

    @Override
    public Object clone(Object source) {
        return SerializableCloner.clone((Serializable) source);
    }

    /**
     * Clone the given object using serialization.
     * @param <T> Type
     * @param obj - the object to clone.
     * @return The cloned object.
     * @throws RuntimeException If we were unable to clone the object.
     */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T clone(final T obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(out);

			oout.writeObject(obj);
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
			return (T) in.readObject();
		} catch (Exception e) {
			throw new RuntimeException("Unable to clone object " + obj + " (" + obj.getClass().getName() + ")", e);
		}
	}
}
