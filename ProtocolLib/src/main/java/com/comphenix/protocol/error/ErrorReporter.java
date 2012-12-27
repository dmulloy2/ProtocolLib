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
	 * Prints a small minimal error report about an exception from another plugin.
	 * @param sender - the other plugin.
	 * @param methodName - name of the caller method.
	 * @param error - the exception itself.
	 * @param parameters - any relevant parameters to print.
	 */
	public abstract void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters);
	
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