package com.comphenix.protocol;

import java.io.Serializable;
import com.comphenix.protocol.reflect.ObjectEnum;
import com.google.common.base.Objects;

/**
 * Represents the type of a packet in a specific protocol.
 * <p>
 * Note that vanilla Minecraft reuses packet IDs per protocol (ping, game, login), so you cannot 
 * rely on IDs alone.
 * @author Kristian
 */
public class PacketType implements Serializable {
	// Increment whenever the type changes
	private static final long serialVersionUID = 1L;

	public static class Handshake {
		public static final Protocol PROTOCOL = Protocol.HANDSHAKE;
		
		public static class Client extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.CLIENT;
			public final static Client INSTANCE = new Client();
			
			public static final PacketType HANDSHAKE = new PacketType(PROTOCOL, SENDER, 0x00, 2);
		}
	}
	
	public static class Game {
		public static final Protocol PROTOCOL = Protocol.GAME;
		
		public static class Server extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.SERVER;
			public final static Server INSTANCE = new Server();
			
			public static final PacketType KEEP_ALIVE = 		 	  new PacketType(PROTOCOL, SENDER, 0x00, 0);
			public static final PacketType LOGIN = 					  new PacketType(PROTOCOL, SENDER, 0x01, 1);
			public static final PacketType CHAT = 					  new PacketType(PROTOCOL, SENDER, 0x02, 3);
			public static final PacketType UPDATE_TIME = 			  new PacketType(PROTOCOL, SENDER, 0x03, 4);
			public static final PacketType ENTITY_EQUIPMENT = 		  new PacketType(PROTOCOL, SENDER, 0x04, 5);
			public static final PacketType SPAWN_POSITION = 		  new PacketType(PROTOCOL, SENDER, 0x05, 6);
			public static final PacketType UPDATE_HEALTH = 			  new PacketType(PROTOCOL, SENDER, 0x06, 8);
			public static final PacketType RESPAWN = 				  new PacketType(PROTOCOL, SENDER, 0x07, 9);
			public static final PacketType PLAYER_LOOK_MOVE =   	  new PacketType(PROTOCOL, SENDER, 0x08, 13);
			public static final PacketType BLOCK_ITEM_SWITCH =  	  new PacketType(PROTOCOL, SENDER, 0x09, 16);
			public static final PacketType ENTITY_LOCATION_ACTION =   new PacketType(PROTOCOL, SENDER, 0x0A, 17);
			public static final PacketType ARM_ANIMATION = 			  new PacketType(PROTOCOL, SENDER, 0x0B, 18);
			public static final PacketType NAMED_ENTITY_SPAWN = 	  new PacketType(PROTOCOL, SENDER, 0x0C, 20);
			public static final PacketType COLLECT = 				  new PacketType(PROTOCOL, SENDER, 0x0D, 22);
			public static final PacketType VEHICLE_SPAWN = 			  new PacketType(PROTOCOL, SENDER, 0x0E, 23);
			public static final PacketType MOB_SPAWN = 				  new PacketType(PROTOCOL, SENDER, 0x0F, 24);
			public static final PacketType ENTITY_PAINTING = 		  new PacketType(PROTOCOL, SENDER, 0x10, 25);
			public static final PacketType ADD_EXP_ORB = 			  new PacketType(PROTOCOL, SENDER, 0x11, 26);
			public static final PacketType ENTITY_VELOCITY = 		  new PacketType(PROTOCOL, SENDER, 0x12, 28);
			public static final PacketType DESTROY_ENTITY = 		  new PacketType(PROTOCOL, SENDER, 0x13, 29);
			public static final PacketType ENTITY = 				  new PacketType(PROTOCOL, SENDER, 0x14, 30);
			public static final PacketType REL_ENTITY_MOVE = 		  new PacketType(PROTOCOL, SENDER, 0x15, 31);
			public static final PacketType ENTITY_LOOK = 			  new PacketType(PROTOCOL, SENDER, 0x16, 32);
			public static final PacketType ENTITY_MOVE_LOOK =		  new PacketType(PROTOCOL, SENDER, 0x17, 33);
			public static final PacketType ENTITY_TELEPORT =		  new PacketType(PROTOCOL, SENDER, 0x18, 34);
			public static final PacketType ENTITY_HEAD_ROTATION =	  new PacketType(PROTOCOL, SENDER, 0x19, 35);
			public static final PacketType ENTITY_STATUS = 			  new PacketType(PROTOCOL, SENDER, 0x1A, 38);
			public static final PacketType ATTACH_ENTITY = 			  new PacketType(PROTOCOL, SENDER, 0x1B, 39);
			public static final PacketType ENTITY_METADATA = 		  new PacketType(PROTOCOL, SENDER, 0x1C, 40);
			public static final PacketType MOB_EFFECT = 			  new PacketType(PROTOCOL, SENDER, 0x1D, 41);
			public static final PacketType REMOVE_MOB_EFFECT =		  new PacketType(PROTOCOL, SENDER, 0x1E, 42);
			public static final PacketType SET_EXPERIENCE = 		  new PacketType(PROTOCOL, SENDER, 0x1F, 43);
			public static final PacketType UPDATE_ATTRIBUTES = 		  new PacketType(PROTOCOL, SENDER, 0x20, 44);
			public static final PacketType MAP_CHUNK = 				  new PacketType(PROTOCOL, SENDER, 0x21, 51);
			public static final PacketType MULTI_BLOCK_CHANGE = 	  new PacketType(PROTOCOL, SENDER, 0x22, 52);
			public static final PacketType BLOCK_CHANGE = 			  new PacketType(PROTOCOL, SENDER, 0x23, 53);
			public static final PacketType PLAY_NOTE_BLOCK = 		  new PacketType(PROTOCOL, SENDER, 0x24, 54);
			public static final PacketType BLOCK_BREAK_ANIMATION =    new PacketType(PROTOCOL, SENDER, 0x25, 55);
			public static final PacketType MAP_CHUNK_BULK = 		  new PacketType(PROTOCOL, SENDER, 0x26, 56);
			public static final PacketType EXPLOSION =				  new PacketType(PROTOCOL, SENDER, 0x27, 60);
			public static final PacketType WORLD_EVENT = 			  new PacketType(PROTOCOL, SENDER, 0x28, 61);
			public static final PacketType NAMED_SOUND_EFFECT = 	  new PacketType(PROTOCOL, SENDER, 0x29, 62);
			public static final PacketType WORLD_PARTICLES = 		  new PacketType(PROTOCOL, SENDER, 0x2A, 63);
			public static final PacketType BED = 			 		  new PacketType(PROTOCOL, SENDER, 0x2B, 70);
			public static final PacketType WEATHER = 				  new PacketType(PROTOCOL, SENDER, 0x2C, 71);
			public static final PacketType OPEN_WINDOW = 			  new PacketType(PROTOCOL, SENDER, 0x2D, 100);
			public static final PacketType CLOSE_WINDOW = 			  new PacketType(PROTOCOL, SENDER, 0x2E, 101);
			public static final PacketType SET_SLOT = 				  new PacketType(PROTOCOL, SENDER, 0x2F, 103);
			public static final PacketType WINDOW_ITEMS = 			  new PacketType(PROTOCOL, SENDER, 0x30, 104);
			public static final PacketType CRAFT_PROGRESS_BAR = 	  new PacketType(PROTOCOL, SENDER, 0x31, 105);
			public static final PacketType TRANSACTION = 			  new PacketType(PROTOCOL, SENDER, 0x32, 106);
			public static final PacketType UPDATE_SIGN = 			  new PacketType(PROTOCOL, SENDER, 0x33, 130);
			public static final PacketType ITEM_DATA = 				  new PacketType(PROTOCOL, SENDER, 0x34, 131);
			public static final PacketType TILE_ENTITY_DATA = 		  new PacketType(PROTOCOL, SENDER, 0x35, 132);
			public static final PacketType OPEN_TILE_ENTITY = 		  new PacketType(PROTOCOL, SENDER, 0x36, 133);
			public static final PacketType STATISTICS = 			  new PacketType(PROTOCOL, SENDER, 0x37, 200);
			public static final PacketType PLAYER_INFO = 			  new PacketType(PROTOCOL, SENDER, 0x38, 201);
			public static final PacketType ABILITIES = 				  new PacketType(PROTOCOL, SENDER, 0x39, 202);
			public static final PacketType TAB_COMPLETE = 			  new PacketType(PROTOCOL, SENDER, 0x3A, 203);
			public static final PacketType SET_SCOREBOARD_OBJECTIVE = new PacketType(PROTOCOL, SENDER, 0x3B, 206);
			public static final PacketType SET_SCOREBOARD_SCORE =     new PacketType(PROTOCOL, SENDER, 0x3C, 207);
			public static final PacketType SET_SCOREBOARD_DISPLAY_OBJECTIVE = 
																	  new PacketType(PROTOCOL, SENDER, 0x3D, 208);
			public static final PacketType SET_SCOREOARD_TEAM =       new PacketType(PROTOCOL, SENDER, 0x3E, 209);
			public static final PacketType CUSTOM_PAYLOAD =           new PacketType(PROTOCOL, SENDER, 0x3F, 250);
			public static final PacketType KICK_DISCONNECT =          new PacketType(PROTOCOL, SENDER, 0x40, 255);
		}
		
		public static class Client extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.CLIENT;
			public final static Client INSTANCE = new Client();
			
			public static final PacketType KEEP_ALIVE =          	  new PacketType(PROTOCOL, SENDER, 0x00, 0);
			public static final PacketType CHAT =          			  new PacketType(PROTOCOL, SENDER, 0x01, 3);
			public static final PacketType USE_ENTITY =          	  new PacketType(PROTOCOL, SENDER, 0x02, 7);
			public static final PacketType FLYING =          		  new PacketType(PROTOCOL, SENDER, 0x03, 10);
			public static final PacketType PLAYER_POSITION =          new PacketType(PROTOCOL, SENDER, 0x04, 11);
			public static final PacketType PLAYER_LOOK =          	  new PacketType(PROTOCOL, SENDER, 0x05, 12);
			public static final PacketType PLAYER_LOOK_MOVE =         new PacketType(PROTOCOL, SENDER, 0x06, 13);
			public static final PacketType BLOCK_DIG =          	  new PacketType(PROTOCOL, SENDER, 0x07, 14);
			public static final PacketType PLACE =          		  new PacketType(PROTOCOL, SENDER, 0x08, 15);
			public static final PacketType BLOCK_ITEM_SWITCH =        new PacketType(PROTOCOL, SENDER, 0x09, 16);
			public static final PacketType ARM_ANIMATION =            new PacketType(PROTOCOL, SENDER, 0x0A, 18);
			public static final PacketType ENTITY_ACTION =            new PacketType(PROTOCOL, SENDER, 0x0B, 19);
			public static final PacketType PLAYER_INPUT =          	  new PacketType(PROTOCOL, SENDER, 0x0C, 27);
			public static final PacketType CLOSE_WINDOW =          	  new PacketType(PROTOCOL, SENDER, 0x0D, 101);
			public static final PacketType WINDOW_CLICK =          	  new PacketType(PROTOCOL, SENDER, 0x0E, 102);
			public static final PacketType TRANSACTION =          	  new PacketType(PROTOCOL, SENDER, 0x0F, 106);
			public static final PacketType CREATIVE_SLOT =            new PacketType(PROTOCOL, SENDER, 0x10, 107);
			public static final PacketType BUTTON_CLICK =          	  new PacketType(PROTOCOL, SENDER, 0x11, 108);
			public static final PacketType UPDATE_SIGN =          	  new PacketType(PROTOCOL, SENDER, 0x12, 130);
			public static final PacketType ABILITIES =         		  new PacketType(PROTOCOL, SENDER, 0x13, 202);
			public static final PacketType TAB_COMPLETE =             new PacketType(PROTOCOL, SENDER, 0x14, 203);
			public static final PacketType LOCALE_AND_VIEW_DISTANCE = new PacketType(PROTOCOL, SENDER, 0x15, 204);
			public static final PacketType CLIENT_COMMAND =           new PacketType(PROTOCOL, SENDER, 0x16, 205);
			public static final PacketType CUSTOM_PAYLOAD =           new PacketType(PROTOCOL, SENDER, 0x17, 250);
		}
	}
	
	public static class Status {
		public static final Protocol PROTOCOL = Protocol.STATUS;
		
		public static class Server extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.SERVER;
			public final static Server INSTANCE = new Server();
			
			public static final PacketType KICK_DISCONNECT =   new PacketType(PROTOCOL, SENDER, 0x00, 255);
			@SuppressWarnings("deprecation")
			public static final PacketType PING_TIME =         new PacketType(PROTOCOL, SENDER, 0x00, Packets.Server.PING_TIME);
		}
		
		public static class Client extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.CLIENT;
			public final static Client INSTANCE = new Client();
			
			public static final PacketType STATUS_REQUEST =    new PacketType(PROTOCOL, SENDER, 0x00, 254);
			@SuppressWarnings("deprecation")
			public static final PacketType PING_TIME =         new PacketType(PROTOCOL, SENDER, 0x00, Packets.Client.PING_TIME);
		}
	}
	
	public static class Login {
		public static final Protocol PROTOCOL = Protocol.LOGIN;
		
		public static class Server extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.SERVER;
			public final static Server INSTANCE = new Server();
			
			public static final PacketType KICK_DISCONNECT =   new PacketType(PROTOCOL, SENDER, 0x00, 255);
			public static final PacketType KEY_REQUEST =       new PacketType(PROTOCOL, SENDER, 0x01, 253);
			@SuppressWarnings("deprecation")
			public static final PacketType LOGIN_SUCCESS =     new PacketType(PROTOCOL, SENDER, 0x02, Packets.Server.LOGIN_SUCCESS);
		}
		
		public static class Client extends ObjectEnum<PacketType> {
			public final static Sender SENDER = Sender.CLIENT;
			public final static Client INSTANCE = new Client();
			
			@SuppressWarnings("deprecation")
			public static final PacketType LOGIN_START =     new PacketType(PROTOCOL, SENDER, 0x00, Packets.Client.LOGIN_START);
			public static final PacketType KEY_RESPONSE =      new PacketType(PROTOCOL, SENDER, 0x01, 252);
		}
	}
	
	/**
	 * Represents the different protocol or connection states.
	 * @author Kristian
	 */
	public enum Protocol {
		HANDSHAKE,
		GAME,
		STATUS,
		LOGIN
	}
	
	/**
	 * Represens the sender of this packet type.
	 * @author Kristian
	 *
	 */
	public enum Sender {
		/**
		 * Indicates that packets of this type will be sent by connected clients.
		 */
		CLIENT,
		
		/**
		 * Indicate that packets of this type will be sent by the current server.
		 */
		SERVER
	}
	
	private final Protocol protocol;
	private final Sender sender;
	private final int currentId;
	private final int legacyId;
	
	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param target - the target - client or server.
	 * @param currentId - the current packet ID.
	 * @param legacyId - the legacy packet ID.
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, int legacyId) {
		this.protocol = protocol;
		this.sender = sender;
		this.currentId = currentId;
		this.legacyId = legacyId;
	}

	/**
	 * Retrieve the protocol (the connection state) the packet type belongs.
	 * @return The protocol of this type.
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Retrieve which sender will transmit packets of this type.
	 * @return The sender of these packets.
	 */
	public Sender getSender() {
		return sender;
	}
	
	/**
	 * Retrieve the current protocol ID for this packet type.
	 * <p>
	 * This is only unique within a specific protocol and target.
	 * @return The current ID.
	 */
	public int getCurrentId() {
		return currentId;
	}
	
	/**
	 * Retrieve the legacy (pre 1.7.2) protocol ID of the packet type.
	 * <p>
	 * This ID is globally unique.
	 * @return The legacy ID.
	 */
	public int getLegacyId() {
		return legacyId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(protocol, sender, legacyId, currentId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof PacketType) {
			PacketType other = (PacketType) obj;
			return protocol == other.protocol && 
				   sender == other.sender && 
				   currentId == other.currentId;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Packet [protocol=" + protocol + ", sender=" + sender + ", legacyId=" + legacyId + ", currentId=" + currentId + "]";
	}
}
