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
		static final Protocol PROTOCOL = Protocol.HANDSHAKING;

		/**
		 * Incoming packets.
		 * @author Kristian
		 */
		public static class Client extends PacketTypeEnum {
			private static final Sender SENDER = Sender.CLIENT;

			@ForceAsync
			public static final PacketType SET_PROTOCOL =                 new PacketType(PROTOCOL, SENDER, 0x00, "SetProtocol", "C00Handshake");

			private static final Client INSTANCE = new Client();

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
			private static final Sender SENDER = Sender.CLIENT;
			private static final Server INSTANCE = new Server();
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
		static final Protocol PROTOCOL = Protocol.PLAY;

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		public static class Server extends PacketTypeEnum {
			private static final Sender SENDER = Sender.SERVER;

			public static final PacketType BUNDLE =                       new PacketType(PROTOCOL, SENDER, 0x00, "Delimiter", "BundleDelimiterPacket");
			public static final PacketType SPAWN_ENTITY =                 new PacketType(PROTOCOL, SENDER, 0x01, "SpawnEntity", "SPacketSpawnObject");
			public static final PacketType SPAWN_ENTITY_EXPERIENCE_ORB =  new PacketType(PROTOCOL, SENDER, 0x02, "SpawnEntityExperienceOrb", "SPacketSpawnExperienceOrb");
			public static final PacketType NAMED_ENTITY_SPAWN =           new PacketType(PROTOCOL, SENDER, 0x03, "NamedEntitySpawn", "SPacketSpawnPlayer");
			public static final PacketType ANIMATION =                    new PacketType(PROTOCOL, SENDER, 0x04, "Animation", "SPacketAnimation");
			public static final PacketType STATISTIC =                    new PacketType(PROTOCOL, SENDER, 0x05, "Statistic", "SPacketStatistics");
			public static final PacketType BLOCK_CHANGED_ACK =            new PacketType(PROTOCOL, SENDER, 0x06, "BlockChangedAck");
			public static final PacketType BLOCK_BREAK_ANIMATION =        new PacketType(PROTOCOL, SENDER, 0x07, "BlockBreakAnimation", "SPacketBlockBreakAnim");
			public static final PacketType TILE_ENTITY_DATA =             new PacketType(PROTOCOL, SENDER, 0x08, "TileEntityData", "SPacketUpdateTileEntity");
			public static final PacketType BLOCK_ACTION =                 new PacketType(PROTOCOL, SENDER, 0x09, "BlockAction", "SPacketBlockAction");
			public static final PacketType BLOCK_CHANGE =                 new PacketType(PROTOCOL, SENDER, 0x0A, "BlockChange", "SPacketBlockChange");
			public static final PacketType BOSS =                         new PacketType(PROTOCOL, SENDER, 0x0B, "Boss", "SPacketUpdateBossInfo");
			public static final PacketType SERVER_DIFFICULTY =            new PacketType(PROTOCOL, SENDER, 0x0C, "ServerDifficulty", "SPacketServerDifficulty");
			public static final PacketType CHUNKS_BIOMES =                new PacketType(PROTOCOL, SENDER, 0x0D, "ChunksBiomes", "ClientboundChunksBiomesPacket");
			public static final PacketType CLEAR_TITLES =                 new PacketType(PROTOCOL, SENDER, 0x0E, "ClearTitles");
			public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x0F, "TabComplete", "SPacketTabComplete");
			public static final PacketType COMMANDS =                     new PacketType(PROTOCOL, SENDER, 0x10, "Commands");
			public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x11, "CloseWindow", "SPacketCloseWindow");
			public static final PacketType WINDOW_ITEMS =                 new PacketType(PROTOCOL, SENDER, 0x12, "WindowItems", "SPacketWindowItems");
			public static final PacketType WINDOW_DATA =                  new PacketType(PROTOCOL, SENDER, 0x13, "WindowData", "SPacketWindowProperty");
			public static final PacketType SET_SLOT =                     new PacketType(PROTOCOL, SENDER, 0x14, "SetSlot", "SPacketSetSlot");
			public static final PacketType SET_COOLDOWN =                 new PacketType(PROTOCOL, SENDER, 0x15, "SetCooldown", "SPacketCooldown");
			public static final PacketType CUSTOM_CHAT_COMPLETIONS =      new PacketType(PROTOCOL, SENDER, 0x16, "CustomChatCompletions");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x17, "CustomPayload", "SPacketCustomPayload");
			public static final PacketType DAMAGE_EVENT =                 new PacketType(PROTOCOL, SENDER, 0x18, "DamageEvent", "ClientboundDamageEventPacket");
			public static final PacketType DELETE_CHAT_MESSAGE =          new PacketType(PROTOCOL, SENDER, 0x19, "DeleteChat");
			public static final PacketType KICK_DISCONNECT =              new PacketType(PROTOCOL, SENDER, 0x1A, "KickDisconnect", "SPacketDisconnect");
			public static final PacketType DISGUISED_CHAT =               new PacketType(PROTOCOL, SENDER, 0x1B, "DisguisedChat");
			public static final PacketType ENTITY_STATUS =                new PacketType(PROTOCOL, SENDER, 0x1C, "EntityStatus", "SPacketEntityStatus");
			public static final PacketType EXPLOSION =                    new PacketType(PROTOCOL, SENDER, 0x1D, "Explosion", "SPacketExplosion");
			public static final PacketType UNLOAD_CHUNK =                 new PacketType(PROTOCOL, SENDER, 0x1E, "UnloadChunk", "SPacketUnloadChunk");
			public static final PacketType GAME_STATE_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x1F, "GameStateChange", "SPacketChangeGameState");
			public static final PacketType OPEN_WINDOW_HORSE =            new PacketType(PROTOCOL, SENDER, 0x20, "OpenWindowHorse");
			public static final PacketType HURT_ANIMATION =               new PacketType(PROTOCOL, SENDER, 0x21, "HurtAnimation", "ClientboundHurtAnimationPacket");
			public static final PacketType INITIALIZE_BORDER =            new PacketType(PROTOCOL, SENDER, 0x22, "InitializeBorder");
			public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x23, "KeepAlive", "SPacketKeepAlive");
			public static final PacketType MAP_CHUNK =                    new PacketType(PROTOCOL, SENDER, 0x24, "LevelChunkWithLight", "MapChunk", "SPacketChunkData");
			public static final PacketType WORLD_EVENT =                  new PacketType(PROTOCOL, SENDER, 0x25, "WorldEvent", "SPacketEffect");
			public static final PacketType WORLD_PARTICLES =              new PacketType(PROTOCOL, SENDER, 0x26, "WorldParticles", "SPacketParticles");
			public static final PacketType LIGHT_UPDATE =                 new PacketType(PROTOCOL, SENDER, 0x27, "LightUpdate");
			public static final PacketType LOGIN =                        new PacketType(PROTOCOL, SENDER, 0x28, "Login", "SPacketJoinGame");
			public static final PacketType MAP =                          new PacketType(PROTOCOL, SENDER, 0x29, "Map", "SPacketMaps");
			public static final PacketType OPEN_WINDOW_MERCHANT =         new PacketType(PROTOCOL, SENDER, 0x2A, "OpenWindowMerchant");
			public static final PacketType REL_ENTITY_MOVE =              new PacketType(PROTOCOL, SENDER, 0x2B, "Entity$RelEntityMove", "Entity$PacketPlayOutRelEntityMove");
			public static final PacketType REL_ENTITY_MOVE_LOOK =         new PacketType(PROTOCOL, SENDER, 0x2C, "Entity$RelEntityMoveLook", "Entity$PacketPlayOutRelEntityMoveLook");
			public static final PacketType ENTITY_LOOK =                  new PacketType(PROTOCOL, SENDER, 0x2D, "Entity$EntityLook", "Entity$PacketPlayOutEntityLook");
			public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x2E, "VehicleMove", "SPacketMoveVehicle");
			public static final PacketType OPEN_BOOK =                    new PacketType(PROTOCOL, SENDER, 0x2F, "OpenBook");
			public static final PacketType OPEN_WINDOW =                  new PacketType(PROTOCOL, SENDER, 0x30, "OpenWindow", "SPacketOpenWindow");
			public static final PacketType OPEN_SIGN_EDITOR =             new PacketType(PROTOCOL, SENDER, 0x31, "OpenSignEditor", "SPacketSignEditorOpen");
			public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x32, "Ping");
			public static final PacketType AUTO_RECIPE =                  new PacketType(PROTOCOL, SENDER, 0x33, "AutoRecipe", "SPacketPlaceGhostRecipe");
			public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x34, "Abilities", "SPacketPlayerAbilities");
			public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x35, "PlayerChat", "Chat", "SPacketChat");
			public static final PacketType PLAYER_COMBAT_END =            new PacketType(PROTOCOL, SENDER, 0x36, "PlayerCombatEnd");
			public static final PacketType PLAYER_COMBAT_ENTER =          new PacketType(PROTOCOL, SENDER, 0x37, "PlayerCombatEnter");
			public static final PacketType PLAYER_COMBAT_KILL =           new PacketType(PROTOCOL, SENDER, 0x38, "PlayerCombatKill");
			public static final PacketType PLAYER_INFO_REMOVE =           new PacketType(PROTOCOL, SENDER, 0x39, "PlayerInfoRemove");
			public static final PacketType PLAYER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x3A, "PlayerInfoUpdate", "PlayerInfo");
			public static final PacketType LOOK_AT =                      new PacketType(PROTOCOL, SENDER, 0x3B, "LookAt", "SPacketPlayerPosLook");
			public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x3C, "Position");
			public static final PacketType RECIPES =                      new PacketType(PROTOCOL, SENDER, 0x3D, "Recipes", "SPacketRecipeBook");
			public static final PacketType ENTITY_DESTROY =               new PacketType(PROTOCOL, SENDER, 0x3E, "EntityDestroy", "SPacketDestroyEntities");
			public static final PacketType REMOVE_ENTITY_EFFECT =         new PacketType(PROTOCOL, SENDER, 0x3F, "RemoveEntityEffect", "SPacketRemoveEntityEffect");
			public static final PacketType RESOURCE_PACK_SEND =           new PacketType(PROTOCOL, SENDER, 0x40, "ResourcePackSend", "SPacketResourcePackSend");
			public static final PacketType RESPAWN =                      new PacketType(PROTOCOL, SENDER, 0x41, "Respawn", "SPacketRespawn");
			public static final PacketType ENTITY_HEAD_ROTATION =         new PacketType(PROTOCOL, SENDER, 0x42, "EntityHeadRotation", "SPacketEntityHeadLook");
			public static final PacketType MULTI_BLOCK_CHANGE =           new PacketType(PROTOCOL, SENDER, 0x43, "MultiBlockChange", "SPacketMultiBlockChange");
			public static final PacketType SELECT_ADVANCEMENT_TAB =       new PacketType(PROTOCOL, SENDER, 0x44, "SelectAdvancementTab", "SPacketSelectAdvancementsTab");
			public static final PacketType SERVER_DATA =                  new PacketType(PROTOCOL, SENDER, 0x45, "ServerData");
			public static final PacketType SET_ACTION_BAR_TEXT =          new PacketType(PROTOCOL, SENDER, 0x46, "SetActionBarText");
			public static final PacketType SET_BORDER_CENTER =            new PacketType(PROTOCOL, SENDER, 0x47, "SetBorderCenter");
			public static final PacketType SET_BORDER_LERP_SIZE =         new PacketType(PROTOCOL, SENDER, 0x48, "SetBorderLerpSize");
			public static final PacketType SET_BORDER_SIZE =              new PacketType(PROTOCOL, SENDER, 0x49, "SetBorderSize");
			public static final PacketType SET_BORDER_WARNING_DELAY =     new PacketType(PROTOCOL, SENDER, 0x4A, "SetBorderWarningDelay");
			public static final PacketType SET_BORDER_WARNING_DISTANCE =  new PacketType(PROTOCOL, SENDER, 0x4B, "SetBorderWarningDistance");
			public static final PacketType CAMERA =                       new PacketType(PROTOCOL, SENDER, 0x4C, "Camera", "SPacketCamera");
			public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x4D, "HeldItemSlot", "SPacketHeldItemChange");
			public static final PacketType VIEW_CENTRE =                  new PacketType(PROTOCOL, SENDER, 0x4E, "ViewCentre");
			public static final PacketType VIEW_DISTANCE =                new PacketType(PROTOCOL, SENDER, 0x4F, "ViewDistance");
			public static final PacketType SPAWN_POSITION =               new PacketType(PROTOCOL, SENDER, 0x50, "SpawnPosition", "SPacketSpawnPosition");
			public static final PacketType SCOREBOARD_DISPLAY_OBJECTIVE = new PacketType(PROTOCOL, SENDER, 0x51, "ScoreboardDisplayObjective", "SPacketDisplayObjective");
			public static final PacketType ENTITY_METADATA =              new PacketType(PROTOCOL, SENDER, 0x52, "EntityMetadata", "SPacketEntityMetadata");
			public static final PacketType ATTACH_ENTITY =                new PacketType(PROTOCOL, SENDER, 0x53, "AttachEntity", "SPacketEntityAttach");
			public static final PacketType ENTITY_VELOCITY =              new PacketType(PROTOCOL, SENDER, 0x54, "EntityVelocity", "SPacketEntityVelocity");
			public static final PacketType ENTITY_EQUIPMENT =             new PacketType(PROTOCOL, SENDER, 0x55, "EntityEquipment", "SPacketEntityEquipment");
			public static final PacketType EXPERIENCE =                   new PacketType(PROTOCOL, SENDER, 0x56, "Experience", "SPacketSetExperience");
			public static final PacketType UPDATE_HEALTH =                new PacketType(PROTOCOL, SENDER, 0x57, "UpdateHealth", "SPacketUpdateHealth");
			public static final PacketType SCOREBOARD_OBJECTIVE =         new PacketType(PROTOCOL, SENDER, 0x58, "ScoreboardObjective", "SPacketScoreboardObjective");
			public static final PacketType MOUNT =                        new PacketType(PROTOCOL, SENDER, 0x59, "Mount", "SPacketSetPassengers");
			public static final PacketType SCOREBOARD_TEAM =              new PacketType(PROTOCOL, SENDER, 0x5A, "ScoreboardTeam", "SPacketTeams");
			public static final PacketType SCOREBOARD_SCORE =             new PacketType(PROTOCOL, SENDER, 0x5B, "ScoreboardScore", "SPacketUpdateScore");
			public static final PacketType UPDATE_SIMULATION_DISTANCE =   new PacketType(PROTOCOL, SENDER, 0x5C, "SetSimulationDistance");
			public static final PacketType SET_SUBTITLE_TEXT =            new PacketType(PROTOCOL, SENDER, 0x5D, "SetSubtitleText");
			public static final PacketType UPDATE_TIME =                  new PacketType(PROTOCOL, SENDER, 0x5E, "UpdateTime", "SPacketTimeUpdate");
			public static final PacketType SET_TITLE_TEXT =               new PacketType(PROTOCOL, SENDER, 0x5F, "SetTitleText");
			public static final PacketType SET_TITLES_ANIMATION =         new PacketType(PROTOCOL, SENDER, 0x60, "SetTitlesAnimation");
			public static final PacketType ENTITY_SOUND =                 new PacketType(PROTOCOL, SENDER, 0x61, "EntitySound", "SPacketSoundEffect");
			public static final PacketType NAMED_SOUND_EFFECT =           new PacketType(PROTOCOL, SENDER, 0x62, "NamedSoundEffect");
			public static final PacketType STOP_SOUND =                   new PacketType(PROTOCOL, SENDER, 0x63, "StopSound");
			public static final PacketType SYSTEM_CHAT =                  new PacketType(PROTOCOL, SENDER, 0x64, "SystemChat");
			public static final PacketType PLAYER_LIST_HEADER_FOOTER =    new PacketType(PROTOCOL, SENDER, 0x65, "PlayerListHeaderFooter", "SPacketPlayerListHeaderFooter");
			public static final PacketType NBT_QUERY =                    new PacketType(PROTOCOL, SENDER, 0x66, "NBTQuery");
			public static final PacketType COLLECT =                      new PacketType(PROTOCOL, SENDER, 0x67, "Collect", "SPacketCollectItem");
			public static final PacketType ENTITY_TELEPORT =              new PacketType(PROTOCOL, SENDER, 0x68, "EntityTeleport", "SPacketEntityTeleport");
			public static final PacketType ADVANCEMENTS =                 new PacketType(PROTOCOL, SENDER, 0x69, "Advancements", "SPacketAdvancementInfo");
			public static final PacketType UPDATE_ATTRIBUTES =            new PacketType(PROTOCOL, SENDER, 0x6A, "UpdateAttributes", "SPacketEntityProperties");
			public static final PacketType UPDATE_ENABLED_FEATURES =      new PacketType(PROTOCOL, SENDER, 0x6B, "UpdateEnabledFeatures");
			public static final PacketType ENTITY_EFFECT =                new PacketType(PROTOCOL, SENDER, 0x6C, "EntityEffect", "SPacketEntityEffect");
			public static final PacketType RECIPE_UPDATE =                new PacketType(PROTOCOL, SENDER, 0x6D, "RecipeUpdate");
			public static final PacketType TAGS =                         new PacketType(PROTOCOL, SENDER, 0x6E, "Tags");

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
			public static final PacketType BED =                          new PacketType(PROTOCOL, SENDER, 251, "Bed", "SPacketUseBed");

			/**
			 * @deprecated Renamed to {@link #BED}
			 */
			@Deprecated
			public static final PacketType USE_BED =                      BED.clone();

			/**
			 * @deprecated Removed in 1.16
			 */
			@Deprecated
			public static final PacketType SPAWN_ENTITY_WEATHER =         new PacketType(PROTOCOL, SENDER, 250, "SpawnEntityWeather", "SPacketSpawnGlobalEntity");

			/**
			 * @deprecated Removed in 1.17, split into separate packets
			 */
			@Deprecated
			public static final PacketType TITLE = new PacketType(PROTOCOL, SENDER, 249, "Title");

			/**
			 * @deprecated Removed in 1.17, split into separate packets
			 */
			@Deprecated
			public static final PacketType WORLD_BORDER = new PacketType(PROTOCOL, SENDER, 248, "WorldBorder");

			/**
			 * @deprecated Removed in 1.17, split into separate packets
			 */
			@Deprecated
			public static final PacketType COMBAT_EVENT = new PacketType(PROTOCOL, SENDER, 247, "CombatEvent");

			/**
			 * @deprecated Removed in 1.17
			 */
			@Deprecated
			public static final PacketType TRANSACTION = new PacketType(PROTOCOL, SENDER, 246, "Transaction", "SPacketConfirmTransaction");

			/**
			 * @deprecated Made abstract in 1.17, no actual packet anymore
			 */
			@Deprecated
			public static final PacketType ENTITY = new PacketType(PROTOCOL, SENDER, 245, "Entity", "SPacketEntity");

			/**
			 * @deprecated Removed in 1.19
			 */
			@Deprecated
			public static final PacketType SPAWN_ENTITY_LIVING = new PacketType(PROTOCOL, SENDER, 244, "SpawnEntityLiving", "SPacketSpawnMob");

			/**
			 * @deprecated Removed in 1.19
			 */
			@Deprecated
			public static final PacketType SPAWN_ENTITY_PAINTING = new PacketType(PROTOCOL, SENDER, 243, "SpawnEntityPainting", "SPacketSpawnPainting");

			/**
			 * @deprecated Removed in 1.19
			 */
			@Deprecated
			public static final PacketType ADD_VIBRATION_SIGNAL = new PacketType(PROTOCOL, SENDER, 242, "AddVibrationSignal");

			/**
			 * @deprecated Removed in 1.19
			 */
			@Deprecated
			public static final PacketType BLOCK_BREAK = new PacketType(PROTOCOL, SENDER, 241, "BlockBreak");

			/**
			 * @deprecated Removed in 1.19.3
			 */
			@Deprecated
			public static final PacketType CHAT_PREVIEW =                 new PacketType(PROTOCOL, SENDER, 0x0C, "ChatPreview");

			/**
			 * @deprecated Removed in 1.19.3
			 */
			@Deprecated
			public static final PacketType PLAYER_CHAT_HEADER =           new PacketType(PROTOCOL, SENDER, 0x32, "PlayerChatHeader");

			/**
			 * @deprecated Removed in 1.19.3
			 */
			@Deprecated
			public static final PacketType SET_DISPLAY_CHAT_PREVIEW =     new PacketType(PROTOCOL, SENDER, 0x4E, "SetDisplayChatPreview");

			/**
			 * @deprecated Removed in 1.19.3
			 */
			@Deprecated
			public static final PacketType CUSTOM_SOUND_EFFECT =          new PacketType(PROTOCOL, SENDER, 0x16, "CustomSoundEffect", "SPacketCustomSound");

			private static final Server INSTANCE = new Server();

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
			private static final Sender SENDER = Sender.CLIENT;

			public static final PacketType TELEPORT_ACCEPT =              new PacketType(PROTOCOL, SENDER, 0x00, "TeleportAccept", "CPacketConfirmTeleport");
			public static final PacketType TILE_NBT_QUERY =               new PacketType(PROTOCOL, SENDER, 0x01, "TileNBTQuery");
			public static final PacketType DIFFICULTY_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x02, "DifficultyChange");
			public static final PacketType CHAT_ACK =                     new PacketType(PROTOCOL, SENDER, 0x03, "ChatAck");
			public static final PacketType CHAT_COMMAND =                 new PacketType(PROTOCOL, SENDER, 0x04, "ChatCommand");
			public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x05, "Chat", "CPacketChatMessage");
			public static final PacketType CHAT_SESSION_UPDATE =          new PacketType(PROTOCOL, SENDER, 0x06, "ChatSessionUpdate");
			public static final PacketType CLIENT_COMMAND =               new PacketType(PROTOCOL, SENDER, 0x07, "ClientCommand", "CPacketClientStatus");
			public static final PacketType SETTINGS =                     new PacketType(PROTOCOL, SENDER, 0x08, "Settings", "CPacketClientSettings");
			public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x09, "TabComplete", "CPacketTabComplete");
			public static final PacketType ENCHANT_ITEM =                 new PacketType(PROTOCOL, SENDER, 0x0A, "EnchantItem", "CPacketEnchantItem");
			public static final PacketType WINDOW_CLICK =                 new PacketType(PROTOCOL, SENDER, 0x0B, "WindowClick", "CPacketClickWindow");
			public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x0C, "CloseWindow", "CPacketCloseWindow");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x0D, "CustomPayload", "CPacketCustomPayload");
			public static final PacketType B_EDIT =                       new PacketType(PROTOCOL, SENDER, 0x0E, "BEdit");
			public static final PacketType ENTITY_NBT_QUERY =             new PacketType(PROTOCOL, SENDER, 0x0F, "EntityNBTQuery");
			public static final PacketType USE_ENTITY =                   new PacketType(PROTOCOL, SENDER, 0x10, "UseEntity", "CPacketUseEntity");
			public static final PacketType JIGSAW_GENERATE =              new PacketType(PROTOCOL, SENDER, 0x11, "JigsawGenerate");
			public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x12, "KeepAlive", "CPacketKeepAlive");
			public static final PacketType DIFFICULTY_LOCK =              new PacketType(PROTOCOL, SENDER, 0x13, "DifficultyLock");
			public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x14, "Flying$Position", "Flying$PacketPlayInPosition", "CPacketPlayer$Position");
			public static final PacketType POSITION_LOOK =                new PacketType(PROTOCOL, SENDER, 0x15, "Flying$PositionLook", "Flying$PacketPlayInPositionLook", "CPacketPlayer$PositionRotation");
			public static final PacketType LOOK =                         new PacketType(PROTOCOL, SENDER, 0x16, "Flying$Look", "Flying$PacketPlayInLook", "CPacketPlayer$Rotation");
			public static final PacketType GROUND =                       new PacketType(PROTOCOL, SENDER, 0x17, "Flying$d");
			public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x18, "VehicleMove", "CPacketVehicleMove");
			public static final PacketType BOAT_MOVE =                    new PacketType(PROTOCOL, SENDER, 0x19, "BoatMove", "CPacketSteerBoat");
			public static final PacketType PICK_ITEM =                    new PacketType(PROTOCOL, SENDER, 0x1A, "PickItem");
			public static final PacketType AUTO_RECIPE =                  new PacketType(PROTOCOL, SENDER, 0x1B, "AutoRecipe", "CPacketPlaceRecipe");
			public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x1C, "Abilities", "CPacketPlayerAbilities");
			public static final PacketType BLOCK_DIG =                    new PacketType(PROTOCOL, SENDER, 0x1D, "BlockDig", "CPacketPlayerDigging");
			public static final PacketType ENTITY_ACTION =                new PacketType(PROTOCOL, SENDER, 0x1E, "EntityAction", "CPacketEntityAction");
			public static final PacketType STEER_VEHICLE =                new PacketType(PROTOCOL, SENDER, 0x1F, "SteerVehicle", "CPacketInput");
			public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x20, "Pong");
			public static final PacketType RECIPE_SETTINGS =              new PacketType(PROTOCOL, SENDER, 0x21, "RecipeSettings");
			public static final PacketType RECIPE_DISPLAYED =             new PacketType(PROTOCOL, SENDER, 0x22, "RecipeDisplayed", "CPacketRecipeInfo");
			public static final PacketType ITEM_NAME =                    new PacketType(PROTOCOL, SENDER, 0x23, "ItemName");
			public static final PacketType RESOURCE_PACK_STATUS =         new PacketType(PROTOCOL, SENDER, 0x24, "ResourcePackStatus", "CPacketResourcePackStatus");
			public static final PacketType ADVANCEMENTS =                 new PacketType(PROTOCOL, SENDER, 0x25, "Advancements", "CPacketSeenAdvancements");
			public static final PacketType TR_SEL =                       new PacketType(PROTOCOL, SENDER, 0x26, "TrSel");
			public static final PacketType BEACON =                       new PacketType(PROTOCOL, SENDER, 0x27, "Beacon");
			public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x28, "HeldItemSlot", "CPacketHeldItemChange");
			public static final PacketType SET_COMMAND_BLOCK =            new PacketType(PROTOCOL, SENDER, 0x29, "SetCommandBlock");
			public static final PacketType SET_COMMAND_MINECART =         new PacketType(PROTOCOL, SENDER, 0x2A, "SetCommandMinecart");
			public static final PacketType SET_CREATIVE_SLOT =            new PacketType(PROTOCOL, SENDER, 0x2B, "SetCreativeSlot", "CPacketCreativeInventoryAction");
			public static final PacketType SET_JIGSAW =                   new PacketType(PROTOCOL, SENDER, 0x2C, "SetJigsaw");
			public static final PacketType STRUCT =                       new PacketType(PROTOCOL, SENDER, 0x2D, "Struct");
			public static final PacketType UPDATE_SIGN =                  new PacketType(PROTOCOL, SENDER, 0x2E, "UpdateSign", "CPacketUpdateSign");
			public static final PacketType ARM_ANIMATION =                new PacketType(PROTOCOL, SENDER, 0x2F, "ArmAnimation", "CPacketAnimation");
			public static final PacketType SPECTATE =                     new PacketType(PROTOCOL, SENDER, 0x30, "Spectate", "CPacketSpectate");
			public static final PacketType USE_ITEM =                     new PacketType(PROTOCOL, SENDER, 0x31, "UseItem", "CPacketPlayerTryUseItemOnBlock");
			public static final PacketType BLOCK_PLACE =                  new PacketType(PROTOCOL, SENDER, 0x32, "BlockPlace", "CPacketPlayerTryUseItem");

			/**
			 * @deprecated Removed in 1.17
			 */
			@Deprecated
			public static final PacketType TRANSACTION =                  new PacketType(PROTOCOL, SENDER, 255, "Transaction", "CPacketConfirmTransaction");

			/**
			 * @deprecated Removed in 1.17
			 */
			@Deprecated
			public static final PacketType FLYING = 					  new PacketType(PROTOCOL, SENDER, 254, "Flying", "CPacketPlayer");

			/**
			 * @deprecated Removed in 1.19.3
			 */
			@Deprecated
			public static final PacketType CHAT_PREVIEW =                 new PacketType(PROTOCOL, SENDER, 0x06, "ChatPreview");

			private static final Client INSTANCE = new Client();

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
		static final Protocol PROTOCOL = Protocol.STATUS;

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		public static class Server extends PacketTypeEnum {
			private static final Sender SENDER = Sender.SERVER;

			@ForceAsync
			public static final PacketType SERVER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x00, "ServerInfo", "SPacketServerInfo");
			@ForceAsync
			public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x01, "Pong", "SPacketPong");

			/**
			 * @deprecated Renamed to {@link #SERVER_INFO}
			 */
			@Deprecated
			@ForceAsync
			public static final PacketType OUT_SERVER_INFO =              SERVER_INFO.clone();

			private static final Server INSTANCE = new Server();

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
			private static final Sender SENDER = Sender.CLIENT;

			public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, "Start", "CPacketServerQuery");
			@ForceAsync
			public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x01, "Ping", "CPacketPing");

			private static final Client INSTANCE = new Client();

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
		static final Protocol PROTOCOL = Protocol.LOGIN;

		/**
		 * Outgoing packets.
		 * @author Kristian
		 */
		public static class Server extends PacketTypeEnum {
			private static final Sender SENDER = Sender.SERVER;

			@ForceAsync
			public static final PacketType DISCONNECT =                   new PacketType(PROTOCOL, SENDER, 0x00, "Disconnect", "SPacketDisconnect");
			public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, "EncryptionBegin", "SPacketEncryptionRequest");
			public static final PacketType SUCCESS =                      new PacketType(PROTOCOL, SENDER, 0x02, "Success", "SPacketLoginSuccess");
			public static final PacketType SET_COMPRESSION =              new PacketType(PROTOCOL, SENDER, 0x03, "SetCompression", "SPacketEnableCompression");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x04, "CustomPayload", "SPacketCustomPayload");

			private static final Server INSTANCE = new Server();

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
			private static final Sender SENDER = Sender.CLIENT;

			public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, "Start", "CPacketLoginStart");
			public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, "EncryptionBegin", "CPacketEncryptionResponse");
			public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x02, "CustomPayload", "CPacketCustomPayload");

			private static final Client INSTANCE = new Client();

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
		CLIENT("Serverbound", "In", "client"),

		/**
		 * Indicate that packets of this type will be sent by the current server.
		 */
		SERVER("Clientbound", "Out", "server");

		private String mojangName;
		private String packetName;
		private String mcpName;

		Sender(String mojangName, String packetName, String mcpName) {
			this.mojangName = mojangName;
			this.packetName = packetName;
			this.mcpName = mcpName;
		}

		/**
		 * Retrieve the equivialent connection side.
		 * @return The connection side.
		 */
		public ConnectionSide toSide() {
			return this == CLIENT ? ConnectionSide.CLIENT_SIDE : ConnectionSide.SERVER_SIDE;
		}

		public String getPacketName() {
			return packetName;
		}

		public String getMcpPacketName() {
			return mcpName;
		}

		public String getMojangName() {
			return mojangName;
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
	private static final MinecraftVersion PROTOCOL_VERSION = MinecraftVersion.LATEST;

	private final Protocol protocol;
	private final Sender sender;
	private final int currentId;
	private final MinecraftVersion version;
	private final List<String> classNames;
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
		final List<Iterable<? extends PacketType>> sources = new ArrayList<>();

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

	private static String formatMojangClassName(Protocol protocol, Sender sender, String name) {
		return "net.minecraft.network.protocol." + protocol.getMojangName() + "." + sender.getMojangName()
				+ name + "Packet";
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
			List<String> aliases = check.getClassNames();
			if (aliases.size() > 1) {
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
	 * @deprecated Doesn't really have a purpose
	 */
	@Deprecated
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
		
		this.classNames = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			if (isMcpPacketName(names[i])) { // Minecraft MCP packets
				classNames.add(formatMcpClassName(protocol, sender, names[i]));
			} else {
				classNames.add(formatClassName(protocol, sender, names[i]));
				classNames.add(formatMojangClassName(protocol, sender, names[i]));
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

	public List<String> getClassNames() {
		return classNames;
	}

	/**
	 * Retrieve the equivalent packet class.
	 * @return The packet class, or NULL if not found.
	 */
	public Class<?> getPacketClass() {
		return PacketRegistry.tryGetPacketClass(this).orElse(null);
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
			return name() + "[" + protocol + ", " + sender + ", " + currentId + ", classNames: " + classNames + " (unregistered)]";
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
