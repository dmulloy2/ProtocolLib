package com.comphenix.protocol.injector;

import org.bukkit.Bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

/**
 * Packets that are known to be transmitted during login. 
 * <p>
 * This may be dynamically extended later.
 * @author Kristian
 */
class LoginPackets {
	private IntegerSet clientSide = new IntegerSet(Packets.PACKET_COUNT);
	private IntegerSet serverSide = new IntegerSet(Packets.PACKET_COUNT);
	
	@SuppressWarnings("deprecation")
	public LoginPackets(MinecraftVersion version) {
		// Ordinary login
		clientSide.add(Packets.Client.HANDSHAKE);
		serverSide.add(Packets.Server.KEY_REQUEST);
		clientSide.add(Packets.Client.KEY_RESPONSE);
		serverSide.add(Packets.Server.KEY_RESPONSE);
		clientSide.add(Packets.Client.CLIENT_COMMAND);
		serverSide.add(Packets.Server.LOGIN);
		
		// List ping
		clientSide.add(Packets.Client.GET_INFO);
		
		// In 1.6.2, Minecraft started sending CUSTOM_PAYLOAD in the server list protocol
		if (version.compareTo(MinecraftVersion.HORSE_UPDATE) >= 0 || triesForgeIntegration()) {
			clientSide.add(Packets.Client.CUSTOM_PAYLOAD);
		}
		serverSide.add(Packets.Server.KICK_DISCONNECT);
		
		// Forge uses packet 250 during login
		if (triesForgeIntegration()) {
			serverSide.add(Packets.Client.CUSTOM_PAYLOAD);
		}
	}
	
	/**
	 * Determine if we are runnign MCPC.
	 * @return TRUE if we are, FALSE otherwise.
	 */
	private static boolean triesForgeIntegration() {
		String version = Bukkit.getVersion();
		return version.contains("MCPC-Plus") || version.contains("Cauldron") || version.contains("Thermos");
	}
	
	/**
	 * Determine if a packet may be sent during login from a given direction.
	 * @param packetId - the ID of the packet.
	 * @param side - the direction.
	 * @return TRUE if it may, FALSE otherwise.
	 */
	@Deprecated
	public boolean isLoginPacket(int packetId, ConnectionSide side) {
		switch (side) {
			case CLIENT_SIDE:
				return clientSide.contains(packetId);
			case SERVER_SIDE:
				return serverSide.contains(packetId);
			case BOTH:
				return clientSide.contains(packetId) || 
					   serverSide.contains(packetId);
			default:
				throw new IllegalArgumentException("Unknown connection side: " + side);
		}
	}	
	
	/**
	 * Determine if a given packet may be sent during login.
	 * @param type - the packet type.
	 * @return TRUE if it may, FALSE otherwise.
	 */
	public boolean isLoginPacket(PacketType type) {
		if (!MinecraftReflection.isUsingNetty())
			return isLoginPacket(type.getLegacyId(), type.getSender().toSide());
		
		return PacketType.Login.Client.getInstance().hasMember(type) || 
			   PacketType.Login.Server.getInstance().hasMember(type) ||
			   PacketType.Status.Client.getInstance().hasMember(type) || 
			   PacketType.Status.Server.getInstance().hasMember(type);
	}
}
