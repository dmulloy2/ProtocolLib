package com.comphenix.protocol;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;

import com.comphenix.protocol.PacketTypeLookup.ClassLookup;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.ObjectEnum;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;

/**
 * Represents the type of a packet in a specific protocol.
 * <p>
 * Note that vanilla Minecraft reuses packet IDs per protocol (ping, game, login) and IDs are subject to change, so they are not reliable.
 * @author Kristian
 */
public class PacketType implements Serializable, Comparable<PacketType> {
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

		/**
		 * Incoming packets.
		 * @author Kristian
		 */
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType SET_PROTOCOL =                 new PacketType(PROTOCOL, SENDER, 0x00, 0x00, "SetProtocol");

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
		private static final Protocol PROTOCOL = Protocol.PLAY;

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.SERVER;

			public static final PacketType SPAWN_ENTITY =                 new PacketType(PROTOCOL, SENDER, 0x00, 0x0E, "SpawnEntity");
			public static final PacketType SPAWN_ENTITY_EXPERIENCE_ORB =  new PacketType(PROTOCOL, SENDER, 0x01, 0x11, "SpawnEntityExperienceOrb");
			public static final PacketType SPAWN_ENTITY_WEATHER =         new PacketType(PROTOCOL, SENDER, 0x02, 0x2C, "SpawnEntityWeather");
			public static final PacketType SPAWN_ENTITY_LIVING =          new PacketType(PROTOCOL, SENDER, 0x03, 0x0F, "SpawnEntityLiving");
			public static final PacketType SPAWN_ENTITY_PAINTING =        new PacketType(PROTOCOL, SENDER, 0x04, 0x10, "SpawnEntityPainting");
			public static final PacketType NAMED_ENTITY_SPAWN =           new PacketType(PROTOCOL, SENDER, 0x05, 0x0C, "NamedEntitySpawn");
			public static final PacketType ANIMATION =                    new PacketType(PROTOCOL, SENDER, 0x06, 0x0B, "Animation");
			public static final PacketType STATISTIC =                    new PacketType(PROTOCOL, SENDER, 0x07, 0x37, "Statistic");
			public static final PacketType BLOCK_BREAK_ANIMATION =        new PacketType(PROTOCOL, SENDER, 0x08, 0x25, "BlockBreakAnimation");
			public static final PacketType TILE_ENTITY_DATA =             new PacketType(PROTOCOL, SENDER, 0x09, 0x35, "TileEntityData");
			public static final PacketType BLOCK_ACTION =                 new PacketType(PROTOCOL, SENDER, 0x0A, 0x24, "BlockAction");
			public static final PacketType BLOCK_CHANGE =                 new PacketType(PROTOCOL, SENDER, 0x0B, 0x23, "BlockChange");
			public static final PacketType BOSS =                         new PacketType(PROTOCOL, SENDER, 0x0C, 0xFF, "Boss");
			public static final PacketType SERVER_DIFFICULTY =            new PacketType(PROTOCOL, SENDER, 0x0D, 0x41, "ServerDifficulty");
			public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x0E, 0x3A, "TabComplete");
			public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x0F, 0x02, "Chat");
			public static final PacketType MULTI_BLOCK_CHANGE =           new PacketType(PROTOCOL, SENDER, 0x10, 0x22, "MultiBlockChange");
			public static final PacketType TRANSACTION =                  new PacketType(PROTOCOL, SENDER, 0x11, 0x32, "Transaction");
			public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x12, 0x2E, "CloseWindow");
			public static final PacketType OPEN_WINDOW =                  new PacketType(PROTOCOL, SENDER, 0x13, 0x2D, "OpenWindow");
			public static final PacketType WINDOW_ITEMS =                 new PacketType(PROTOCOL, SENDER, 0x14, 0x30, "WindowItems");
			public static final PacketType WINDOW_DATA =                  new PacketType(PROTOCOL, SENDER, 0x15, 0x31, "WindowData");
			public static final PacketType SET_SLOT =                     new PacketType(PROTOCOL, SENDER, 0x16, 0x2F, "SetSlot");
			public static final PacketType SET_COOLDOWN =                 new PacketType(PROTOCOL, SENDER, 0x17, 0xFC, "SetCooldown");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x18, 0x3F, "CustomPayload");
			public static final PacketType CUSTOM_SOUND_EFFECT =          new PacketType(PROTOCOL, SENDER, 0x19, 0xFD, "CustomSoundEffect");
			public static final PacketType KICK_DISCONNECT =              new PacketType(PROTOCOL, SENDER, 0x1A, 0x40, "KickDisconnect");
			public static final PacketType ENTITY_STATUS =                new PacketType(PROTOCOL, SENDER, 0x1B, 0x1A, "EntityStatus");
			public static final PacketType EXPLOSION =                    new PacketType(PROTOCOL, SENDER, 0x1C, 0x27, "Explosion");
			public static final PacketType UNLOAD_CHUNK =                 new PacketType(PROTOCOL, SENDER, 0x1D, 0xFA, "UnloadChunk");
			public static final PacketType GAME_STATE_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x1E, 0x2B, "GameStateChange");
			public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x1F, 0x00, "KeepAlive");
			public static final PacketType MAP_CHUNK =                    new PacketType(PROTOCOL, SENDER, 0x20, 0x21, "MapChunk");
			public static final PacketType WORLD_EVENT =                  new PacketType(PROTOCOL, SENDER, 0x21, 0x28, "WorldEvent");
			public static final PacketType WORLD_PARTICLES =              new PacketType(PROTOCOL, SENDER, 0x22, 0x2A, "WorldParticles");
			public static final PacketType LOGIN =                        new PacketType(PROTOCOL, SENDER, 0x23, 0x01, "Login");
			public static final PacketType MAP =                          new PacketType(PROTOCOL, SENDER, 0x24, 0x34, "Map");
			public static final PacketType REL_ENTITY_MOVE =              new PacketType(PROTOCOL, SENDER, 0x25, 0x15, "RelEntityMove");
			public static final PacketType REL_ENTITY_MOVE_LOOK =         new PacketType(PROTOCOL, SENDER, 0x26, 0x17, "RelEntityMoveLook");
			public static final PacketType ENTITY_LOOK =                  new PacketType(PROTOCOL, SENDER, 0x27, 0x16, "EntityLook");
			public static final PacketType ENTITY =                       new PacketType(PROTOCOL, SENDER, 0x28, 0x14, "Entity");
			public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x29, 0xFE, "VehicleMove");
			public static final PacketType OPEN_SIGN_EDITOR =             new PacketType(PROTOCOL, SENDER, 0x2A, 0x36, "OpenSignEditor");
			public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x2B, 0x39, "Abilities");
			public static final PacketType COMBAT_EVENT =                 new PacketType(PROTOCOL, SENDER, 0x2C, 0x42, "CombatEvent");
			public static final PacketType PLAYER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x2D, 0x38, "PlayerInfo");
			public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x2E, 0x08, "Position");
			public static final PacketType BED =                          new PacketType(PROTOCOL, SENDER, 0x2F, 0x0A, "Bed");
			public static final PacketType ENTITY_DESTROY =               new PacketType(PROTOCOL, SENDER, 0x30, 0x13, "EntityDestroy");
			public static final PacketType REMOVE_ENTITY_EFFECT =         new PacketType(PROTOCOL, SENDER, 0x31, 0x1E, "RemoveEntityEffect");
			public static final PacketType RESOURCE_PACK_SEND =           new PacketType(PROTOCOL, SENDER, 0x32, 0x48, "ResourcePackSend");
			public static final PacketType RESPAWN =                      new PacketType(PROTOCOL, SENDER, 0x33, 0x07, "Respawn");
			public static final PacketType ENTITY_HEAD_ROTATION =         new PacketType(PROTOCOL, SENDER, 0x34, 0x19, "EntityHeadRotation");
			public static final PacketType WORLD_BORDER =                 new PacketType(PROTOCOL, SENDER, 0x35, 0x44, "WorldBorder");
			public static final PacketType CAMERA =                       new PacketType(PROTOCOL, SENDER, 0x36, 0x43, "Camera");
			public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x37, 0x09, "HeldItemSlot");
			public static final PacketType SCOREBOARD_DISPLAY_OBJECTIVE = new PacketType(PROTOCOL, SENDER, 0x38, 0x3D, "ScoreboardDisplayObjective");
			public static final PacketType ENTITY_METADATA =              new PacketType(PROTOCOL, SENDER, 0x39, 0x1C, "EntityMetadata");
			public static final PacketType ATTACH_ENTITY =                new PacketType(PROTOCOL, SENDER, 0x3A, 0x1B, "AttachEntity");
			public static final PacketType ENTITY_VELOCITY =              new PacketType(PROTOCOL, SENDER, 0x3B, 0x12, "EntityVelocity");
			public static final PacketType ENTITY_EQUIPMENT =             new PacketType(PROTOCOL, SENDER, 0x3C, 0x04, "EntityEquipment");
			public static final PacketType EXPERIENCE =                   new PacketType(PROTOCOL, SENDER, 0x3D, 0x1F, "Experience");
			public static final PacketType UPDATE_HEALTH =                new PacketType(PROTOCOL, SENDER, 0x3E, 0x06, "UpdateHealth");
			public static final PacketType SCOREBOARD_OBJECTIVE =         new PacketType(PROTOCOL, SENDER, 0x3F, 0x3B, "ScoreboardObjective");
			public static final PacketType MOUNT =                        new PacketType(PROTOCOL, SENDER, 0x40, 0xFB, "Mount");
			public static final PacketType SCOREBOARD_TEAM =              new PacketType(PROTOCOL, SENDER, 0x41, 0x3E, "ScoreboardTeam");
			public static final PacketType SCOREBOARD_SCORE =             new PacketType(PROTOCOL, SENDER, 0x42, 0x3C, "ScoreboardScore");
			public static final PacketType SPAWN_POSITION =               new PacketType(PROTOCOL, SENDER, 0x43, 0x05, "SpawnPosition");
			public static final PacketType UPDATE_TIME =                  new PacketType(PROTOCOL, SENDER, 0x44, 0x03, "UpdateTime");
			public static final PacketType TITLE =                        new PacketType(PROTOCOL, SENDER, 0x45, 0x45, "Title");
			public static final PacketType NAMED_SOUND_EFFECT =           new PacketType(PROTOCOL, SENDER, 0x47, 0x29, "NamedSoundEffect");
			public static final PacketType PLAYER_LIST_HEADER_FOOTER =    new PacketType(PROTOCOL, SENDER, 0x48, 0x47, "PlayerListHeaderFooter");
			public static final PacketType COLLECT =                      new PacketType(PROTOCOL, SENDER, 0x49, 0x0D, "Collect");
			public static final PacketType ENTITY_TELEPORT =              new PacketType(PROTOCOL, SENDER, 0x4A, 0x18, "EntityTeleport");
			public static final PacketType UPDATE_ATTRIBUTES =            new PacketType(PROTOCOL, SENDER, 0x4B, 0x20, "UpdateAttributes");
			public static final PacketType ENTITY_EFFECT =                new PacketType(PROTOCOL, SENDER, 0x4C, 0x1D, "EntityEffect");

			/**
			 * @deprecated Replaced by {@link WINDOW_DATA}
			 */
			@Deprecated
			public static final PacketType CRAFT_PROGRESS_BAR =           WINDOW_DATA;

			/**
			 * @deprecated Replaced by {@link REL_ENTITY_MOVE_LOOK}
			 */
			@Deprecated
			public static final PacketType ENTITY_MOVE_LOOK =             REL_ENTITY_MOVE_LOOK;

			/**
			 * @deprecated Replaced by {@link STATISTIC}
			 */
			@Deprecated
			public static final PacketType STATISTICS =                   STATISTIC;

			/**
			 * @deprecated Replaced by {@link TILE_ENTITY_DATA}
			 */
			@Deprecated
			public static final PacketType UPDATE_SIGN =                  TILE_ENTITY_DATA;

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

		/**
		 * Incoming packets.
		 * @author Kristian
		 */
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType TELEPORT_ACCEPT =              new PacketType(PROTOCOL, SENDER, 0x00, 0xF9, "TeleportAccept");
			public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x01, 0x14, "TabComplete");
			public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x02, 0x01, "Chat");
			public static final PacketType CLIENT_COMMAND =               new PacketType(PROTOCOL, SENDER, 0x03, 0x16, "ClientCommand");
			public static final PacketType SETTINGS =                     new PacketType(PROTOCOL, SENDER, 0x04, 0x15, "Settings");
			public static final PacketType TRANSACTION =                  new PacketType(PROTOCOL, SENDER, 0x05, 0x0F, "Transaction");
			public static final PacketType ENCHANT_ITEM =                 new PacketType(PROTOCOL, SENDER, 0x06, 0x11, "EnchantItem");
			public static final PacketType WINDOW_CLICK =                 new PacketType(PROTOCOL, SENDER, 0x07, 0x0E, "WindowClick");
			public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x08, 0x0D, "CloseWindow");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x09, 0x17, "CustomPayload");
			public static final PacketType USE_ENTITY =                   new PacketType(PROTOCOL, SENDER, 0x0A, 0x02, "UseEntity");
			public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x0B, 0x00, "KeepAlive");
			public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x0C, 0x04, "Position");
			public static final PacketType POSITION_LOOK =                new PacketType(PROTOCOL, SENDER, 0x0D, 0x06, "PositionLook");
			public static final PacketType LOOK =                         new PacketType(PROTOCOL, SENDER, 0x0E, 0x05, "Look");
			public static final PacketType FLYING =                       new PacketType(PROTOCOL, SENDER, 0x0F, 0x03, "Flying");
			public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x10, 0xF8, "VehicleMove");
			public static final PacketType BOAT_MOVE =                    new PacketType(PROTOCOL, SENDER, 0x11, 0xF7, "BoatMove");
			public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x12, 0x13, "Abilities");
			public static final PacketType BLOCK_DIG =                    new PacketType(PROTOCOL, SENDER, 0x13, 0x07, "BlockDig");
			public static final PacketType ENTITY_ACTION =                new PacketType(PROTOCOL, SENDER, 0x14, 0x0B, "EntityAction");
			public static final PacketType STEER_VEHICLE =                new PacketType(PROTOCOL, SENDER, 0x15, 0x0C, "SteerVehicle");
			public static final PacketType RESOURCE_PACK_STATUS =         new PacketType(PROTOCOL, SENDER, 0x16, 0x19, "ResourcePackStatus");
			public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x17, 0x09, "HeldItemSlot");
			public static final PacketType SET_CREATIVE_SLOT =            new PacketType(PROTOCOL, SENDER, 0x18, 0x10, "SetCreativeSlot");
			public static final PacketType UPDATE_SIGN =                  new PacketType(PROTOCOL, SENDER, 0x19, 0x12, "UpdateSign");
			public static final PacketType ARM_ANIMATION =                new PacketType(PROTOCOL, SENDER, 0x1A, 0x0A, "ArmAnimation");
			public static final PacketType SPECTATE =                     new PacketType(PROTOCOL, SENDER, 0x1B, 0x18, "Spectate");
			public static final PacketType USE_ITEM =                     new PacketType(PROTOCOL, SENDER, 0x1C, 0xF6, "UseItem");
			public static final PacketType BLOCK_PLACE =                  new PacketType(PROTOCOL, SENDER, 0x1D, 0x08, "BlockPlace");

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

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.SERVER;

			public static final PacketType SERVER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x00, 0x00, "ServerInfo").forceAsync(true);
			public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x01, 0x01, "Pong");

			/**
			 * @deprecated Replaced by {@link SERVER_INFO}
			 */
			@Deprecated
			public static final PacketType OUT_SERVER_INFO =              SERVER_INFO;

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

		/**
		 * Incoming packets.
		 * @author Kristian
		 */
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, 0x00, "Start");
			public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x01, 0x01, "Ping");

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

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.SERVER;

			public static final PacketType DISCONNECT =                   new PacketType(PROTOCOL, SENDER, 0x00, 0x00, "Disconnect");
			public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, 0x01, "EncryptionBegin");
			public static final PacketType SUCCESS =                      new PacketType(PROTOCOL, SENDER, 0x02, 0x02, "Success");
			public static final PacketType SET_COMPRESSION =              new PacketType(PROTOCOL, SENDER, 0x03, 0x03, "SetCompression");

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

		/**
		 * Incoming packets.
		 * @author Kristian
		 */
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, 0x00, "Start");
			public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, 0x01, "EncryptionBegin");

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
	 * Contains every packet Minecraft 1.6.4 packet removed in Minecraft 1.7.2.
	 * @author Kristian
	 */
	public static class Legacy {
		private static final Protocol PROTOCOL = Protocol.LEGACY;

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		// Missing server packets: [10, 11, 12, 21, 107, 252]
		public static class Server extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.SERVER;

			public static final PacketType PLAYER_FLYING =            PacketType.newLegacy(SENDER, 10);
			public static final PacketType PLAYER_POSITION =          PacketType.newLegacy(SENDER, 11);
			public static final PacketType PLAYER_POSITON_LOOK =      PacketType.newLegacy(SENDER, 12);
			/**
			 * Removed in Minecraft 1.4.6.
			 */
			public static final PacketType PICKUP_SPAWN =             PacketType.newLegacy(SENDER, 21);
			/**
			 * Removed in Minecraft 1.7.2
			 */
			public static final PacketType SET_CREATIVE_SLOT =        PacketType.newLegacy(SENDER, 107);

			/**
			 * Removed in Minecraft 1.7.2
			 */
			public static final PacketType KEY_RESPONSE =             PacketType.newLegacy(SENDER, 252);

			private final static Server INSTANCE = new Server();

			// Prevent accidental construction
			private Server() {
				super(PacketType.class);
			}

			public static Sender getSender() {
				return SENDER;
			}
			public static Server getInstance() {
				return INSTANCE;
			}
		}

		/**
		 * Incoming packets.
		 * @author Kristian
		 */
		// Missing client packets: [1, 9, 255]
		public static class Client extends ObjectEnum<PacketType> {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType LOGIN =                    PacketType.newLegacy(SENDER, 1);
			public static final PacketType RESPAWN =                  PacketType.newLegacy(SENDER, 9);
			public static final PacketType DISCONNECT =               PacketType.newLegacy(SENDER, 255);

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
		PLAY,
		STATUS,
		LOGIN,

		/**
		 * Only for packets removed in Minecraft 1.7.2
		 */
		LEGACY;

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
				return PLAY;
			if ("STATUS".equals(name))
				return STATUS;
			if ("LOGIN".equals(name))
				return LOGIN;
			throw new IllegalArgumentException("Unrecognized vanilla enum " + vanilla);
		}

		public String getPacketName() {
			return WordUtils.capitalize(name().toLowerCase());
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
		SERVER;

		/**
		 * Retrieve the equivialent connection side.
		 * @return The connection side.
		 */
		public ConnectionSide toSide() {
			return this == CLIENT ? ConnectionSide.CLIENT_SIDE : ConnectionSide.SERVER_SIDE;
		}

		public String getPacketName() {
			return this == CLIENT ? "In" : "Out";
		}
	}

	// Lookup of packet types
	private static PacketTypeLookup LOOKUP;

	/**
	 * Protocol version of all the current IDs.
	 */
	private static final MinecraftVersion PROTOCOL_VERSION = MinecraftVersion.BOUNTIFUL_UPDATE;

	private final Protocol protocol;
	private final Sender sender;
	private final int currentId;
	private final int legacyId;
	private final MinecraftVersion version;
	private final String[] classNames;

	private boolean forceAsync;
	private boolean dynamic;

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
				addPacketTypes(Login.Server.getInstance()).
				addPacketTypes(Legacy.Client.getInstance()).
				addPacketTypes(Legacy.Server.getInstance());
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

		// Add the missing types in earlier versions
		if (!MinecraftReflection.isUsingNetty()) {
			sources.add(Legacy.Client.getInstance());
			sources.add(Legacy.Server.getInstance());
		}
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
	 * Retrieve a packet type from a legacy (1.6.4 and below) packet ID.
	 * @param packetId - the legacy packet ID.
	 * @param preference - the preferred sender, or NULL for any arbitrary sender.
	 * @return The corresponding packet type.
	 * @throws IllegalArgumentException If the legacy packet could not be found.
	 */
	public static PacketType findLegacy(int packetId, Sender preference) {
		if (preference == null)
			return findLegacy(packetId);
		PacketType type = getLookup().getFromLegacy(packetId, preference);

		if (type != null)
			return type;
		throw new IllegalArgumentException("Cannot find legacy packet " + packetId);
	}

	/**
	 * Determine if the given legacy packet exists.
	 * @param packetId - the legacy packet ID.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean hasLegacy(int packetId) {
		return getLookup().getFromLegacy(packetId) != null;
	}

	/**
	 * Retrieve a packet type from a protocol, sender and packet ID.
	 * <p>
	 * It is usually better to access the packet types statically, like so:
	 * <ul>
	 *   <li>{@link PacketType.Play.Server#SPAWN_ENTITY}
	 * </ul>
	 * @param protocol - the current protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID.
	 * @return The corresponding packet type.
	 * @throws IllegalArgumentException If the current packet could not be found.
	 * @deprecated IDs are no longer reliable
	 */
	@Deprecated
	public static PacketType findCurrent(Protocol protocol, Sender sender, int packetId) {
		PacketType type = getLookup().getFromCurrent(protocol, sender, packetId);

		if (type != null)
			return type;
		throw new IllegalArgumentException("Cannot find packet " + packetId +
				"(Protocol: " + protocol + ", Sender: " + sender + ")");
	}

	public static PacketType findCurrent(Protocol protocol, Sender sender, String name) {
		name = format(protocol, sender, name);
		PacketType type = getLookup().getFromCurrent(protocol, sender, name);

		if (type != null) {
			return type;
		} else {
			throw new IllegalArgumentException("Cannot find packet " + name +
					"(Protocol: " + protocol + ", Sender: " + sender + ")");
		}
	}

	private static String format(Protocol protocol, Sender sender, String name) {
		if (name.contains("Packet"))
			return name;

		return String.format("Packet%s%s%s", protocol.getPacketName(), sender.getPacketName(), name);
	}

	/**
	 * Determine if the given packet exists.
	 * @param protocol - the protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID.
	 * @return TRUE if it exists, FALSE otherwise.
	 * @deprecated IDs are no longer reliable
	 */
	@Deprecated
	public static boolean hasCurrent(Protocol protocol, Sender sender, int packetId) {
		return getLookup().getFromCurrent(protocol, sender, packetId) != null;
	}

	/**
	 * Retrieve a packet type from a legacy ID.
	 * <p>
	 * If no associated packet type could be found, a new will be registered under LEGACY.
	 * @param id - the legacy ID.
	 * @param sender - the sender of the packet, or NULL if unknown.
	 * @return The packet type.
	 * @throws IllegalArgumentException If the sender is NULL and the packet doesn't exist.
	 */
	public static PacketType fromLegacy(int id, Sender sender) {
		PacketType type = getLookup().getFromLegacy(id, sender);

		if (type == null) {
			if (sender == null)
				throw new IllegalArgumentException("Cannot find legacy packet " + id);
			type = newLegacy(sender, id);

			// As below
			scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
		}
		return type;
	}

	/**
	 * Retrieve a packet type from a protocol, sender and packet ID, for pre-1.8.
	 * <p>
	 * The packet will automatically be registered if its missing.
	 * @param protocol - the current protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID. Can be UNKNOWN_PACKET.
	 * @param packetClass - the packet class
	 * @return The corresponding packet type.
	 */
	public static PacketType fromID(Protocol protocol, Sender sender, int packetId, Class<?> packetClass) {
		PacketType type = getLookup().getFromCurrent(protocol, sender, packetId);

		if (type == null) {
			type = new PacketType(protocol, sender, packetId, -1, PROTOCOL_VERSION, packetClass.getName());
			type.dynamic = true;

			// Many may be scheduled, but only the first will be executed
			scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
		}

		return type;
	}

	/**
	 * Retrieve a packet type from a protocol, sender, ID, and class for 1.8+
	 * <p>
	 * The packet will automatically be registered if its missing.
	 * @param protocol - the current protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID. Can be UNKNOWN_PACKET.
	 * @param packetClass - the packet class.
	 * @return The corresponding packet type.
	 */
	public static PacketType fromCurrent(Protocol protocol, Sender sender, int packetId, Class<?> packetClass) {
		ClassLookup lookup = getLookup().getClassLookup();
		Map<String, PacketType> map = lookup.getMap(protocol, sender);

		// Check the map first
		String className = packetClass.getSimpleName();
		PacketType type = map.get(className);
		if (type == null) {
			// Then check any aliases
			for (PacketType check : map.values()) {
				String[] aliases = check.getClassNames();
				if (aliases.length > 1) {
					for (String alias : aliases) {
						if (alias.equals(className)) {
							// We have a match!
							type = check;
						}
					}
				}
			}

			// Guess we don't support this packet :/
			type = new PacketType(protocol, sender, packetId, -1, PROTOCOL_VERSION, className);
			type.dynamic = true;

			// Many may be scheduled, but only the first will be executed
			scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
		}

		return type;
	}

	/**
	 * Lookup a packet type from a packet class.
	 * @param packetClass - the packet class.
	 * @return The corresponding packet type, or NULL if not found.
	 */
	public static PacketType fromClass(Class<?> packetClass) {
		PacketType type = PacketRegistry.getPacketType(packetClass);

		if (type != null)
			return type;
		throw new IllegalArgumentException("Class " + packetClass + " is not a registered packet.");
	}

	/**
	 * Retrieve every packet type with the given UPPER_CAMEL_CASE name.
	 * <p>
	 * Note that the collection is unmodiable.
	 * @param name - the name.
	 * @return Every packet type, or an empty collection.
	 */
	public static Collection<PacketType> fromName(String name) {
		return getLookup().getFromName(name);
	}

	/**
	 * Determine if a given class represents a packet class.
	 * @param packetClass - the class to lookup.
	 * @return TRUE if this is a packet class, FALSE otherwise.
	 */
	public static boolean hasClass(Class<?> packetClass) {
		return PacketRegistry.getPacketType(packetClass) != null;
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
				objEnum = getObjectEnum(type);

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
	 * Retrieve the correct object enum from a specific packet type.
	 * @param type - the packet type.
	 * @return The corresponding object enum.
	 */
	public static ObjectEnum<PacketType> getObjectEnum(final PacketType type) {
		switch (type.getProtocol()) {
			case HANDSHAKING:
				return type.isClient() ? Handshake.Client.getInstance() : Handshake.Server.getInstance();
			case PLAY:
				return type.isClient() ? Play.Client.getInstance() : Play.Server.getInstance();
			case STATUS:
				return type.isClient() ? Status.Client.getInstance() : Status.Server.getInstance();
			case LOGIN:
				return type.isClient() ? Login.Client.getInstance() : Login.Server.getInstance();
			case LEGACY:
				return type.isClient() ? Legacy.Client.getInstance() : Legacy.Server.getInstance();
			default:
				throw new IllegalStateException("Unexpected protocol: " + type.getProtocol());
		}
	}

	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param sender - client or server.
	 * @param currentId - the current packet ID, or
	 * @param legacyId - the legacy packet ID.
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, int legacyId, String... names) {
		this(protocol, sender, currentId, legacyId, PROTOCOL_VERSION, names);
	}

	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param sender - client or server.
	 * @param currentId - the current packet ID.
	 * @param legacyId - the legacy packet ID.
	 * @param version - the version of the current ID.
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, int legacyId, MinecraftVersion version, String... names) {
		this.protocol = Preconditions.checkNotNull(protocol, "protocol cannot be NULL");
		this.sender = Preconditions.checkNotNull(sender, "sender cannot be NULL");
		this.currentId = currentId;
		this.legacyId = legacyId;
		this.version = version;
		
		this.classNames = new String[names.length];
		for (int i = 0; i < classNames.length; i++) {
			classNames[i] = format(protocol, sender, names[i]);
		}
		//System.out.println(Arrays.toString(classNames));
	}

	/**
	 * Construct a legacy packet type.
	 * @param sender - client or server.
	 * @param legacyId - the legacy packet ID.
	 * @return Legacy packet type
	 */
	public static PacketType newLegacy(Sender sender, int legacyId) {
		return new PacketType(Protocol.LEGACY, sender, PacketType.UNKNOWN_PACKET, legacyId, MinecraftVersion.WORLD_UPDATE);
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
	 * <p>
	 * It is only unknown if the packet was removed in Minecraft 1.7.2.
	 * @return The current ID, or {@link #UNKNOWN_PACKET} if unknown.
	 * @deprecated IDs are subject to change
	 */
	@Deprecated
	public int getCurrentId() {
		return currentId;
	}

	public String[] getClassNames() {
		return classNames;
	}

	/**
	 * Retrieve the equivalent packet class.
	 * @return The packet class, or NULL if not found.
	 */
	public Class<?> getPacketClass() {
		try {
			return PacketRegistry.getPacketClassFromType(this);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the declared enum name of this packet type.
	 * @return The enum name.
	 */
	public String name() {
		return getObjectEnum(this).getDeclaredName(this);
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

	/**
	 * Whether or not this packet was dynamically created (i.e. we don't have it registered)
	 * @return True if dnyamic, false if not.
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	private PacketType forceAsync(boolean forceAsync) {
		this.forceAsync = forceAsync;
		return this;
	}

	/**
	 * Whether or not this packet must be processed asynchronously.
	 * @return True if it must be, false if not.
	 */
	public boolean forceAsync() {
		return forceAsync;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(protocol, sender, currentId, legacyId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof PacketType) {
			PacketType other = (PacketType) obj;
			return protocol == other.protocol &&
				   sender == other.sender &&
				   currentId == other.currentId &&
				   legacyId == other.legacyId;
		}
		return false;
	}

	@Override
	public int compareTo(PacketType other) {
		return ComparisonChain.start().
				compare(protocol, other.getProtocol()).
				compare(sender, other.getSender()).
				compare(currentId, other.getCurrentId()).
				compare(legacyId, other.getLegacyId()).
				result();
	}

	@Override
	public String toString() {
		Class<?> clazz = getPacketClass();

		if (clazz == null)
			return "UNREGISTERED[" + protocol + ", " + sender + ", " + currentId + ", legacy: " + legacyId + ", classNames: " + Arrays.toString(classNames) + "]";
		else
			return clazz.getSimpleName() + "[" + currentId + ", legacy: " + legacyId + "]";
	}
}
