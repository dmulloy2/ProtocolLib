package com.comphenix.protocol.timing;

import com.comphenix.protocol.PacketType;

public interface TimingTracker {

	public static final TimingTracker EMPTY = (packetType, runnable) -> runnable.run();

	void track(PacketType packetType, Runnable runnable);
}
