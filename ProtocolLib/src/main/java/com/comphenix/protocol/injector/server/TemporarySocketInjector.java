package com.comphenix.protocol.injector.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

class TemporarySocketInjector implements SocketInjector {
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
	
	private Player temporaryPlayer;
	private Socket socket;

	// Queue of server packets
	private List<SendPacketCommand> syncronizedQueue = Collections.synchronizedList(new ArrayList<SendPacketCommand>());
	
	/**
	 * Represents a temporary socket injector.
	 * @param temporaryPlayer - temporary player instance.
	 * @param socket - the socket we are representing.
	 * @param fake - whether or not this connection should be ignored.
	 */
	public TemporarySocketInjector(Player temporaryPlayer, Socket socket) {
		this.temporaryPlayer = temporaryPlayer;
		this.socket = socket;
	}

	@Override
	public Socket getSocket() throws IllegalAccessException {
		return socket;
	}

	@Override
	public SocketAddress getAddress() throws IllegalAccessException {
		if (socket != null)
			return socket.getRemoteSocketAddress();
		return null;
	}

	@Override
	public void disconnect(String message) throws InvocationTargetException {
		// We have no choice - disregard message too
		try {
			socket.close();
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
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
		return temporaryPlayer;
	}

	@Override
	public Player getUpdatedPlayer() {
		return temporaryPlayer;
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
}