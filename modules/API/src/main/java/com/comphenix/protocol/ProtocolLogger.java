/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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
package com.comphenix.protocol;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */
public class ProtocolLogger {
	private static Logger logger;

	protected static void init(Plugin plugin) {
		ProtocolLogger.logger = plugin.getLogger();
	}

	private static boolean isDebugEnabled() {
		try {
			return ProtocolLibrary.getConfig().isDebug();
		} catch (Throwable ex) {
			return true; // For testing
		}
	}

	/**
	 * Logs a message to console with a given level.
	 * @param level Logging level
	 * @param message Message to log
	 * @param args Arguments to format in
	 */
	public static void log(Level level, String message, Object... args) {
		logger.log(level, MessageFormat.format(message, args));
	}

	/**
	 * Logs a method to console with the INFO level.
	 * @param message Message to log
	 * @param args Arguments to format in
	 */
	public static void log(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	/**
	 * Logs a message to console with a given level and exception.
	 * @param level Logging level
	 * @param message Message to log
	 * @param ex Exception to log
	 */
	public static void log(Level level, String message, Throwable ex) {
		logger.log(level, message, ex);
	}

	public static void debug(String message, Object... args) {
		if (isDebugEnabled()) {
			if (logger != null) {
				log("[Debug] " + message, args);
			} else {
				System.out.println("[Debug] " + MessageFormat.format(message, args));
			}
		}
	}
}