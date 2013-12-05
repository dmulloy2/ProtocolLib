package com.comphenix.protocol;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;

import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.ObjectEnum;
import com.comphenix.protocol.utility.MinecraftVersion;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;

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

	/**
	 * Represents an unknown legacy packet ID.
	 */
	public static final int UNKNOWN_PACKET = -1;
	
	/**
	 * Packets sent during handshake.
	 * @author Kristian
	 */
	public static class Handshake {
		private static final Protocol PROTOCOL = Protocol.HANDSHAKING;
		
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;
			/**
			 * Legacy name: HANDSHAKE.
			 */
			public static final PacketType SET_PROTOCOL = new PacketType(PROTOCOL, SENDER, 0x00, 2);
			
			private final static Client INSTANCE = new Client();
			
			// Prevent accidental construction
			private Client() { super(PacketType.class); }
			
			public static Client getInstance() {
				return INSTANCE;
			}
			public static Sender getSender() {
				return SENDER;
			}
		}
		
		/**
		 * An empty enum, as the server will not send any packets in this protocol.
		 * @author Kristian
		 */
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;
			private final static Server INSTANCE = new Server();
			private Server() { super(PacketType.class); }

			public static Server getInstance() {
				return INSTANCE;
			}
			public static Sender getSender() {
				return SENDER;
			}
		}
		
		public static Protocol getProtocol() {
			return PROTOCOL;
		}
	}
	
	/**
	 * Packets sent and received when logged into the game.
	 * @author Kristian
	 */
	public static class Play {
		private static final Protocol PROTOCOL = Protocol.GAME;
		
		public static class Server extends ObjectEnum<PacketType> {		
			private final static Sender SENDER = Sender.SERVER;
			
			public static final PacketType KEEP_ALIVE = 		 	  new PacketType(PROTOCOL, SENDER, 0x00, 0);
			public static final PacketType LOGIN = 					  new PacketType(PROTOCOL, SENDER, 0x01, 1);
			public static final PacketType CHAT = 					  new PacketType(PROTOCOL, SENDER, 0x02, 3);
			public static final PacketType UPDATE_TIME = 			  new PacketType(PROTOCOL, SENDER, 0x03, 4);
			public static final PacketType ENTITY_EQUIPMENT = 		  new PacketType(PROTOCOL, SENDER, 0x04, 5);
			public static final PacketType SPAWN_POSITION = 		  new PacketType(PROTOCOL, SENDER, 0x05, 6);
			public static final PacketType UPDATE_HEALTH = 			  new PacketType(PROTOCOL, SENDER, 0x06, 8);
			public static final PacketType RESPAWN = 				  new PacketType(PROTOCOL, SENDER, 0x07, 9);
			public static final PacketType POSITION =   	  		  new PacketType(PROTOCOL, SENDER, 0x08, 13);
			public static final PacketType HELD_ITEM_SLOT =		  	  new PacketType(PROTOCOL, SENDER, 0x09, 16);
			/**
			 * Note that this was Packets.Server.ENTITY_LOCATION_ACTION.
			 */
			public static final PacketType BED =   					  new PacketType(PROTOCOL, SENDER, 0x0A, 17);
			public static final PacketType ANIMATION = 			 	  new PacketType(PROTOCOL, SENDER, 0x0B, 18);
			public static final PacketType NAMED_ENTITY_SPAWN = 	  new PacketType(PROTOCOL, SENDER, 0x0C, 20);
			public static final PacketType COLLECT = 				  new PacketType(PROTOCOL, SENDER, 0x0D, 22);
			public static final PacketType SPAWN_ENTITY = 			  new PacketType(PROTOCOL, SENDER, 0x0E, 23);
			public static final PacketType SPAWN_ENTITY_LIVING = 	  new PacketType(PROTOCOL, SENDER, 0x0F, 24);
			public static final PacketType SPAWN_ENTITY_PAITING =	  new PacketType(PROTOCOL, SENDER, 0x10, 25);
			public static final PacketType SPAWN_ENTITY_EXPERIENCE_ORB = new PacketType(PROTOCOL, SENDER, 0x11, 26);
			public static final PacketType ENTITY_VELOCITY = 		  new PacketType(PROTOCOL, SENDER, 0x12, 28);
			public static final PacketType ENTITY_DESTROY = 		  new PacketType(PROTOCOL, SENDER, 0x13, 29);
			public static final PacketType ENTITY = 				  new PacketType(PROTOCOL, SENDER, 0x14, 30);
			public static final PacketType REL_ENTITY_MOVE = 		  new PacketType(PROTOCOL, SENDER, 0x15, 31);
			public static final PacketType ENTITY_LOOK = 			  new PacketType(PROTOCOL, SENDER, 0x16, 32);
			public static final PacketType ENTITY_MOVE_LOOK =		  new PacketType(PROTOCOL, SENDER, 0x17, 33);
			public static final PacketType ENTITY_TELEPORT =		  new PacketType(PROTOCOL, SENDER, 0x18, 34);
			public static final PacketType ENTITY_HEAD_ROTATION =	  new PacketType(PROTOCOL, SENDER, 0x19, 35);
			public static final PacketType ENTITY_STATUS = 			  new PacketType(PROTOCOL, SENDER, 0x1A, 38);
			public static final PacketType ATTACH_ENTITY = 			  new PacketType(PROTOCOL, SENDER, 0x1B, 39);
			public static final PacketType ENTITY_METADATA = 		  new PacketType(PROTOCOL, SENDER, 0x1C, 40);
			public static final PacketType ENTITY_EFFECT = 			  new PacketType(PROTOCOL, SENDER, 0x1D, 41);
			public static final PacketType REMOVE_ENTITY_EFFECT =	  new PacketType(PROTOCOL, SENDER, 0x1E, 42);
			public static final PacketType EXPERIENCE = 		 	  new PacketType(PROTOCOL, SENDER, 0x1F, 43);
			public static final PacketType UPDATE_ATTRIBUTES = 		  new PacketType(PROTOCOL, SENDER, 0x20, 44);
			public static final PacketType MAP_CHUNK = 				  new PacketType(PROTOCOL, SENDER, 0x21, 51);
			public static final PacketType MULTI_BLOCK_CHANGE = 	  new PacketType(PROTOCOL, SENDER, 0x22, 52);
			public static final PacketType BLOCK_CHANGE = 			  new PacketType(PROTOCOL, SENDER, 0x23, 53);
			public static final PacketType BLOCK_ACTION = 		  	  new PacketType(PROTOCOL, SENDER, 0x24, 54);
			public static final PacketType BLOCK_BREAK_ANIMATION =    new PacketType(PROTOCOL, SENDER, 0x25, 55);
			public static final PacketType MAP_CHUNK_BULK = 		  new PacketType(PROTOCOL, SENDER, 0x26, 56);
			public static final PacketType EXPLOSION =				  new PacketType(PROTOCOL, SENDER, 0x27, 60);
			public static final PacketType WORLD_EVENT = 			  new PacketType(PROTOCOL, SENDER, 0x28, 61);
			public static final PacketType NAMED_SOUND_EFFECT = 	  new PacketType(PROTOCOL, SENDER, 0x29, 62);
			public static final PacketType WORLD_PARTICLES = 		  new PacketType(PROTOCOL, SENDER, 0x2A, 63);
			/**
			 * Note that this was Packets.Server.BED.
			 */
			public static final PacketType GAME_STATE_CHANGE = 		  new PacketType(PROTOCOL, SENDER, 0x2B, 70);
			public static final PacketType SPAWN_ENTITY_WEATHER =     new PacketType(PROTOCOL, SENDER, 0x2C, 71);
			public static final PacketType OPEN_WINDOW = 			  new PacketType(PROTOCOL, SENDER, 0x2D, 100);
			public static final PacketType CLOSE_WINDOW = 			  new PacketType(PROTOCOL, SENDER, 0x2E, 101);
			public static final PacketType SET_SLOT = 				  new PacketType(PROTOCOL, SENDER, 0x2F, 103);
			public static final PacketType WINDOW_ITEMS = 			  new PacketType(PROTOCOL, SENDER, 0x30, 104);
			public static final PacketType CRAFT_PROGRESS_BAR = 	  new PacketType(PROTOCOL, SENDER, 0x31, 105);
			public static final PacketType TRANSACTION = 			  new PacketType(PROTOCOL, SENDER, 0x32, 106);
			public static final PacketType UPDATE_SIGN = 			  new PacketType(PROTOCOL, SENDER, 0x33, 130);
			public static final PacketType MAP = 				  	  new PacketType(PROTOCOL, SENDER, 0x34, 131);
			public static final PacketType TILE_ENTITY_DATA = 		  new PacketType(PROTOCOL, SENDER, 0x35, 132);
			public static final PacketType OPEN_SIGN_ENTITY = 		  new PacketType(PROTOCOL, SENDER, 0x36, 133);
			public static final PacketType STATISTICS = 			  new PacketType(PROTOCOL, SENDER, 0x37, 200);
			public static final PacketType PLAYER_INFO = 			  new PacketType(PROTOCOL, SENDER, 0x38, 201);
			public static final PacketType ABILITIES = 				  new PacketType(PROTOCOL, SENDER, 0x39, 202);
			public static final PacketType TAB_COMPLETE = 			  new PacketType(PROTOCOL, SENDER, 0x3A, 203);
			public static final PacketType SCOREBOARD_OBJECTIVE = 	  new PacketType(PROTOCOL, SENDER, 0x3B, 206);
			public static final PacketType SCOREBOARD_SCORE =     	  new PacketType(PROTOCOL, SENDER, 0x3C, 207);
			public static final PacketType SCOREBOARD_DISPLAY_OBJECTIVE = 
																	  new PacketType(PROTOCOL, SENDER, 0x3D, 208);
			public static final PacketType SCOREOARD_TEAM =           new PacketType(PROTOCOL, SENDER, 0x3E, 209);
			public static final PacketType CUSTOM_PAYLOAD =           new PacketType(PROTOCOL, SENDER, 0x3F, 250);
			public static final PacketType KICK_DISCONNECT =          new PacketType(PROTOCOL, SENDER, 0x40, 255);
			
			// The instance must 
			private final static Server INSTANCE = new Server();
			
			// Prevent accidental construction
			private Server() { super(PacketType.class); }
			
			public static Sender getSender() {
				return SENDER;
			}
			public static Server getInstance() {
				return INSTANCE;
			}
		}
		
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;
			
			public static final PacketType KEEP_ALIVE =          	  new PacketType(PROTOCOL, SENDER, 0x00, 0);
			public static final PacketType CHAT =          			  new PacketType(PROTOCOL, SENDER, 0x01, 3);
			public static final PacketType USE_ENTITY =          	  new PacketType(PROTOCOL, SENDER, 0x02, 7);
			public static final PacketType FLYING =          		  new PacketType(PROTOCOL, SENDER, 0x03, 10);
			public static final PacketType POSITION =          		  new PacketType(PROTOCOL, SENDER, 0x04, 11);
			public static final PacketType LOOK =          	  		  new PacketType(PROTOCOL, SENDER, 0x05, 12);
			public static final PacketType POSITION_LOOK =         	  new PacketType(PROTOCOL, SENDER, 0x06, 13);
			public static final PacketType BLOCK_DIG =          	  new PacketType(PROTOCOL, SENDER, 0x07, 14);
			public static final PacketType BLOCK_PLACE =          	  new PacketType(PROTOCOL, SENDER, 0x08, 15);
			public static final PacketType HELD_ITEM_SLOT =        	  new PacketType(PROTOCOL, SENDER, 0x09, 16);
			public static final PacketType ARM_ANIMATION =            new PacketType(PROTOCOL, SENDER, 0x0A, 18);
			public static final PacketType ENTITY_ACTION =            new PacketType(PROTOCOL, SENDER, 0x0B, 19);
			public static final PacketType STEER_VEHICLE =           new PacketType(PROTOCOL, SENDER, 0x0C, 27);
			public static final PacketType CLOSE_WINDOW =          	  new PacketType(PROTOCOL, SENDER, 0x0D, 101);
			public static final PacketType WINDOW_CLICK =          	  new PacketType(PROTOCOL, SENDER, 0x0E, 102);
			public static final PacketType TRANSACTION =          	  new PacketType(PROTOCOL, SENDER, 0x0F, 106);
			public static final PacketType SET_CREATIVE_SLOT =        new PacketType(PROTOCOL, SENDER, 0x10, 107);
			public static final PacketType ENCHANT_ITEM =          	  new PacketType(PROTOCOL, SENDER, 0x11, 108);
			public static final PacketType UPDATE_SIGN =          	  new PacketType(PROTOCOL, SENDER, 0x12, 130);
			public static final PacketType ABILITIES =         		  new PacketType(PROTOCOL, SENDER, 0x13, 202);
			public static final PacketType TAB_COMPLETE =             new PacketType(PROTOCOL, SENDER, 0x14, 203);
			public static final PacketType SETTINGS = new PacketType(PROTOCOL, SENDER, 0x15, 204);
			public static final PacketType CLIENT_COMMAND =           new PacketType(PROTOCOL, SENDER, 0x16, 205);
			public static final PacketType CUSTOM_PAYLOAD =           new PacketType(PROTOCOL, SENDER, 0x17, 250);
			
			private final static Client INSTANCE = new Client();
			
			// Prevent accidental construction
			private Client() { super(PacketType.class); }
			
			public static Sender getSender() {
				return SENDER;
			}
			public static Client getInstance() {
				return INSTANCE;
			}
		}
		
		public static Protocol getProtocol() {
			return PROTOCOL;
		}
	}
	
	/**
	 * Packets sent and received when querying the server in the multiplayer menu.
	 * @author Kristian
	 */
	public static class Status {
		private static final Protocol PROTOCOL = Protocol.STATUS;
		
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.SERVER;
			
			public static final PacketType OUT_SERVER_INFO =   new PacketType(PROTOCOL, SENDER, 0x00, 255);
			@SuppressWarnings("deprecation")
			public static final PacketType OUT_PING =          new PacketType(PROTOCOL, SENDER, 0x01, Packets.Server.PING_TIME);
			
			private final static Server INSTANCE = new Server();
			
			// Prevent accidental construction
			private Server() { super(PacketType.class); }
			
			public static Sender getSender() {
				return SENDER;
			}
			public static Server getInstance() {
				return INSTANCE;
			}
		}
		
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;		
			
			public static final PacketType IN_START =        new PacketType(PROTOCOL, SENDER, 0x00, 254);
			@SuppressWarnings("deprecation")
			public static final PacketType IN_PING =         new PacketType(PROTOCOL, SENDER, 0x01, Packets.Client.PING_TIME);
			
			private final static Client INSTANCE = new Client();
			
			// Prevent accidental construction
			private Client() { super(PacketType.class); }
			
			public static Sender getSender() {
				return SENDER;
			}
			public static Client getInstance() {
				return INSTANCE;
			}
		}
		
		public static Protocol getProtocol() {
			return PROTOCOL;
		}
	}
	
	/**
	 * Packets sent and received when logging in to the server.
	 * @author Kristian
	 */
	public static class Login {
		private static final Protocol PROTOCOL = Protocol.LOGIN;
		
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.SERVER;
			
			public static final PacketType DISCONNECT =   		  new PacketType(PROTOCOL, SENDER, 0x00, 255);
			public static final PacketType ENCRYPTION_BEGIN =     new PacketType(PROTOCOL, SENDER, 0x01, 253);
			@SuppressWarnings("deprecation")
			public static final PacketType SUCCESS =     		  new PacketType(PROTOCOL, SENDER, 0x02, Packets.Server.LOGIN_SUCCESS);

			private final static Server INSTANCE = new Server();
			
			// Prevent accidental construction
			private Server() { super(PacketType.class); }
			
			public static Sender getSender() {
				return SENDER;
			}
			public static Server getInstance() {
				return INSTANCE;
			}
		}
		
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;
			
			@SuppressWarnings("deprecation")
			public static final PacketType START =       		   new PacketType(PROTOCOL, SENDER, 0x00, Packets.Client.LOGIN_START);
			public static final PacketType ENCRYPTION_BEGIN =      new PacketType(PROTOCOL, SENDER, 0x01, 252);

			private final static Client INSTANCE = new Client();
			
			// Prevent accidental construction
			private Client() { super(PacketType.class); }
			
			public static Sender getSender() {
				return SENDER;
			}
			public static Client getInstance() {
				return INSTANCE;
			}
		}
		
		public static Protocol getProtocol() {
			return PROTOCOL;
		}
	}
	
	/**
	 * Represents the different protocol or connection states.
	 * @author Kristian
	 */
	public enum Protocol {
		HANDSHAKING,
		GAME,
		STATUS,
		LOGIN;
		
		/**
		 * Retrieve the correct protocol enum from a given vanilla enum instance.
		 * @param vanilla - the vanilla protocol enum instance.
		 * @return The corresponding protocol.
		 */
		public static Protocol fromVanilla(Enum<?> vanilla) {
			String name = vanilla.name();
			
			if ("HANDSHAKING".equals(name))
					return HANDSHAKING;
			if ("PLAY".equals(name))
				return GAME;
			if ("STATUS".equals(name))
				return STATUS;
			if ("LOGIN".equals(name))
				return LOGIN;
			throw new IllegalArgumentException("Unrecognized vanilla enum " + vanilla);
		}
	}
	
	/**
	 * Represents the sender of this packet type.
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
	
	// Lookup of packet types
	private static PacketTypeLookup LOOKUP;
	
	/**
	 * Protocol version of all the current IDs.
	 */
	private static final MinecraftVersion PROTOCOL_VERSION = MinecraftVersion.WORLD_UPDATE;
	
	private final Protocol protocol;
	private final Sender sender;
	private final int currentId;
	private final int legacyId;
	private final MinecraftVersion version;
	
	/**
	 * Retrieve the current packet/legacy lookup.
	 * @return The packet type lookup.
	 */
	private static PacketTypeLookup getLookup() {
		if (LOOKUP == null) {
			LOOKUP = new PacketTypeLookup().
				addPacketTypes(Handshake.Client.getInstance()).
				addPacketTypes(Handshake.Server.getInstance()).
				addPacketTypes(Play.Client.getInstance()).
				addPacketTypes(Play.Server.getInstance()).
				addPacketTypes(Status.Client.getInstance()).
				addPacketTypes(Status.Server.getInstance()).
				addPacketTypes(Login.Client.getInstance()).
				addPacketTypes(Login.Server.getInstance());
		}
		return LOOKUP;
	}
	
	/**
	 * Find every packet type known to the current version of ProtocolLib.
	 * @return Every packet type.
	 */
	public static Iterable<PacketType> values() {
		List<Iterable<? extends PacketType>> sources = Lists.newArrayList();
		sources.add(Handshake.Client.getInstance());
		sources.add(Handshake.Server.getInstance());
		sources.add(Play.Client.getInstance());
		sources.add(Play.Server.getInstance());
		sources.add(Status.Client.getInstance());
		sources.add(Status.Server.getInstance());
		sources.add(Login.Client.getInstance());
		sources.add(Login.Server.getInstance());
		return Iterables.concat(sources);
	}
	
	/**
	 * Retrieve a packet type from a legacy (1.6.4 and below) packet ID.
	 * @param packetId - the legacy packet ID.
	 * @return The corresponding packet type.
	 * @throws IllegalArgumentException If the legacy packet could not be found.
	 */
	public static PacketType findLegacy(int packetId) {
		PacketType type = getLookup().getFromLegacy(packetId);
		
		if (type != null)
			return type;
		throw new IllegalArgumentException("Cannot find legacy packet " + packetId);
	}
	
	/**
	 * Retrieve a packet type from a protocol, sender and packet ID.
	 * @param protocol - the current protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID.
	 * @return The corresponding packet type.
	 * @throws IllegalArgumentException If the current packet could not be found.
	 */
	public static PacketType findCurrent(Protocol protocol, Sender sender, int packetId) {
		PacketType type = getLookup().getFromCurrent(protocol, sender, packetId);
		
		if (type != null)
			return type;
		throw new IllegalArgumentException("Cannot find packet " + packetId + 
				"(Protocol: " + protocol + ", Sender: " + sender + ")");
	}
	
	/**
	 * Retrieve a packet type from a protocol, sender and packet ID.
	 * <p>
	 * The packet will automatically be registered if its missing.
	 * @param protocol - the current protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID.
	 * @param legacyId - the legacy packet ID. Can be UNKNOWN_PACKET.
	 * @return The corresponding packet type.
	 */
	public static PacketType fromCurrent(Protocol protocol, Sender sender, int packetId, int legacyId) {
		PacketType type = getLookup().getFromCurrent(protocol, sender, packetId);
		
		if (type == null) {
			type = new PacketType(protocol, sender, packetId, legacyId);
			
			// Many may be scheduled, but only the first will be executed
			scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
		}
		return type;
	}
	
	/**
	 * Register a particular packet type.
	 * <p>
	 * Note that the registration will be performed on the main thread. 
	 * @param type - the type to register.
	 * @param name - the name of the packet.
	 * @return A future telling us if our instance was registered.
	 */
	public static Future<Boolean> scheduleRegister(final PacketType type, final String name) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				ObjectEnum<PacketType> objEnum;
				
				// A bit ugly, but performance is critical
				switch (type.getProtocol()) {
					case HANDSHAKING:
						objEnum = type.isClient() ? Handshake.Client.getInstance() : Handshake.Server.getInstance(); break;
					case GAME:
						objEnum = type.isClient() ? Play.Client.getInstance() : Play.Server.getInstance(); break;
					case STATUS:
						objEnum = type.isClient() ? Status.Client.getInstance() : Status.Server.getInstance(); break;
					case LOGIN:
						objEnum = type.isClient() ? Login.Client.getInstance() : Login.Server.getInstance(); break;
					default:
						throw new IllegalStateException("Unexpected protocol: " + type.getProtocol());
				}
				
				if (objEnum.registerMember(type, name)) {
					getLookup().addPacketTypes(Arrays.asList(type));
					return true;
				}
				return false;
			}
		};

		// Execute in the main thread if possible
		if (Bukkit.getServer() == null || Bukkit.isPrimaryThread()) {
			try {
				return Futures.immediateFuture(callable.call());
			} catch (Exception e) {
				return Futures.immediateFailedFuture(e);
			}
		}
		return ProtocolLibrary.getExecutorSync().submit(callable);
	}
	
	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param target - the target - client or server.
	 * @param currentId - the current packet ID, or 
	 * @param legacyId - the legacy packet ID.
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, int legacyId) {
		this(protocol, sender, currentId, legacyId, PROTOCOL_VERSION);
	}

	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param target - the target - client or server.
	 * @param currentId - the current packet ID.
	 * @param legacyId - the legacy packet ID.
	 * @param version - the version of the current ID.
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, int legacyId, MinecraftVersion version) {
		this.protocol = Preconditions.checkNotNull(protocol, "protocol cannot be NULL");
		this.sender = Preconditions.checkNotNull(sender, "sender cannot be NULL");
		this.currentId = currentId;
		this.legacyId = legacyId;
		this.version = version;
	}
	
	/**
	 * Determine if this packet is supported on the current server.
	 * @return Whether or not the packet is supported.
	 */
	public boolean isSupported() {
		return PacketRegistry.isSupported(this);
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
	 * Determine if this packet was sent by the client.
	 * @return TRUE if it was, FALSE otherwise.
	 */
	public boolean isClient() {
		return sender == Sender.CLIENT;
	}
	
	/**
	 * Determine if this packet was sent by the server.
	 * @return TRUE if it was, FALSE otherwise.
	 */
	public boolean isServer() {
		return sender == Sender.SERVER;
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
	 * Retrieve the equivalent packet class.
	 * @return The packet class.
	 */
	public Class<?> getPacketClass() {
		return PacketRegistry.getPacketClassFromType(this);
	}
	
	/**
	 * Retrieve the Minecraft version for the current ID.
	 * @return The Minecraft version.
	 */
	public MinecraftVersion getCurrentVersion() {
		return version;
	}
	
	/**
	 * Retrieve the legacy (1.6.4 or below) protocol ID of the packet type.
	 * <p>
	 * This ID is globally unique.
	 * @return The legacy ID, or {@link #UNKNOWN_PACKET} if unknown.
	 */
	public int getLegacyId() {
		return legacyId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(protocol, sender, currentId);
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
		Class<?> clazz = getPacketClass();
		return (clazz != null ? clazz.getSimpleName() : "UNREGISTERED") + 
			" [" + protocol + ", " + sender + ", " + currentId + ", legacy: " + legacyId + "]";
	}
}
