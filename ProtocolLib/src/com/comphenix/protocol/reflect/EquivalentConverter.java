package com.comphenix.protocol.reflect;

/**
 * Interface that converts generic objects into types and back.
 * 
 * @author Kristian
 * @param <TType> The specific type.
 */
public interface EquivalentConverter<TType> {
	public TType getSpecific(Object generic);
	public Object getGeneric(TType specific);
}
