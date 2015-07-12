/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.compat.guava;

import java.io.DataInputStream;
import java.util.Set;
import java.util.logging.Level;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.Range;

/**
 * @author dmulloy2
 */

public class Guava {
	private static GuavaCompat compat;

	static {
		try {
			Range.closed(1, 2);
			compat = new Guava17();
		} catch (Throwable ex) {
			try {
				Class<?> clazz = Class.forName("com.comphenix.protocol.compat.guava.Guava10");
				compat = (GuavaCompat) clazz.newInstance();
			} catch (Throwable ex1) {
				ProtocolLibrary.getStaticLogger().log(Level.SEVERE, "Failed to create Guava 10 compat:", ex1);
			}
		}
	}

	private static GuavaCompat getCompat() {
		return compat;
	}

	public static <C extends Comparable<C>> Range<C> closedRange(C lower, C upper) {
		return getCompat().closedRange(lower, upper);
	}

	public static <C extends Comparable<C>> Range<C> singleton(C singleton) {
		return getCompat().singletonRange(singleton);
	}

	public static Set<Integer> toSet(Range<Integer> range) {
		return getCompat().toSet(range);
	}

	public static DataInputStream addHeader(DataInputStream input, PacketType type) {
		return getCompat().addHeader(input, type);
	}
}