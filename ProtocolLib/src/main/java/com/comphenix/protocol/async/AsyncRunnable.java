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

package com.comphenix.protocol.async;

/**
 * A runnable representing a asynchronous event listener.
 * 
 * @author Kristian
 */
public interface AsyncRunnable extends Runnable {
	
	/**
	 * Retrieve a unique worker ID.
	 * @return Unique worker ID.
	 */
	public int getID();
	
	/**
	 * Stop the given runnable.
	 * <p>
	 * This may not occur right away.
	 * @return TRUE if the thread was stopped, FALSE if it was already stopped.
	 */
	public boolean stop() throws InterruptedException;
	
	/**
	 * Determine if we're running or not.
	 * @return TRUE if we're running, FALSE otherwise.
	 */
	public boolean isRunning();

	/**
	 * Determine if this runnable has already run its course.
	 * @return TRUE if it has been stopped, FALSE otherwise.
	 */
	boolean isFinished();
}
