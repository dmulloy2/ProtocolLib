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

package com.comphenix.protocol;

import org.bukkit.Bukkit;

/**
 * Ignore this class.
 * 
 * @author Kristian
 */
public class Application {
	private static Thread mainThread;
	private static boolean primaryMethod = true;
	
	public static void main(String[] args) {
		// For now, though we might consider making a proper application
		System.out.println("This is a Bukkit library. Place it in the plugin-folder and restart the server!");
	}

	/**
	 * Determine if we are running on the main thread.
	 * @return TRUE if we are, FALSE otherwise.
	 */
	public static boolean isPrimaryThread() {
		if (primaryMethod) {
			try {
				return Bukkit.isPrimaryThread();
			} catch (LinkageError e) {
				primaryMethod = false;
			}
		}
		// Fallback method
		return Thread.currentThread().equals(mainThread);
	}
	
	/**
	 * Register the calling thread as the primary thread.
	 */
	static void registerPrimaryThread() {
		mainThread = Thread.currentThread();
	}
}
