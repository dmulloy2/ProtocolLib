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
	private static boolean debugEnabled = true;
	private static Logger logger = Logger.getLogger("Minecraft");

	protected static void init(Plugin plugin) {
		logger = plugin.getLogger();

		try {
			debugEnabled = plugin.getConfig().getBoolean("global.debug", false);
		} catch (Throwable ignored) { }
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
		if (debugEnabled) {
			log("[Debug] " + message, args);
		}
	}

	public static void debug(String message, Throwable ex) {
		if (debugEnabled) {
			logger.log(Level.WARNING, "[Debug] " + message, ex);
		}
	}
}
