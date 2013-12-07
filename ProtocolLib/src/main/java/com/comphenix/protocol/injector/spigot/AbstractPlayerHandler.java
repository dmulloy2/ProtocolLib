package com.comphenix.protocol.injector.spigot;

import java.io.DataInputStream;
import java.util.Set;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;

public abstract class AbstractPlayerHandler implements PlayerInjectionHandler {
	protected PacketTypeSet sendingFilters;

	public AbstractPlayerHandler(PacketTypeSet sendingFilters) {
		this.sendingFilters = sendingFilters;
	}

	@Override
	public void setPlayerHook(GamePhase phase, PlayerInjectHooks playerHook) {
		throw new UnsupportedOperationException("This is not needed in Spigot.");
	}

	@Override
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		throw new UnsupportedOperationException("This is not needed in Spigot.");
	}

	@Override
	public void addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		sendingFilters.addType(type);
	}

	@Override
	public void removePacketHandler(PacketType type) {
		sendingFilters.removeType(type);
	}

	@Override
	public Set<PacketType> getSendingFilters() {
		return sendingFilters.values();
	}

	@Override
	public void close() {
		sendingFilters.clear();
	}

	@Override
	public PlayerInjectHooks getPlayerHook(GamePhase phase) {
		return PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	}

	@Override
	public boolean canRecievePackets() {
		return true;
	}

	@Override
	public PlayerInjectHooks getPlayerHook() {
		// Pretend that we do
		return PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	}

	@Override
	public Player getPlayerByConnection(DataInputStream inputStream) throws InterruptedException {
		throw new UnsupportedOperationException("This is not needed in Spigot.");
	}

	@Override
	public void checkListener(PacketListener listener) {
		// They're all fine!
	}

	@Override
	public void checkListener(Set<PacketListener> listeners) {
		// Yes, really
	}
}