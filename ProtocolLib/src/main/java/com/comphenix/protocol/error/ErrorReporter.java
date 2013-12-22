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

import com.comphenix.protocol.error.Report.ReportBuilder;

/**
 * Represents an object that can forward an error {@link Report} to the display and permanent storage.
 * 
 * @author Kristian
 */
public interface ErrorReporter {
	/**
	 * Prints a small minimal error report regarding an exception from another plugin.
	 * @param sender - the other plugin.
	 * @param methodName - name of the caller method.
	 * @param error - the exception itself.
	 */
	public abstract void reportMinimal(Plugin sender, String methodName, Throwable error);

	/**
	 * Prints a small minimal error report regarding an exception from another plugin.
	 * @param sender - the other plugin.
	 * @param methodName - name of the caller method.
	 * @param error - the exception itself.
	 * @param parameters - any relevant parameters to print.
	 */
	public abstract void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters);
	
	/**
	 * Prints a debug message from the current sender.
	 * <p>
	 * Most users will not see this message.
	 * @param sender - the sender.
	 * @param report - the report.
	 */
	public abstract void reportDebug(Object sender, Report report);
	
	/**
	 * Prints a debug message from the current sender.
	 * @param sender - the sender.
	 * @param report - the report builder.
	 */
	public abstract void reportDebug(Object sender, ReportBuilder builder);
	
	/**
	 * Prints a warning message from the current plugin.
	 * @param sender - the object containing the caller method. 
	 * @param report - an error report to include.
	 */
	public abstract void reportWarning(Object sender, Report report);
	
	/**
	 * Prints a warning message from the current plugin.
	 * @param sender - the object containing the caller method. 
	 * @param reportBuilder - an error report builder that will be used to get the report.
	 */
	public abstract void reportWarning(Object sender, ReportBuilder reportBuilder);

	/**
	 * Prints a detailed error report about an unhandled exception.
	 * @param sender - the object containing the caller method.
	 * @param report - an error report to include.
	 */
	public abstract void reportDetailed(Object sender, Report report);
	
	/**
	 * Prints a detailed error report about an unhandled exception.
	 * @param sender - the object containing the caller method.
	 * @param reportBuilder - an error report builder that will be used to get the report.
	 */
	public abstract void reportDetailed(Object sender, ReportBuilder reportBuilder);
}