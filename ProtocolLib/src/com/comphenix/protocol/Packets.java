package com.comphenix.protocol;

import com.comphenix.protocol.reflect.IntEnum;

/**
 * List of known packet IDs since 1.3.2.
 * 
 * @author Kristian
 */
public final class Packets {
	
	/**
	 * List of every known server packet.
	 */
	public static final Server SERVER = Server.INSTANCE;

	/**
	 * List of every known client packet.
	 */
	public static final Client CLIENT = Client.INSTANCE;
	
	/**
	 * List of packets sent only by the server.
	 * @author Kristian
	 */
	public static final class Server extends IntEnum {
		/**
		 * The singleton instance. Can also be retrieved from the parent class.
		 */
		public static Server INSTANCE = new Server();
		
		public final int KEEP_ALIVE = 0;
		public final int LOGIN = 1;
		public final int CHAT = 3;
		public final int UPDATE_TIME = 4;
		public final int ENTITY_EQUIPMENT = 5;
		public final int SPAWN_POSITION = 6;
		public final int UPDATE_HEALTH = 8;
		public final int RESPAWN = 9;
		public final int FLYING = 10;
		public final int PLAYER_POSITION = 11;
		public final int PLAYER_LOOK = 12;
		public final int PLAYER_LOOK_MOVE = 13;
		public final int ENTITY_LOCATION_ACTION = 17;
		public final int ARM_ANIMATION = 18;
		public final int NAMED_ENTITY_SPAWN = 20;
		public final int PICKUP_SPAWN = 21;
		public final int COLLECT = 22;
		public final int VEHICLE_SPAWN = 23;
		public final int MOB_SPAWN = 24;
		public final int ENTITY_PAINTING = 25;
		public final int ADD_EXP_ORB = 26;
		public final int ENTITY_VELOCITY = 28;
		public final int DESTROY_ENTITY = 29;
		public final int ENTITY = 30;
		public final int REL_ENTITY_MOVE = 31;
		public final int ENTITY_LOOK = 32;
		public final int REL_ENTITY_MOVE_LOOK = 33;
		public final int ENTITY_TELEPORT = 34;
		public final int ENTITY_HEAD_ROTATION = 35;
		public final int ENTITY_STATUS = 38;
		public final int ATTACH_ENTITY = 39;
		public final int ENTITY_METADATA = 40;
		public final int MOB_EFFECT = 41;
		public final int REMOVE_MOB_EFFECT = 42;
		public final int SET_EXPERIENCE = 43;
		public final int MAP_CHUNK = 51;
		public final int MULTI_BLOCK_CHANGE = 52;
		public final int BLOCK_CHANGE = 53;
		public final int PLAY_NOTE_BLOCK = 54;
		public final int BLOCK_BREAK_ANIMATION = 55;
		public final int MAP_CHUNK_BULK = 56;
		public final int EXPLOSION = 60;
		public final int WORLD_EVENT = 61;
		public final int NAMED_SOUND_EFFECT = 62;
		public final int BED = 70;
		public final int WEATHER = 71;
		public final int OPEN_WINDOW = 100;
		public final int CLOSE_WINDOW = 101;
		public final int SET_SLOT = 103;
		public final int WINDOW_ITEMS = 104;
		public final int CRAFT_PROGRESS_BAR = 105;
		public final int TRANSACTION = 106;
		public final int SET_CREATIVE_SLOT = 107;
		public final int UPDATE_SIGN = 130;
		public final int ITEM_DATA = 131;
		public final int TILE_ENTITY_DATA = 132;
		public final int STATISTIC = 200;
		public final int PLAYER_INFO = 201;
		public final int ABILITIES = 202;
		public final int TAB_COMPLETE = 203;
		public final int CUSTOM_PAYLOAD = 250;
		public final int KEY_RESPONSE = 252;
		public final int KEY_REQUEST = 253;
		public final int KICK_DISCONNECT = 255;
		
		// We only allow a single instance of this class
	    private Server() {
			super();
		}
	}
	
	/**
	 * List of packets sent by the client.
	 * @author Kristian
	 */
	public static class Client extends IntEnum {
		/**
		 * The singleton instance. Can also be retrieved from the parent class.
		 */
		public static Client INSTANCE = new Client();
		
		public final int KEEP_ALIVE = 0;
		public final int LOGIN = 1;
		public final int HANDSHAKE = 2;
		public final int CHAT = 3;
		public final int USE_ENTITY = 7;
		public final int RESPAWN = 9;
		public final int FLYING = 10;
		public final int PLAYER_POSITION = 11;
		public final int PLAYER_LOOK = 12;
		public final int PLAYER_LOOK_MOVE = 13;
		public final int BLOCK_DIG = 14;
		public final int PLACE = 15;
		public final int BLOCK_ITEM_SWITCH = 16;
		public final int ARM_ANIMATION = 18;
		public final int ENTITY_ACTION = 19;
		public final int CLOSE_WINDOW = 101;
		public final int WINDOW_CLICK = 102;
		public final int TRANSACTION = 106;
		public final int SET_CREATIVE_SLOT = 107;
		public final int BUTTON_CLICK = 108;
		public final int UPDATE_SIGN = 130;
		public final int ABILITIES = 202;
		public final int TAB_COMPLETE = 203;
		public final int LOCALE_AND_VIEW_DISTANCE = 204;
		public final int CLIENT_COMMAND = 205;
		public final int CUSTOM_PAYLOAD = 250;
		public final int KEY_RESPONSE = 252;
		public final int GET_INFO = 254;
		public final int KICK_DISCONNECT = 255;
		
		// Like above
		private Client() {
			super();
		}
	}

	/**
	 * Find a packet by name. Must be capitalized and use underscores.
	 * @param name - name of packet to find.
	 * @return The packet ID found.
	 */
	public static int valueOf(String name) {
		Integer serverAttempt = SERVER.valueOf(name);
		
		if (serverAttempt != null)
			return serverAttempt;
		else
			return CLIENT.valueOf(name);
	}
	
	/**
	 * Retrieves the name of a packet.
	 * @param packetID - packet to retrieve name.
	 * @return The name, or NULL if unable to find such a packet.
	 */
	public static String getDeclaredName(int packetID) {
		String serverAttempt = SERVER.getDeclaredName(packetID);
		
		if (serverAttempt != null)
			return serverAttempt;
		else
			return CLIENT.getDeclaredName(packetID);
	}
}
