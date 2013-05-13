package com.comphenix.protocol.injector.server;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

public class BukkitSocketInjector implements SocketInjector {
	/**
	 * Represents a single send packet command.
	 * @author Kristian
	 */
	static class SendPacketCommand {
		private final Object packet;
		private final boolean filtered;
		
		public SendPacketCommand(Object packet, boolean filtered) {
			this.packet = packet;
			this.filtered = filtered;
		}

		public Object getPacket() {
			return packet;
		}

		public boolean isFiltered() {
			return filtered;
		}
	}
	
	private Player player;
	
	// Queue of server packets
	private List<SendPacketCommand> syncronizedQueue = Collections.synchronizedList(new ArrayList<SendPacketCommand>());
	
	/**
	 * Represents a temporary socket injector.
	 * @param temporaryPlayer - 
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
	public void sendServerPacket(Object packet, boolean filtered)
			throws InvocationTargetException {
		SendPacketCommand command = new SendPacketCommand(packet, filtered);
		
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
			    for (SendPacketCommand command : syncronizedQueue) {
					delegate.sendServerPacket(command.getPacket(), command.isFiltered());
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
