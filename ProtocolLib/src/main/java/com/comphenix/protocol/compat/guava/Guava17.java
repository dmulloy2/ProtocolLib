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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.io.ByteSource;

/**
 * @author dmulloy2
 */

public class Guava17 implements GuavaCompat {

	@Override
	public <C extends Comparable<C>> Range<C> closedRange(C lower, C upper) {
		return Range.closed(lower, upper);
	}

	@Override
	public <C extends Comparable<C>> Range<C> singletonRange(C singleton) {
		return Range.singleton(singleton);
	}

	@Override
	public Set<Integer> toSet(Range<Integer> range) {
		return ContiguousSet.create(range, DiscreteDomain.integers());
	}

	@Override
	public DataInputStream addHeader(final DataInputStream input, final PacketType type) {
		ByteSource header = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				byte[] data = new byte[] { (byte) type.getLegacyId() };
				return new ByteArrayInputStream(data);
			}
		};

		ByteSource data = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return input;
			}
		};
		
		// Combine them into a single stream
		try {
			return new DataInputStream(ByteSource.concat(header, data).openStream());
		} catch (IOException e) {
			throw new RuntimeException("Cannot add header.", e);
		}
	}
}