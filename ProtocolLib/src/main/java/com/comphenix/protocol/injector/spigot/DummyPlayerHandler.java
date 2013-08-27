package com.comphenix.protocol.injector.spigot;

import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Set;
import org.bukkit.entity.Player;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;

/**
 * Dummy player handler that simply delegates to its parent Spigot packet injector.
 * 
 * @author Kristian
 */
class DummyPlayerHandler implements PlayerInjectionHandler {
	private SpigotPacketInjector injector;
	private IntegerSet sendingFilters;
	
	public DummyPlayerHandler(SpigotPacketInjector injector, IntegerSet sendingFilters) {
		this.injector = injector;
		this.sendingFilters = sendingFilters;
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
	public void setPlayerHook(GamePhase phase, PlayerInjectHooks playerHook) {
		throw new UnsupportedOperationException("This is not needed in Spigot.");
	}
	
	@Override
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		throw new UnsupportedOperationException("This is not needed in Spigot.");
	}

	@Override
	public void addPacketHandler(int packetID) {
		sendingFilters.add(packetID);
	}
	
	@Override
	public void removePacketHandler(int packetID) {
		sendingFilters.remove(packetID);
	}
	
	@Override
	public Set<Integer> getSendingFilters() {
		return sendingFilters.toSet();
	}
	
	@Override
	public void close() {
		sendingFilters.clear();
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
		injector.sendServerPacket(reciever, packet, marker, filters);
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
	public void handleDisconnect(Player player) {
		// Just ignore
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
	public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
		// Associate this buffered data
		if (buffered != null) {
			injector.saveBuffered(packet.getHandle(), buffered);
		}
		return null;
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
	
	@Override
	public void updatePlayer(Player player) {
		// Do nothing
	}
}
