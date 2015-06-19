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
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

/**
 * @author dmulloy2
 */

public class Guava10 implements GuavaCompat {

	@Override
	public <C extends Comparable<C>> Range<C> closedRange(C lower, C upper) {
		return Ranges.closed(lower, upper);
	}

	@Override
	public <C extends Comparable<C>> Range<C> singletonRange(C value) {
		return Ranges.singleton(value);
	}

	@Override
	public Set<Integer> toSet(Range<Integer> range) {
		return range.asSet(DiscreteDomains.integers());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public DataInputStream addHeader(final DataInputStream input, final PacketType type) {
		InputSupplier<InputStream> header = new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				byte[] data = new byte[] { (byte) type.getLegacyId() };
				return new ByteArrayInputStream(data);
			}
		};

		InputSupplier<InputStream> data = new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return input;
			}
		};
		
		// Combine them into a single stream
		try {
			return new DataInputStream(ByteStreams.join(header, data).getInput());
		} catch (IOException e) {
			throw new RuntimeException("Cannot add header.", e);
		}
	}
}