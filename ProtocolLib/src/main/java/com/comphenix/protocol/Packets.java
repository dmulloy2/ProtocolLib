/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol;

import java.util.Set;

import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.IntEnum;

/**
 * List of known packet IDs since 1.3.2.
 * <p>
 * Deprecated: Use {@link PacketType} instead.
 * @author Kristian
 */
@Deprecated
public final class Packets {
	
	/**
	 * The highest possible packet ID. It's unlikely that this value will ever change.
	 */
	public static final int MAXIMUM_PACKET_ID = 255;
	
	/**
	 * The maximum number of unique packet IDs. It's unlikely this will ever change.
	 */
	public static final int PACKET_COUNT = 256;
	
	/**
	 * List of packets sent only by the server.
	 * <p>
	 * Deprecated: Use {@link PacketType} instead.
	 * @author Kristian
	 */
	@Deprecated
	public static final class Server extends IntEnum {
		/**
		 * The singleton instance. Can also be retrieved from the parent class.
		 */
		private static Server INSTANCE = new Server();
		
		public static final int KEEP_ALIVE = 0;
		public static final int LOGIN = 1;
		public static final int CHAT = 3;
		public static final int UPDATE_TIME = 4;
		public static final int ENTITY_EQUIPMENT = 5;
		public static final int SPAWN_POSITION = 6;
		public static final int UPDATE_HEALTH = 8;
		public static final int RESPAWN = 9;
		public static final int FLYING = 10;
		public static final int PLAYER_POSITION = 11;
		public static final int PLAYER_LOOK = 12;
		public static final int PLAYER_LOOK_MOVE = 13;
		/**
		 * Made bi-directional in 1.4.6.
		 */
		public static final int BLOCK_ITEM_SWITCH = 16;
		public static final int ENTITY_LOCATION_ACTION = 17;
		public static final int ARM_ANIMATION = 18;
		public static final int NAMED_ENTITY_SPAWN = 20;
		/**
		 * Removed in 1.4.6 and replaced with VEHICLE_SPAWN. 
		 * @see <a href="http://www.wiki.vg/Protocol_History#2012-12-20">Protocol History - MinecraftCoalition</a>
		 */
		@Deprecated()
		public static final int PICKUP_SPAWN = 21;
		public static final int COLLECT = 22;
		public static final int VEHICLE_SPAWN = 23;
		public static final int MOB_SPAWN = 24;
		public static final int ENTITY_PAINTING = 25;
		public static final int ADD_EXP_ORB = 26;
		public static final int ENTITY_VELOCITY = 28;
		public static final int DESTROY_ENTITY = 29;
		public static final int ENTITY = 30;
		public static final int REL_ENTITY_MOVE = 31;
		public static final int ENTITY_LOOK = 32;
		public static final int REL_ENTITY_MOVE_LOOK = 33;
		public static final int ENTITY_TELEPORT = 34;
		public static final int ENTITY_HEAD_ROTATION = 35;
		public static final int ENTITY_STATUS = 38;
		public static final int ATTACH_ENTITY = 39;
		
		/**
		 * Sent when an entities DataWatcher is updated.
		 * <p>
		 * Remember to clone the packet if you are modifying it.
		 */
		public static final int ENTITY_METADATA = 40;
		public static final int MOB_EFFECT = 41;
		public static final int REMOVE_MOB_EFFECT = 42;
		public static final int SET_EXPERIENCE = 43;
		public static final int UPDATE_ATTRIBUTES = 44;
		public static final int MAP_CHUNK = 51;
		public static final int MULTI_BLOCK_CHANGE = 52;
		public static final int BLOCK_CHANGE = 53;
		public static final int PLAY_NOTE_BLOCK = 54;
		public static final int BLOCK_BREAK_ANIMATION = 55;
		public static final int MAP_CHUNK_BULK = 56;
		public static final int EXPLOSION = 60;
		public static final int WORLD_EVENT = 61;
		public static final int NAMED_SOUND_EFFECT = 62;
		public static final int WORLD_PARTICLES = 63;
		public static final int BED = 70;
		public static final int WEATHER = 71;
		public static final int OPEN_WINDOW = 100;
		public static final int CLOSE_WINDOW = 101;
		public static final int SET_SLOT = 103;
		public static final int WINDOW_ITEMS = 104;
		public static final int CRAFT_PROGRESS_BAR = 105;
		public static final int TRANSACTION = 106;
		public static final int SET_CREATIVE_SLOT = 107;
		public static final int UPDATE_SIGN = 130;
		public static final int ITEM_DATA = 131;
		
		/**
		 * Sent the first time a tile entity (chest inventory, etc.) is withing range of the player, or has been updated.
		 * <p>
		 * Remember to clone the packet if you are modifying it.
		 */
		public static final int TILE_ENTITY_DATA = 132;
		public static final int OPEN_TILE_ENTITY = 133;
		public static final int STATISTIC = 200;
		public static final int PLAYER_INFO = 201;
		public static final int ABILITIES = 202;
		public static final int TAB_COMPLETE = 203;
		public static final int SCOREBOARD_OBJECTIVE = 206;
		public static final int UPDATE_SCORE  = 207;
		public static final int DISPLAY_SCOREBOARD = 208;
		public static final int TEAMS = 209;
		public static final int CUSTOM_PAYLOAD = 250;
		public static final int KEY_RESPONSE = 252;
		public static final int KEY_REQUEST = 253;
		public static final int KICK_DISCONNECT = 255;
		
		/**
		 * This packet was introduced in 1.7.2.
		 */
		public static final int PING_TIME = 230;
		
		/**
		 * This packet was introduced in 1.7.2.
		 */
		public static final int LOGIN_SUCCESS = 232;
		
