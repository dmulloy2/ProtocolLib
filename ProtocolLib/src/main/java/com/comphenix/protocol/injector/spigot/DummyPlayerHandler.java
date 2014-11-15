package com.comphenix.protocol.injector.spigot;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;

/**
 * Dummy player handler that simply delegates to its parent Spigot packet injector.
 * 
 * @author Kristian
 */
class DummyPlayerHandler extends AbstractPlayerHandler {
	private SpigotPacketInjector injector;
	
	@Override
	public int getProtocolVersion(Player player) {
		// Just use the server version
		return MinecraftProtocolVersion.getCurrentVersion();
	}
	
	public DummyPlayerHandler(SpigotPacketInjector injector, PacketTypeSet sendingFilters) {
		super(sendingFilters);
		this.injector = injector;
	}

	@Override
	public boolean uninjectPlayer(InetSocketAddress address) {
		return true;
	}

	@Override
	public boolean uninjectPlayer(Player player) {
		injector.uninjectPlayer(player);
		return true;
	}
	
	@Override
	public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
		injector.sendServerPacket(receiver, packet, marker, filters);
	}

	@Override
	public void recieveClientPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
		injector.processPacket(player, mcPacket);
	}
	
	@Override
	public void injectPlayer(Player player, ConflictStrategy strategy) {
		// We don't care about strategy
		injector.injectPlayer(player);
	}
	
	@Override
	public boolean hasMainThreadListener(PacketType type) {
		return sendingFilters.contains(type);
	}
	
	@Override
	public void handleDisconnect(Player player) {
		// Just ignore
	}
	
	@Override
	public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
		// Associate this buffered data
		if (buffered != null) {
			injector.saveBuffered(packet.getHandle(), buffered);
		}
		return null;
	}
	
	@Override
	public void updatePlayer(Player player) {
		// Do nothing
	}
}
