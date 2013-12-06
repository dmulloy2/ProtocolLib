package com.comphenix.protocol.injector.netty;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;

class NettyNetworkMarker extends NetworkMarker {
	public NettyNetworkMarker(@Nonnull ConnectionSide side, byte[] inputBuffer) {
		super(side, inputBuffer, null);
	}

	public NettyNetworkMarker(@Nonnull ConnectionSide side, ByteBuffer inputBuffer) {
		super(side, inputBuffer, null);
	}

	@Override
	protected DataInputStream skipHeader(DataInputStream input) throws IOException {
		// Skip the variable int containing the packet ID
		getSerializer().deserializeVarInt(input);
		return input;
	}
	
	@Override
	protected ByteBuffer addHeader(ByteBuffer buffer, PacketType type) {
		// We don't have to add anything - it's already there
		return buffer;
	}
	
	@Override
	protected DataInputStream addHeader(DataInputStream input, PacketType type) {
		// As above
		return input;
	}
}
