package com.comphenix.protocol;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.function.Consumer;

import com.comphenix.protocol.PacketTypeLookup.ClassLookup;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents the type of a packet in a specific protocol.
 * <p>
 * Note that vanilla Minecraft reuses packet IDs per protocol (ping, game, login) and IDs are subject to change, so they are not reliable.
 * @author Kristian
 */
public class PacketType implements Serializable, Cloneable, Comparable<PacketType> {
	// Increment whenever the type changes
	private static final long serialVersionUID = 1L;
	
	/**
	 * Represents an unknown packet ID.
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
		public static class Client extends PacketTypeEnum {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType SET_PROTOCOL =                 new PacketType(PROTOCOL, SENDER, 0x00, "SetProtocol", "C00Handshake");

			private final static Client INSTANCE = new Client();

			// Prevent accidental construction
			private Client() { super(); }

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
		public static class Server extends PacketTypeEnum {
			private final static Sender SENDER = Sender.CLIENT;
			private final static Server INSTANCE = new Server();
			private Server() { super(); }

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
		public static class Server extends PacketTypeEnum {
			private final static Sender SENDER = Sender.SERVER;

			public static final PacketType SPAWN_ENTITY =                 new PacketType(PROTOCOL, SENDER, 0x00, "SpawnEntity", "SPacketSpawnObject");
			public static final PacketType SPAWN_ENTITY_EXPERIENCE_ORB =  new PacketType(PROTOCOL, SENDER, 0x01, "SpawnEntityExperienceOrb", "SPacketSpawnExperienceOrb");
			public static final PacketType SPAWN_ENTITY_LIVING =          new PacketType(PROTOCOL, SENDER, 0x02, "SpawnEntityLiving", "SPacketSpawnMob");
			public static final PacketType SPAWN_ENTITY_PAINTING =        new PacketType(PROTOCOL, SENDER, 0x03, "SpawnEntityPainting", "SPacketSpawnPainting");
			public static final PacketType NAMED_ENTITY_SPAWN =           new PacketType(PROTOCOL, SENDER, 0x04, "NamedEntitySpawn", "SPacketSpawnPlayer");
			public static final PacketType ANIMATION =                    new PacketType(PROTOCOL, SENDER, 0x05, "Animation", "SPacketAnimation");
			public static final PacketType STATISTIC =                    new PacketType(PROTOCOL, SENDER, 0x06, "Statistic", "SPacketStatistics");
			public static final PacketType BLOCK_BREAK =                  new PacketType(PROTOCOL, SENDER, 0x07, "BlockBreak");
			public static final PacketType BLOCK_BREAK_ANIMATION =        new PacketType(PROTOCOL, SENDER, 0x08, "BlockBreakAnimation", "SPacketBlockBreakAnim");
			public static final PacketType TILE_ENTITY_DATA =             new PacketType(PROTOCOL, SENDER, 0x09, "TileEntityData", "SPacketUpdateTileEntity");
			public static final PacketType BLOCK_ACTION =                 new PacketType(PROTOCOL, SENDER, 0x0A, "BlockAction", "SPacketBlockAction");
			public static final PacketType BLOCK_CHANGE =                 new PacketType(PROTOCOL, SENDER, 0x0B, "BlockChange", "SPacketBlockChange");
			public static final PacketType BOSS =                         new PacketType(PROTOCOL, SENDER, 0x0C, "Boss", "SPacketUpdateBossInfo");
			public static final PacketType SERVER_DIFFICULTY =            new PacketType(PROTOCOL, SENDER, 0x0D, "ServerDifficulty", "SPacketServerDifficulty");
			public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x0E, "Chat", "SPacketChat");
			public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x0F, "TabComplete", "SPacketTabComplete");
			public static final PacketType COMMANDS =                     new PacketType(PROTOCOL, SENDER, 0x10, "Commands");
			public static final PacketType TRANSACTION =                  new PacketType(PROTOCOL, SENDER, 0x11, "Transaction", "SPacketConfirmTransaction");
			public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x12, "CloseWindow", "SPacketCloseWindow");
			public static final PacketType WINDOW_ITEMS =                 new PacketType(PROTOCOL, SENDER, 0x13, "WindowItems", "SPacketWindowItems");
			public static final PacketType WINDOW_DATA =                  new PacketType(PROTOCOL, SENDER, 0x14, "WindowData", "SPacketWindowProperty");
			public static final PacketType SET_SLOT =                     new PacketType(PROTOCOL, SENDER, 0x15, "SetSlot", "SPacketSetSlot");
			public static final PacketType SET_COOLDOWN =                 new PacketType(PROTOCOL, SENDER, 0x16, "SetCooldown", "SPacketCooldown");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x17, "CustomPayload", "SPacketCustomPayload");
			public static final PacketType CUSTOM_SOUND_EFFECT =          new PacketType(PROTOCOL, SENDER, 0x18, "CustomSoundEffect", "SPacketCustomSound");
			public static final PacketType KICK_DISCONNECT =              new PacketType(PROTOCOL, SENDER, 0x19, "KickDisconnect", "SPacketDisconnect");
			public static final PacketType ENTITY_STATUS =                new PacketType(PROTOCOL, SENDER, 0x1A, "EntityStatus", "SPacketEntityStatus");
			public static final PacketType EXPLOSION =                    new PacketType(PROTOCOL, SENDER, 0x1B, "Explosion", "SPacketExplosion");
			public static final PacketType UNLOAD_CHUNK =                 new PacketType(PROTOCOL, SENDER, 0x1C, "UnloadChunk", "SPacketUnloadChunk");
			public static final PacketType GAME_STATE_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x1D, "GameStateChange", "SPacketChangeGameState");
			public static final PacketType OPEN_WINDOW_HORSE =            new PacketType(PROTOCOL, SENDER, 0x1E, "OpenWindowHorse");
			public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x1F, "KeepAlive", "SPacketKeepAlive");
			public static final PacketType MAP_CHUNK =                    new PacketType(PROTOCOL, SENDER, 0x20, "MapChunk", "SPacketChunkData");
			public static final PacketType WORLD_EVENT =                  new PacketType(PROTOCOL, SENDER, 0x21, "WorldEvent", "SPacketEffect");
			public static final PacketType WORLD_PARTICLES =              new PacketType(PROTOCOL, SENDER, 0x22, "WorldParticles", "SPacketParticles");
			public static final PacketType LIGHT_UPDATE =                 new PacketType(PROTOCOL, SENDER, 0x23, "LightUpdate");
			public static final PacketType LOGIN =                        new PacketType(PROTOCOL, SENDER, 0x24, "Login", "SPacketJoinGame");
			public static final PacketType MAP =                          new PacketType(PROTOCOL, SENDER, 0x25, "Map", "SPacketMaps");
			public static final PacketType OPEN_WINDOW_MERCHANT =         new PacketType(PROTOCOL, SENDER, 0x26, "OpenWindowMerchant");
			public static final PacketType REL_ENTITY_MOVE =              new PacketType(PROTOCOL, SENDER, 0x27, "Entity$RelEntityMove", "SPacketEntity$S15PacketEntityRelMove");
			public static final PacketType REL_ENTITY_MOVE_LOOK =         new PacketType(PROTOCOL, SENDER, 0x28, "Entity$RelEntityMoveLook", "SPacketEntity$S17PacketEntityLookMove");
			public static final PacketType ENTITY_LOOK =                  new PacketType(PROTOCOL, SENDER, 0x29, "Entity$EntityLook", "SPacketEntity$S16PacketEntityLook");
			public static final PacketType ENTITY =                       new PacketType(PROTOCOL, SENDER, 0x2A, "Entity", "SPacketEntity");
			public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x2B, "VehicleMove", "SPacketMoveVehicle");
			public static final PacketType OPEN_BOOK =                    new PacketType(PROTOCOL, SENDER, 0x2C, "OpenBook");
			public static final PacketType OPEN_WINDOW =                  new PacketType(PROTOCOL, SENDER, 0x2D, "OpenWindow", "SPacketOpenWindow");
			public static final PacketType OPEN_SIGN_EDITOR =             new PacketType(PROTOCOL, SENDER, 0x2E, "OpenSignEditor", "SPacketSignEditorOpen");
			public static final PacketType AUTO_RECIPE =                  new PacketType(PROTOCOL, SENDER, 0x2F, "AutoRecipe", "SPacketPlaceGhostRecipe");
			public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x30, "Abilities", "SPacketPlayerAbilities");
			public static final PacketType COMBAT_EVENT =                 new PacketType(PROTOCOL, SENDER, 0x31, "CombatEvent", "SPacketCombatEvent");
			public static final PacketType PLAYER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x32, "PlayerInfo", "SPacketPlayerListItem");
			public static final PacketType LOOK_AT =                      new PacketType(PROTOCOL, SENDER, 0x33, "LookAt", "SPacketPlayerPosLook");
			public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x34, "Position");
			public static final PacketType RECIPES =                      new PacketType(PROTOCOL, SENDER, 0x35, "Recipes", "SPacketRecipeBook");
			public static final PacketType ENTITY_DESTROY =               new PacketType(PROTOCOL, SENDER, 0x36, "EntityDestroy", "SPacketDestroyEntities");
			public static final PacketType REMOVE_ENTITY_EFFECT =         new PacketType(PROTOCOL, SENDER, 0x37, "RemoveEntityEffect", "SPacketRemoveEntityEffect");
			public static final PacketType RESOURCE_PACK_SEND =           new PacketType(PROTOCOL, SENDER, 0x38, "ResourcePackSend", "SPacketResourcePackSend");
			public static final PacketType RESPAWN =                      new PacketType(PROTOCOL, SENDER, 0x39, "Respawn", "SPacketRespawn");
			public static final PacketType ENTITY_HEAD_ROTATION =         new PacketType(PROTOCOL, SENDER, 0x3A, "EntityHeadRotation", "SPacketEntityHeadLook");
			public static final PacketType MULTI_BLOCK_CHANGE =           new PacketType(PROTOCOL, SENDER, 0x3B, "MultiBlockChange", "SPacketMultiBlockChange");
			public static final PacketType SELECT_ADVANCEMENT_TAB =       new PacketType(PROTOCOL, SENDER, 0x3C, "SelectAdvancementTab", "SPacketSelectAdvancementsTab");
			public static final PacketType WORLD_BORDER =                 new PacketType(PROTOCOL, SENDER, 0x3D, "WorldBorder", "SPacketWorldBorder");
			public static final PacketType CAMERA =                       new PacketType(PROTOCOL, SENDER, 0x3E, "Camera", "SPacketCamera");
			public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x3F, "HeldItemSlot", "SPacketHeldItemChange");
			public static final PacketType VIEW_CENTRE =                  new PacketType(PROTOCOL, SENDER, 0x40, "ViewCentre");
			public static final PacketType VIEW_DISTANCE =                new PacketType(PROTOCOL, SENDER, 0x41, "ViewDistance");
			public static final PacketType SPAWN_POSITION =               new PacketType(PROTOCOL, SENDER, 0x42, "SpawnPosition", "SPacketSpawnPosition");
			public static final PacketType SCOREBOARD_DISPLAY_OBJECTIVE = new PacketType(PROTOCOL, SENDER, 0x43, "ScoreboardDisplayObjective", "SPacketDisplayObjective");
			public static final PacketType ENTITY_METADATA =              new PacketType(PROTOCOL, SENDER, 0x44, "EntityMetadata", "SPacketEntityMetadata");
			public static final PacketType ATTACH_ENTITY =                new PacketType(PROTOCOL, SENDER, 0x45, "AttachEntity", "SPacketEntityAttach");
			public static final PacketType ENTITY_VELOCITY =              new PacketType(PROTOCOL, SENDER, 0x46, "EntityVelocity", "SPacketEntityVelocity");
			public static final PacketType ENTITY_EQUIPMENT =             new PacketType(PROTOCOL, SENDER, 0x47, "EntityEquipment", "SPacketEntityEquipment");
			public static final PacketType EXPERIENCE =                   new PacketType(PROTOCOL, SENDER, 0x48, "Experience", "SPacketSetExperience");
			public static final PacketType UPDATE_HEALTH =                new PacketType(PROTOCOL, SENDER, 0x49, "UpdateHealth", "SPacketUpdateHealth");
			public static final PacketType SCOREBOARD_OBJECTIVE =         new PacketType(PROTOCOL, SENDER, 0x4A, "ScoreboardObjective", "SPacketScoreboardObjective");
			public static final PacketType MOUNT =                        new PacketType(PROTOCOL, SENDER, 0x4B, "Mount", "SPacketSetPassengers");
			public static final PacketType SCOREBOARD_TEAM =              new PacketType(PROTOCOL, SENDER, 0x4C, "ScoreboardTeam", "SPacketTeams");
			public static final PacketType SCOREBOARD_SCORE =             new PacketType(PROTOCOL, SENDER, 0x4D, "ScoreboardScore", "SPacketUpdateScore");
			public static final PacketType UPDATE_TIME =                  new PacketType(PROTOCOL, SENDER, 0x4E, "UpdateTime", "SPacketTimeUpdate");
			public static final PacketType TITLE =                        new PacketType(PROTOCOL, SENDER, 0x4F, "Title", "SPacketTitle");
			public static final PacketType ENTITY_SOUND =                 new PacketType(PROTOCOL, SENDER, 0x50, "EntitySound", "SPacketSoundEffect");
			public static final PacketType NAMED_SOUND_EFFECT =           new PacketType(PROTOCOL, SENDER, 0x51, "NamedSoundEffect");
			public static final PacketType STOP_SOUND =                   new PacketType(PROTOCOL, SENDER, 0x52, "StopSound");
			public static final PacketType PLAYER_LIST_HEADER_FOOTER =    new PacketType(PROTOCOL, SENDER, 0x53, "PlayerListHeaderFooter", "SPacketPlayerListHeaderFooter");
			public static final PacketType NBT_QUERY =                    new PacketType(PROTOCOL, SENDER, 0x54, "NBTQuery");
			public static final PacketType COLLECT =                      new PacketType(PROTOCOL, SENDER, 0x55, "Collect", "SPacketCollectItem");
			public static final PacketType ENTITY_TELEPORT =              new PacketType(PROTOCOL, SENDER, 0x56, "EntityTeleport", "SPacketEntityTeleport");
			public static final PacketType ADVANCEMENTS =                 new PacketType(PROTOCOL, SENDER, 0x57, "Advancements", "SPacketAdvancementInfo");
			public static final PacketType UPDATE_ATTRIBUTES =            new PacketType(PROTOCOL, SENDER, 0x58, "UpdateAttributes", "SPacketEntityProperties");
			public static final PacketType ENTITY_EFFECT =                new PacketType(PROTOCOL, SENDER, 0x59, "EntityEffect", "SPacketEntityEffect");
			public static final PacketType RECIPE_UPDATE =                new PacketType(PROTOCOL, SENDER, 0x5A, "RecipeUpdate");
			public static final PacketType TAGS =                         new PacketType(PROTOCOL, SENDER, 0x5B, "Tags");

			// ---- Removed in 1.9

			/**
			 * @deprecated Removed in 1.9
			 */
			@Deprecated
			public static final PacketType MAP_CHUNK_BULK =              new PacketType(PROTOCOL, SENDER, 255, "MapChunkBulk");

