package com.comphenix.protocol.reflect.compiler;

import com.comphenix.protocol.reflect.StructureModifier;

/**
 * Used to save the result of an compilation.
 * 
 * @author Kristian
 * @param <TKey> - type of the structure modifier field.
 */
public interface CompileListener<TKey> {
	/**
	 * Invoked when a structure modifier has been successfully compiled.
	 * @param compiledModifier - the compiled structure modifier.
	 */
	public void onCompiled(StructureModifier<TKey> compiledModifier);
}
