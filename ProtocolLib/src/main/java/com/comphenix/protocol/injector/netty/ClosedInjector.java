package com.comphenix.protocol.injector.netty;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;


/**
 * Represents a closed injector.
 * @author Kristian
 */
class ClosedInjector implements Injector {
	private Player player;

	/**
	 * Construct a new injector that is always closed.
	 * @param player - the associated player.
	 */
	public ClosedInjector(Player player) {
		this.player = player;
	}

	@Override
	public boolean inject() {
		return false;
	}

	@Override
	public void close() {
		// Do nothing
	}

	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
		// Do nothing
	}

	@Override
	public void recieveClientPacket(Object packet, NetworkMarker marker, boolean filtered) {
		// Do nothing
	}

	@Override
	public Protocol getCurrentProtocol() {
		return Protocol.HANDSHAKING;
	}

	@Override
	public boolean unignorePacket(Object packet) {
		return false;
	}

	@Override
	public boolean ignorePacket(Object packet) {
		return false;
	}

	@Override
	public NetworkMarker getMarker(Object packet) {
		return null;
	}

	@Override
	public void saveMarker(Object packet, NetworkMarker marker) {
		// Do nothing
	}

	@Override
	public void saveEvent(NetworkMarker marker, PacketEvent event) {
		// Do nothing
	}

	@Override
	public void setUpdatedPlayer(Player player) {
		// Do nothing
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public boolean isInjected() {
		return false;
	}

	@Override
	public boolean isClosed() {
		return true;
	}
}
