/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.utility;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dmulloy2
 */

// TODO Switch to AutoCloseable w/ Java 7
@Deprecated
public class Closer implements AutoCloseable {
	private final List<Closeable> list;

	private Closer() {
		this.list = new ArrayList<>();
	}

	public static Closer create() {
		return new Closer();
	}

	public static void closeQuietly(Closeable close) {
		try {
			close.close();
		} catch (Throwable ex) {
		}
	}

	public <C extends Closeable> C register(C close) {
		list.add(close);
		return close;
	}

	@Override
	public void close() {
		for (Closeable close : list) {
			closeQuietly(close);
		}
	}

}
