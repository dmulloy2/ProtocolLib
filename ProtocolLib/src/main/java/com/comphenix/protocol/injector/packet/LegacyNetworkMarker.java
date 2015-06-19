package com.comphenix.protocol.injector.packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.compat.guava.Guava;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.google.common.primitives.Bytes;

/**
 * Represents a network marker for 1.6.4 and earlier.
 * @author Kristian
 */
public class LegacyNetworkMarker extends NetworkMarker {
	public LegacyNetworkMarker(@Nonnull ConnectionSide side, byte[] inputBuffer, PacketType type) {
		super(side, inputBuffer, type);
	}

	public LegacyNetworkMarker(@Nonnull ConnectionSide side, ByteBuffer inputBuffer, PacketType type) {
		super(side, inputBuffer, type);
	}
	
	@Override
	protected DataInputStream skipHeader(DataInputStream input) throws IOException {
		// This has already been done
		return input;
	}

	@Override
	protected ByteBuffer addHeader(ByteBuffer buffer, PacketType type) {
		return ByteBuffer.wrap(Bytes.concat(new byte[] { (byte) type.getLegacyId() }, buffer.array()));
	}

	@Override
	protected DataInputStream addHeader(final DataInputStream input, final PacketType type) {
		return Guava.addHeader(input, type);
	}
}