			/**
			 * @deprecated Removed in 1.9
			 */
			@Deprecated
			public static final PacketType SET_COMPRESSION =             new PacketType(PROTOCOL, SENDER, 254, "SetCompression");

			/**
			 * @deprecated Removed in 1.9
			 */
			@Deprecated
			public static final PacketType UPDATE_ENTITY_NBT =           new PacketType(PROTOCOL, SENDER, 253, "UpdateEntityNBT");

			// ----- Renamed packets

			/**
			 * @deprecated Renamed to {@link #WINDOW_DATA}
			 */
			@Deprecated
			public static final PacketType CRAFT_PROGRESS_BAR =           WINDOW_DATA.clone();

			/**
			 * @deprecated Renamed to {@link #REL_ENTITY_MOVE_LOOK}
			 */
			@Deprecated
			public static final PacketType ENTITY_MOVE_LOOK =             REL_ENTITY_MOVE_LOOK.clone();

			/**
			 * @deprecated Renamed to {@link #STATISTIC}
			 */
			@Deprecated
			public static final PacketType STATISTICS =                   STATISTIC.clone();

			/**
			 * @deprecated Renamed to {@link #OPEN_SIGN_EDITOR}
			 */
			@Deprecated
			public static final PacketType OPEN_SIGN_ENTITY =             OPEN_SIGN_EDITOR.clone();

