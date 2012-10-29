package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;

public interface ErrorReporter {

	/**
	 * Prints a small minimal error report about an exception from another plugin.
	 * @param sender - the other plugin.
	 * @param methodName - name of the caller method.
	 * @param error - the exception itself.
	 */
	public abstract void reportMinimal(Plugin sender, String methodName, Throwable error);

	/**
	 * Prints a warning message from the current plugin.
	 * @param sender - the object containing the caller method. 
	 * @param message - error message. 
	 */
	public abstract void reportWarning(Object sender, String message);

	/**
	 * Prints a warning message from the current plugin.
	 * @param sender - the object containing the caller method. 
	 * @param message - error message. 
	 * @param error - the exception that was thrown.
	 */
	public abstract void reportWarning(Object sender, String message, Throwable error);

	/**
	 * Prints a detailed error report about an unhandled exception.
	 * @param sender - the object containing the caller method.
	 * @param message - an error message to include.
	 * @param error - the exception that was thrown in the caller method.
	 * @param parameters - parameters from the caller method.
	 */
	public abstract void reportDetailed(Object sender, String message, Throwable error, Object... parameters);

}