		/**
		 * A registry that parses between names and packet IDs.
		 * @return The current server registry.
		 */
		public static Server getRegistry() {
			return INSTANCE;
		}
		
		/**
		 * Determine if the given packet is a valid server packet in the current version of Minecraft.
		 * <p>
		 * Use {@link PacketType#isSupported()} instead.
		 * @param packetID - the packet to test.
		 * @return TRUE if this packet is supported, FALSE otherwise.
		 * @throws FieldAccessException If we're unable to retrieve the server packet data from Minecraft.
		 */
		@Deprecated
		public static boolean isSupported(int packetID) throws FieldAccessException {
			return PacketFilterManager.getServerPackets().contains(packetID);
		}
		
		/**
		 * Retrieve every client packet the current version of Minecraft is aware of.
		 * @return Every supported server packet.
		 * @throws FieldAccessException If we're unable to retrieve the server packet data from Minecraft.
		 */
		@Deprecated
		public static Set<Integer> getSupported() throws FieldAccessException {
			return PacketFilterManager.getServerPackets();
		}
		
		// We only allow a single instance of this class
	    private Server() {
			super();
		}
	}
	
	/**
	 * List of packets sent by the client.
	 * <p>
	 * Deprecated: Use {@link PacketType} instead.
	 * @author Kristian
	 */
	@Deprecated
	public static class Client extends IntEnum {
		/**
		 * The singleton instance. Can also be retrieved from the parent class.
		 */
		private static Client INSTANCE = new Client();
		
		public static final int KEEP_ALIVE = 0;
		public static final int LOGIN = 1;
		public static final int HANDSHAKE = 2;
		public static final int CHAT = 3;
		public static final int USE_ENTITY = 7;
		
		/**
		 * Since 1.3.1, the client no longer sends a respawn packet. Moved to CLIENT_COMMAND.
		 */
		@Deprecated
		public static final int RESPAWN = 9;
		
		public static final int FLYING = 10;
		public static final int PLAYER_POSITION = 11;
		public static final int PLAYER_LOOK = 12;
		public static final int PLAYER_LOOK_MOVE = 13;
		public static final int BLOCK_DIG = 14;
		public static final int PLACE = 15;
		public static final int BLOCK_ITEM_SWITCH = 16;
		public static final int ARM_ANIMATION = 18;
		public static final int ENTITY_ACTION = 19;
		public static final int PLAYER_INPUT = 27;
		public static final int CLOSE_WINDOW = 101;
		public static final int WINDOW_CLICK = 102;
		public static final int TRANSACTION = 106;
		public static final int SET_CREATIVE_SLOT = 107;
		public static final int BUTTON_CLICK = 108;
		public static final int UPDATE_SIGN = 130;
		public static final int ABILITIES = 202;
		public static final int TAB_COMPLETE = 203;
		public static final int LOCALE_AND_VIEW_DISTANCE = 204;
		public static final int CLIENT_COMMAND = 205;
		public static final int CUSTOM_PAYLOAD = 250;
		public static final int KEY_RESPONSE = 252;
		public static final int GET_INFO = 254;
		public static final int KICK_DISCONNECT = 255;
		
		/**
		 * This packet was introduced in 1.7.2.
		 */
		public static final int PING_TIME = 230;
		
		/**
		 * This packet was introduced in 1.7.2.
		 */
		public static final int LOGIN_START = 231;
		
		/**
		 * A registry that parses between names and packet IDs.
		 * @return The current client registry.
		 */
		public static Client getRegistry() {
			return INSTANCE;
		}
		
		/**
		 * Determine if the given packet is a valid client packet in the current version of Minecraft.
		 * @param packetID - the packet to test.
		 * @return TRUE if this packet is supported, FALSE otherwise.
		 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
		 */
		public static boolean isSupported(int packetID) throws FieldAccessException {
			return PacketFilterManager.getClientPackets().contains(packetID);
		}
		
		/**
		 * Retrieve every client packet the current version of Minecraft is aware of.
		 * @return Every supported client packet.
		 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
		 */
		public static Set<Integer> getSupported() throws FieldAccessException {
			return PacketFilterManager.getClientPackets();
		}
		
		// Like above
		private Client() {
			super();
		}
	}
	
	/**
	 * A registry that parses between names and packet IDs.
	 * <p>
	 * Deprecated: Use {@link PacketType} instead.
	 * @return The current client registry.
	 */
	@Deprecated
	public static Server getServerRegistry() {
		return Server.getRegistry();
	}

	/**
	 * A registry that parses between names and packet IDs.
	 * <p>
	 * Deprecated: Use {@link PacketType} instead.
	 * @return The current server registry.
	 */
	@Deprecated
	public static Client getClientRegistry() {
		return Client.INSTANCE;
	}
	
	/**
	 * Find a packet by name. Must be capitalized and use underscores.
	 * <p>
	 * Deprecated: Use {@link PacketType} instead.
	 * @param name - name of packet to find.
	 * @return The packet ID found.
	 */
	@Deprecated
	public static int valueOf(String name) {
		Integer serverAttempt = Server.INSTANCE.valueOf(name);
		
		if (serverAttempt != null)
			return serverAttempt;
		else
			return Client.INSTANCE.valueOf(name);
	}
	
	/**
	 * Retrieves the name of a packet.
	 * <p>
	 * Deprecated: Use {@link PacketType} instead.
	 * @param packetID - packet to retrieve name.
	 * @return The name, or NULL if unable to find such a packet.
	 */
	@Deprecated
	public static String getDeclaredName(int packetID) {
		String serverAttempt = Server.INSTANCE.getDeclaredName(packetID);
		
		if (serverAttempt != null)
			return serverAttempt;
		else
			return Client.INSTANCE.getDeclaredName(packetID);
	}
}
