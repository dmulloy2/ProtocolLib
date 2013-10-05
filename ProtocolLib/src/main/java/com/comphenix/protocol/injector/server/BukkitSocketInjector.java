package com.comphenix.protocol.injector.server;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.NetworkMarker;

public class BukkitSocketInjector implements SocketInjector {
	private Player player;
	
	// Queue of server packets
	private List<QueuedSendPacket> syncronizedQueue = Collections.synchronizedList(new ArrayList<QueuedSendPacket>());
	
	/**
	 * Represents a temporary socket injector.
	 * @param player - a temporary player.
	 */
	public BukkitSocketInjector(Player player) {
		if (player == null)
			throw new IllegalArgumentException("Player cannot be NULL.");
		this.player = player;
	}

	@Override
	public Socket getSocket() throws IllegalAccessException {
		throw new UnsupportedOperationException("Cannot get socket from Bukkit player.");
	}

	@Override
	public SocketAddress getAddress() throws IllegalAccessException {
		return player.getAddress();
	}

	@Override
	public void disconnect(String message) throws InvocationTargetException {
		player.kickPlayer(message);
	}

	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered)
			throws InvocationTargetException {
		QueuedSendPacket command = new QueuedSendPacket(packet, marker, filtered);
		
		// Queue until we can find something better
		syncronizedQueue.add(command);
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Player getUpdatedPlayer() {
		return player;
	}

	@Override
	public void transferState(SocketInjector delegate) {
		// Transmit all queued packets to a different injector.
		try {
			synchronized(syncronizedQueue) {
			    for (QueuedSendPacket command : syncronizedQueue) {
					delegate.sendServerPacket(command.getPacket(), command.getMarker(), command.isFiltered());
			    }
			    syncronizedQueue.clear();
			}
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unable to transmit packets to " + delegate + " from old injector.", e);
		}
	}

	@Override
	public void setUpdatedPlayer(Player updatedPlayer) {
		this.player = updatedPlayer;
	}
}