			// ----- Replaced in 1.9.4

			/**
			 * @deprecated Replaced by {@link #TILE_ENTITY_DATA}
			 */
			@Deprecated
			public static final PacketType UPDATE_SIGN =                  MinecraftReflection.signUpdateExists() ? new PacketType(PROTOCOL, SENDER, 252, "UpdateSign") :
																			  TILE_ENTITY_DATA.clone();

			// ---- Removed in 1.14

			/**
			 * @deprecated Removed in 1.14
			 */
			@Deprecated
			public static final PacketType BED =                          new PacketType(PROTOCOL, SENDER, 0x33, "Bed", "SPacketUseBed");

			/**
			 * @deprecated Renamed to {@link #BED}
			 */
			@Deprecated
			public static final PacketType USE_BED =                      BED.clone();

			/**
			 * @deprecated Removed in 1.16
			 */
			@Deprecated
			public static final PacketType SPAWN_ENTITY_WEATHER =         new PacketType(PROTOCOL, SENDER, 0x02, "SpawnEntityWeather", "SPacketSpawnGlobalEntity");

			private final static Server INSTANCE = new Server();

			// Prevent accidental construction
			private Server() { super(); }

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
		public static class Client extends PacketTypeEnum {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType TELEPORT_ACCEPT =              new PacketType(PROTOCOL, SENDER, 0x00, "TeleportAccept", "CPacketConfirmTeleport");
			public static final PacketType TILE_NBT_QUERY =               new PacketType(PROTOCOL, SENDER, 0x01, "TileNBTQuery");
			public static final PacketType DIFFICULTY_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x02, "DifficultyChange");
			public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x03, "Chat", "CPacketChatMessage");
			public static final PacketType CLIENT_COMMAND =               new PacketType(PROTOCOL, SENDER, 0x04, "ClientCommand", "CPacketClientStatus");
			public static final PacketType SETTINGS =                     new PacketType(PROTOCOL, SENDER, 0x05, "Settings", "CPacketClientSettings");
			public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x06, "TabComplete", "CPacketTabComplete");
			public static final PacketType TRANSACTION =                  new PacketType(PROTOCOL, SENDER, 0x07, "Transaction", "CPacketConfirmTransaction");
			public static final PacketType ENCHANT_ITEM =                 new PacketType(PROTOCOL, SENDER, 0x08, "EnchantItem", "CPacketEnchantItem");
			public static final PacketType WINDOW_CLICK =                 new PacketType(PROTOCOL, SENDER, 0x09, "WindowClick", "CPacketClickWindow");
			public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x0A, "CloseWindow", "CPacketCloseWindow");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x0B, "CustomPayload", "CPacketCustomPayload");
			public static final PacketType B_EDIT =                       new PacketType(PROTOCOL, SENDER, 0x0C, "BEdit");
			public static final PacketType ENTITY_NBT_QUERY =             new PacketType(PROTOCOL, SENDER, 0x0D, "EntityNBTQuery");
			public static final PacketType USE_ENTITY =                   new PacketType(PROTOCOL, SENDER, 0x0E, "UseEntity", "CPacketUseEntity");
			public static final PacketType JIGSAW_GENERATE =              new PacketType(PROTOCOL, SENDER, 0x0F, "JigsawGenerate");
			public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x10, "KeepAlive", "CPacketKeepAlive");
			public static final PacketType DIFFICULTY_LOCK =              new PacketType(PROTOCOL, SENDER, 0x11, "DifficultyLock");
			public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x12, "Flying$Position", "CPacketPlayer$Position");
			public static final PacketType POSITION_LOOK =                new PacketType(PROTOCOL, SENDER, 0x13, "Flying$PositionLook", "CPacketPlayer$PositionRotation");
			public static final PacketType LOOK =                         new PacketType(PROTOCOL, SENDER, 0x14, "Flying$Look", "CPacketPlayer$Rotation");
			public static final PacketType FLYING =                       new PacketType(PROTOCOL, SENDER, 0x15, "Flying", "CPacketPlayer");
			public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x16, "VehicleMove", "CPacketVehicleMove");
			public static final PacketType BOAT_MOVE =                    new PacketType(PROTOCOL, SENDER, 0x17, "BoatMove", "CPacketSteerBoat");
			public static final PacketType PICK_ITEM =                    new PacketType(PROTOCOL, SENDER, 0x18, "PickItem");
			public static final PacketType AUTO_RECIPE =                  new PacketType(PROTOCOL, SENDER, 0x19, "AutoRecipe", "CPacketPlaceRecipe");
			public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x1A, "Abilities", "CPacketPlayerAbilities");
			public static final PacketType BLOCK_DIG =                    new PacketType(PROTOCOL, SENDER, 0x1B, "BlockDig", "CPacketPlayerDigging");
			public static final PacketType ENTITY_ACTION =                new PacketType(PROTOCOL, SENDER, 0x1C, "EntityAction", "CPacketEntityAction");
			public static final PacketType STEER_VEHICLE =                new PacketType(PROTOCOL, SENDER, 0x1D, "SteerVehicle", "CPacketInput");
			public static final PacketType RECIPE_SETTINGS =              new PacketType(PROTOCOL, SENDER, 0x1E, "RecipeSettings");
			public static final PacketType RECIPE_DISPLAYED =             new PacketType(PROTOCOL, SENDER, 0x1F, "RecipeDisplayed", "CPacketRecipeInfo");
			public static final PacketType ITEM_NAME =                    new PacketType(PROTOCOL, SENDER, 0x20, "ItemName");
			public static final PacketType RESOURCE_PACK_STATUS =         new PacketType(PROTOCOL, SENDER, 0x21, "ResourcePackStatus", "CPacketResourcePackStatus");
			public static final PacketType ADVANCEMENTS =                 new PacketType(PROTOCOL, SENDER, 0x22, "Advancements", "CPacketSeenAdvancements");
			public static final PacketType TR_SEL =                       new PacketType(PROTOCOL, SENDER, 0x23, "TrSel");
			public static final PacketType BEACON =                       new PacketType(PROTOCOL, SENDER, 0x24, "Beacon");
			public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x25, "HeldItemSlot", "CPacketHeldItemChange");
			public static final PacketType SET_COMMAND_BLOCK =            new PacketType(PROTOCOL, SENDER, 0x26, "SetCommandBlock");
			public static final PacketType SET_COMMAND_MINECART =         new PacketType(PROTOCOL, SENDER, 0x27, "SetCommandMinecart");
			public static final PacketType SET_CREATIVE_SLOT =            new PacketType(PROTOCOL, SENDER, 0x28, "SetCreativeSlot", "CPacketCreativeInventoryAction");
			public static final PacketType SET_JIGSAW =                   new PacketType(PROTOCOL, SENDER, 0x29, "SetJigsaw");
			public static final PacketType STRUCT =                       new PacketType(PROTOCOL, SENDER, 0x2A, "Struct");
			public static final PacketType UPDATE_SIGN =                  new PacketType(PROTOCOL, SENDER, 0x2B, "UpdateSign", "CPacketUpdateSign");
			public static final PacketType ARM_ANIMATION =                new PacketType(PROTOCOL, SENDER, 0x2C, "ArmAnimation", "CPacketAnimation");
			public static final PacketType SPECTATE =                     new PacketType(PROTOCOL, SENDER, 0x2D, "Spectate", "CPacketSpectate");
			public static final PacketType USE_ITEM =                     new PacketType(PROTOCOL, SENDER, 0x2E, "UseItem", "CPacketPlayerTryUseItemOnBlock");
			public static final PacketType BLOCK_PLACE =                  new PacketType(PROTOCOL, SENDER, 0x2F, "BlockPlace", "CPacketPlayerTryUseItem");

			private final static Client INSTANCE = new Client();

			// Prevent accidental construction
			private Client() { super(); }

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
		public static class Server extends PacketTypeEnum {
			private final static Sender SENDER = Sender.SERVER;

			@ForceAsync
			public static final PacketType SERVER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x00, "ServerInfo", "SPacketServerInfo");
			public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x01, "Pong", "SPacketPong");

			/**
			 * @deprecated Renamed to {@link #SERVER_INFO}
			 */
			@Deprecated
			@ForceAsync
			public static final PacketType OUT_SERVER_INFO =              SERVER_INFO.clone();

			private final static Server INSTANCE = new Server();

			// Prevent accidental construction
			private Server() { super(); }

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
		public static class Client extends PacketTypeEnum {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, "Start", "CPacketServerQuery");
			public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x01, "Ping", "CPacketPing");

			private final static Client INSTANCE = new Client();

			// Prevent accidental construction
			private Client() { super(); }

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
		public static class Server extends PacketTypeEnum {
			private final static Sender SENDER = Sender.SERVER;

			public static final PacketType DISCONNECT =                   new PacketType(PROTOCOL, SENDER, 0x00, "Disconnect", "SPacketDisconnect");
			public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, "EncryptionBegin", "SPacketEncryptionRequest");
			public static final PacketType SUCCESS =                      new PacketType(PROTOCOL, SENDER, 0x02, "Success", "SPacketLoginSuccess");
			public static final PacketType SET_COMPRESSION =              new PacketType(PROTOCOL, SENDER, 0x03, "SetCompression", "SPacketEnableCompression");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x04, "CustomPayload", "SPacketCustomPayload");

			private final static Server INSTANCE = new Server();

			// Prevent accidental construction
			private Server() { super(); }

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
		public static class Client extends PacketTypeEnum {
			private final static Sender SENDER = Sender.CLIENT;

			public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, "Start", "CPacketLoginStart");
			public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, "EncryptionBegin", "CPacketEncryptionResponse");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x02, "CustomPayload", "CPacketCustomPayload");

			private final static Client INSTANCE = new Client();

			// Prevent accidental construction
			private Client() { super(); }

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
		HANDSHAKING("Handshaking", "handshake"),
		PLAY("Play", "game"),
		STATUS("Status", "status"),
		LOGIN("Login", "login"),

		/**
		 * Only for packets removed in Minecraft 1.7.2
		 */
		LEGACY("", "");

		private String packetName;
		private String mojangName;

		Protocol(String packetName, String mojangName) {
			this.packetName = packetName;
			this.mojangName = mojangName;
		}

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
			return packetName;
		}

		public String getMojangName() {
			return mojangName;
		}

		public String getMcpPacketName() {
			return name().toLowerCase(Locale.ENGLISH);
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

		public String getMcpPacketName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	/**
	 * Whether or not packets of this type must be handled asynchronously.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ForceAsync { }

	// Lookup of packet types
	private static PacketTypeLookup LOOKUP;

	/**
	 * Protocol version of all the current IDs.
	 */
	private static final MinecraftVersion PROTOCOL_VERSION = MinecraftVersion.NETHER_UPDATE;

	private final Protocol protocol;
	private final Sender sender;
	private final int currentId;
	private final MinecraftVersion version;
	private final String[] classNames;
	String[] names;

	private String name;
	private boolean deprecated;
	private boolean forceAsync;

	private boolean dynamic;
	private int hashCode;

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
	 * @deprecated Legacy IDs haven't functioned properly for some time
	 */
	@Deprecated
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
	 * @deprecated Legacy IDs haven't functioned properly for some time
	 */
	@Deprecated
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
	 * @deprecated Legacy IDs haven't functioned properly for some time
	 */
	@Deprecated
	public static boolean hasLegacy(int packetId) {
		return getLookup().getFromLegacy(packetId) != null;
	}

	/**
	 * Retrieve a packet type from a protocol, sender and packet ID.
	 * <p>
	 * It is almost always better to access the packet types statically, like so:
	 * <ul>
	 *   <li>{@link PacketType.Play.Server#SPAWN_ENTITY}
	 * </ul>
	 * However there are some valid uses for packet IDs. Please note that IDs
	 * change almost every Minecraft version.
	 *
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

	public static PacketType findCurrent(Protocol protocol, Sender sender, String name) {
		name = formatClassName(protocol, sender, name);
		PacketType type = getLookup().getFromCurrent(protocol, sender, name);

		if (type != null) {
			return type;
		} else {
			throw new IllegalArgumentException("Cannot find packet " + name +
					"(Protocol: " + protocol + ", Sender: " + sender + ")");
		}
	}

	private static String formatClassName(Protocol protocol, Sender sender, String name) {
		if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
			return "net.minecraft.network.protocol." + protocol.getMojangName() + ".Packet"
					+ protocol.getPacketName() + sender.getPacketName() + name;
		}

		String base = MinecraftReflection.getMinecraftPackage() + ".Packet";
		if (name.startsWith(base)) {
			return name;
		}

		if (name.contains("$")) {
			String[] split = name.split("\\$");
			String parent = split[0];
			String child = split[1];
			return base + protocol.getPacketName() + sender.getPacketName() + WordUtils.capitalize(parent)
					+ "$Packet" + protocol.getPacketName() + sender.getPacketName() + WordUtils.capitalize(child);
		}

		return base + protocol.getPacketName() + sender.getPacketName() + WordUtils.capitalize(name);
	}

	private static boolean isMcpPacketName(String packetName) {
		return packetName.startsWith("C00") || packetName.startsWith("CPacket") || packetName.startsWith("SPacket");
	}

	private static String formatMcpClassName(Protocol protocol, Sender sender, String name) {
		return "net.minecraft.network." + protocol.getMcpPacketName() + "." + sender.getMcpPacketName() + "." + name;
	}

	/**
	 * Determine if the given packet exists.
	 * @param protocol - the protocol.
	 * @param sender - the sender.
	 * @param packetId - the packet ID.
	 * @return TRUE if it exists, FALSE otherwise.
	 */
	public static boolean hasCurrent(Protocol protocol, Sender sender, int packetId) {
		return getLookup().getFromCurrent(protocol, sender, packetId) != null;
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
			type = new PacketType(protocol, sender, packetId, PROTOCOL_VERSION, packetClass.getName());
			type.dynamic = true;

			// Many may be scheduled, but only the first will be executed
			scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
		}

		return type;
	}

	static Consumer<String> onDynamicCreate = x -> {};

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
		String className = packetClass.getName();
		PacketType type = find(map, className);
		if (type == null) {
			// Guess we don't support this packet :/
			type = new PacketType(protocol, sender, packetId, PROTOCOL_VERSION, className);
			type.dynamic = true;

			// Many may be scheduled, but only the first will be executed
			scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
			onDynamicCreate.accept(className);
		}

		return type;
	}

	private static PacketType find(Map<String, PacketType> map, String clazz) {
		PacketType ret = map.get(clazz);
		if (ret != null) {
			return ret;
		}

		// Check any aliases
		for (PacketType check : map.values()) {
			String[] aliases = check.getClassNames();
			if (aliases.length > 1) {
				for (String alias : aliases) {
					if (alias.equals(clazz)) {
						// We have a match!
						return check;
					}
				}
			}
		}

		return null;
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
	 */
	public static void scheduleRegister(final PacketType type, final String name) {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				PacketTypeEnum objEnum;

				// A bit ugly, but performance is critical
				objEnum = getObjectEnum(type);

				if (objEnum.registerMember(type, name)) {
					getLookup().addPacketTypes(Collections.singletonList(type));
				}
			}
		};

		if (Bukkit.getServer() == null || Bukkit.isPrimaryThread()) {
			try {
				runnable.run();
			} catch (Exception ignored) { }
		} else {
			runnable.runTaskLater(ProtocolLibrary.getPlugin(), 0);
		}
	}

	/**
	 * Retrieve the correct object enum from a specific packet type.
	 * @param type - the packet type.
	 * @return The corresponding object enum.
	 */
	public static PacketTypeEnum getObjectEnum(final PacketType type) {
		switch (type.getProtocol()) {
			case HANDSHAKING:
				return type.isClient() ? Handshake.Client.getInstance() : Handshake.Server.getInstance();
			case PLAY:
				return type.isClient() ? Play.Client.getInstance() : Play.Server.getInstance();
			case STATUS:
				return type.isClient() ? Status.Client.getInstance() : Status.Server.getInstance();
			case LOGIN:
				return type.isClient() ? Login.Client.getInstance() : Login.Server.getInstance();
			default:
				throw new IllegalStateException("Unexpected protocol: " + type.getProtocol());
		}
	}

	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param sender - client or server.
	 * @param currentId - the current packet ID, or
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, String... names) {
		this(protocol, sender, currentId, PROTOCOL_VERSION, names);
	}

	/**
	 * Construct a new packet type.
	 * @param protocol - the current protocol.
	 * @param sender - client or server.
	 * @param currentId - the current packet ID.
	 * @param version - the version of the current ID.
	 */
	public PacketType(Protocol protocol, Sender sender, int currentId, MinecraftVersion version, String... names) {
		this.protocol = Preconditions.checkNotNull(protocol, "protocol cannot be NULL");
		this.sender = Preconditions.checkNotNull(sender, "sender cannot be NULL");
		this.currentId = currentId;
		this.version = version;
		
		this.classNames = new String[names.length];
		for (int i = 0; i < classNames.length; i++) {
			if (isMcpPacketName(names[i])) { // Minecraft MCP packets
				classNames[i] = formatMcpClassName(protocol, sender, names[i]);
			} else {
				classNames[i] = formatClassName(protocol, sender, names[i]);
			}
		}

		this.names = names;
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
	 * It is unknown if the packet was removed at any point.
	 * @return The current ID, or {@link #UNKNOWN_PACKET} if unknown.
	 * @deprecated Don't rely on packet IDs, they change every version
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

	// Only used by Enum processor
	void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieve the declared enum name of this packet type.
	 * @return The enum name.
	 */
	public String name() {
		return name;
	}

	// Only used by enum processor
	void setDeprecated() {
		this.deprecated = true;
	}

	/**
	 * Whether or not this packet is deprecated. Deprecated packet types have either been renamed, replaced, or removed.
	 * Kind of like the thing they use to tell children to recycle except with packets you probably shouldn't be using.
	 *
	 * @return True if the type is deprecated, false if not
	 */
	public boolean isDeprecated() {
		return deprecated;
	}

	// Only used by enum processor
	void forceAsync() {
		this.forceAsync = true;
	}

	/**
	 * Whether or not the processing of this packet must take place on a thread different than the main thread. You don't
	 * get a choice. If this is false it's up to you.
	 *
	 * @return True if async processing is forced, false if not.
	 */
	public boolean isAsyncForced() {
		return forceAsync;
	}

	/**
	 * Retrieve the Minecraft version for the current ID.
	 * @return The Minecraft version.
	 */
	public MinecraftVersion getCurrentVersion() {
		return version;
	}

	/**
	 * Whether or not this packet was dynamically created (i.e. we don't have it registered)
	 * @return True if dnyamic, false if not.
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	@Override
	public int hashCode() {
		int hash = hashCode;
		if (hash == 0) {
			hash = protocol.hashCode();
			hash = 31 * hash + sender.hashCode();
			hash = 31 * hash + Integer.hashCode(currentId);
			hashCode = hash;
		}
		return hash;
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
	public int compareTo(PacketType other) {
		return ComparisonChain.start().
				compare(protocol, other.getProtocol()).
				compare(sender, other.getSender()).
				compare(currentId, other.getCurrentId()).
				result();
	}

	@Override
	public String toString() {
		Class<?> clazz = getPacketClass();

		if (clazz == null)
			return name() + "[" + protocol + ", " + sender + ", " + currentId + ", classNames: " + Arrays.toString(classNames) + " (unregistered)]";
		else
			return name() + "[class=" + clazz.getSimpleName() + ", id=" + currentId + "]";
	}

	@Override
	public PacketType clone() {
		try {
			return (PacketType) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new Error("This shouldn't happen", ex);
		}
	}
}
