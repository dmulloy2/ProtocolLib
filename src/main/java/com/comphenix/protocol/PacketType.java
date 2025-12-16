package com.comphenix.protocol;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;

import com.comphenix.protocol.PacketTypeLookup.ClassLookup;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.scheduler.UniversalRunnable;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
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
            public static final PacketType SET_PROTOCOL =                 new PacketType(PROTOCOL, SENDER, 0x00, "ClientIntentionPacket", "SetProtocol", "C00Handshake");

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

            public static final PacketType BUNDLE =                       new PacketType(PROTOCOL, SENDER, 0x00, "BundleDelimiter", "Delimiter", "BundleDelimiterPacket");
            public static final PacketType SPAWN_ENTITY =                 new PacketType(PROTOCOL, SENDER, 0x01, "AddEntity", "SpawnEntity", "SPacketSpawnObject");
            public static final PacketType ANIMATION =                    new PacketType(PROTOCOL, SENDER, 0x02, "Animate", "Animation", "SPacketAnimation");
            public static final PacketType STATISTIC =                    new PacketType(PROTOCOL, SENDER, 0x03, "AwardStats", "Statistic", "SPacketStatistics");
            public static final PacketType BLOCK_CHANGED_ACK =            new PacketType(PROTOCOL, SENDER, 0x04, "BlockChangedAck");
            public static final PacketType BLOCK_BREAK_ANIMATION =        new PacketType(PROTOCOL, SENDER, 0x05, "BlockDestruction", "BlockBreakAnimation", "SPacketBlockBreakAnim");
            public static final PacketType TILE_ENTITY_DATA =             new PacketType(PROTOCOL, SENDER, 0x06, "BlockEntityData", "TileEntityData", "SPacketUpdateTileEntity");
            public static final PacketType BLOCK_ACTION =                 new PacketType(PROTOCOL, SENDER, 0x07, "BlockEvent", "BlockAction", "SPacketBlockAction");
            public static final PacketType BLOCK_CHANGE =                 new PacketType(PROTOCOL, SENDER, 0x08, "BlockUpdate", "BlockChange", "SPacketBlockChange");
            public static final PacketType BOSS =                         new PacketType(PROTOCOL, SENDER, 0x09, "BossEvent", "Boss", "SPacketUpdateBossInfo");
            public static final PacketType SERVER_DIFFICULTY =            new PacketType(PROTOCOL, SENDER, 0x0A, "ChangeDifficulty", "ServerDifficulty", "SPacketServerDifficulty");
            public static final PacketType CHUNK_BATCH_FINISHED =         new PacketType(PROTOCOL, SENDER, 0x0B, "ChunkBatchFinished");
            public static final PacketType CHUNK_BATCH_START =            new PacketType(PROTOCOL, SENDER, 0x0C, "ChunkBatchStart");
            public static final PacketType CHUNKS_BIOMES =                new PacketType(PROTOCOL, SENDER, 0x0D, "ChunksBiomes", "ClientboundChunksBiomesPacket");
            public static final PacketType CLEAR_TITLES =                 new PacketType(PROTOCOL, SENDER, 0x0E, "ClearTitles");
            public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x0F, "CommandSuggestions", "TabComplete", "SPacketTabComplete");
            public static final PacketType COMMANDS =                     new PacketType(PROTOCOL, SENDER, 0x10, "Commands");
            public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x11, "ContainerClose", "CloseWindow", "SPacketCloseWindow");
            public static final PacketType WINDOW_ITEMS =                 new PacketType(PROTOCOL, SENDER, 0x12, "ContainerSetContent", "WindowItems", "SPacketWindowItems");
            public static final PacketType WINDOW_DATA =                  new PacketType(PROTOCOL, SENDER, 0x13, "ContainerSetData", "WindowData", "SPacketWindowProperty");
            public static final PacketType SET_SLOT =                     new PacketType(PROTOCOL, SENDER, 0x14, "ContainerSetSlot", "SetSlot", "SPacketSetSlot");
            public static final PacketType COOKIE_REQUEST =               new PacketType(PROTOCOL, SENDER, 0x15, "CookieRequest");
            public static final PacketType SET_COOLDOWN =                 new PacketType(PROTOCOL, SENDER, 0x16, "Cooldown", "SetCooldown", "SPacketCooldown");
            public static final PacketType CUSTOM_CHAT_COMPLETIONS =      new PacketType(PROTOCOL, SENDER, 0x17, "CustomChatCompletions");
            public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x18, "CustomPayload", "SPacketCustomPayload");
            public static final PacketType DAMAGE_EVENT =                 new PacketType(PROTOCOL, SENDER, 0x19, "DamageEvent", "ClientboundDamageEventPacket");
            public static final PacketType DEBUG_BLOCK_VALUE =            new PacketType(PROTOCOL, SENDER, 0x1A, "DebugBlockValue");
            public static final PacketType DEBUG_CHUNK_VALUE =            new PacketType(PROTOCOL, SENDER, 0x1B, "DebugChunkValue");
            public static final PacketType DEBUG_ENTITY_VALUE =           new PacketType(PROTOCOL, SENDER, 0x1C, "DebugEntityValue");
            public static final PacketType DEBUG_EVENT =                  new PacketType(PROTOCOL, SENDER, 0x1D, "DebugEvent");
            public static final PacketType DEBUG_SAMPLE =                 new PacketType(PROTOCOL, SENDER, 0x1E, "DebugSample");
            public static final PacketType DELETE_CHAT_MESSAGE =          new PacketType(PROTOCOL, SENDER, 0x1F, "DeleteChat");
            public static final PacketType KICK_DISCONNECT =              new PacketType(PROTOCOL, SENDER, 0x20, "Disconnect", "KickDisconnect", "SPacketDisconnect");
            public static final PacketType DISGUISED_CHAT =               new PacketType(PROTOCOL, SENDER, 0x21, "DisguisedChat");
            public static final PacketType ENTITY_STATUS =                new PacketType(PROTOCOL, SENDER, 0x22, "EntityEvent", "EntityStatus", "SPacketEntityStatus");
            public static final PacketType ENTITY_POSITION_SYNC =         new PacketType(PROTOCOL, SENDER, 0x23, "EntityPositionSync");
            public static final PacketType EXPLOSION =                    new PacketType(PROTOCOL, SENDER, 0x24, "Explode", "Explosion", "SPacketExplosion");
            public static final PacketType UNLOAD_CHUNK =                 new PacketType(PROTOCOL, SENDER, 0x25, "ForgetLevelChunk", "UnloadChunk", "SPacketUnloadChunk");
            public static final PacketType GAME_STATE_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x26, "GameEvent", "GameStateChange", "SPacketChangeGameState");
            public static final PacketType GAME_TEST_HIGHLIGHT_POS =      new PacketType(PROTOCOL, SENDER, 0x27, "GameTestHighlightPos");
            public static final PacketType OPEN_WINDOW_HORSE =            new PacketType(PROTOCOL, SENDER, 0x28, "MountScreenOpen", "HorseScreenOpen", "OpenWindowHorse");
            public static final PacketType HURT_ANIMATION =               new PacketType(PROTOCOL, SENDER, 0x29, "HurtAnimation", "ClientboundHurtAnimationPacket");
            public static final PacketType INITIALIZE_BORDER =            new PacketType(PROTOCOL, SENDER, 0x2A, "InitializeBorder");
            public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x2B, "KeepAlive", "SPacketKeepAlive");
            public static final PacketType MAP_CHUNK =                    new PacketType(PROTOCOL, SENDER, 0x2C, "LevelChunkWithLight", "MapChunk", "SPacketChunkData");
            public static final PacketType WORLD_EVENT =                  new PacketType(PROTOCOL, SENDER, 0x2D, "LevelEvent", "WorldEvent", "SPacketEffect");
            public static final PacketType WORLD_PARTICLES =              new PacketType(PROTOCOL, SENDER, 0x2E, "LevelParticles", "WorldParticles", "SPacketParticles");
            public static final PacketType LIGHT_UPDATE =                 new PacketType(PROTOCOL, SENDER, 0x2F, "LightUpdate");
            public static final PacketType LOGIN =                        new PacketType(PROTOCOL, SENDER, 0x30, "Login", "SPacketJoinGame");
            public static final PacketType MAP =                          new PacketType(PROTOCOL, SENDER, 0x31, "MapItemData", "Map", "SPacketMaps");
            public static final PacketType OPEN_WINDOW_MERCHANT =         new PacketType(PROTOCOL, SENDER, 0x32, "MerchantOffers", "OpenWindowMerchant");
            public static final PacketType REL_ENTITY_MOVE =              new PacketType(PROTOCOL, SENDER, 0x33, "net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Pos", "Entity$RelEntityMove", "Entity$PacketPlayOutRelEntityMove");
            public static final PacketType REL_ENTITY_MOVE_LOOK =         new PacketType(PROTOCOL, SENDER, 0x34, "net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$PosRot", "Entity$RelEntityMoveLook", "Entity$PacketPlayOutRelEntityMoveLook");
            public static final PacketType MOVE_MINECART =                new PacketType(PROTOCOL, SENDER, 0x35, "MoveMinecart");
            public static final PacketType ENTITY_LOOK =                  new PacketType(PROTOCOL, SENDER, 0x36, "net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Rot", "Entity$EntityLook", "Entity$PacketPlayOutEntityLook");
            public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x37, "MoveVehicle", "VehicleMove", "SPacketMoveVehicle");
            public static final PacketType OPEN_BOOK =                    new PacketType(PROTOCOL, SENDER, 0x38, "OpenBook");
            public static final PacketType OPEN_WINDOW =                  new PacketType(PROTOCOL, SENDER, 0x39, "OpenScreen", "OpenWindow", "SPacketOpenWindow");
            public static final PacketType OPEN_SIGN_EDITOR =             new PacketType(PROTOCOL, SENDER, 0x3A, "OpenSignEditor", "SPacketSignEditorOpen");
            public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x3B, "Ping");
            public static final PacketType PONG_RESPONSE =                new PacketType(PROTOCOL, SENDER, 0x3C, "PongResponse");
            public static final PacketType AUTO_RECIPE =                  new PacketType(PROTOCOL, SENDER, 0x3D, "PlaceGhostRecipe", "AutoRecipe", "SPacketPlaceGhostRecipe");
            public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x3E, "PlayerAbilities", "Abilities", "SPacketPlayerAbilities");
            public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x3F, "PlayerChat", "Chat", "SPacketChat");
            public static final PacketType PLAYER_COMBAT_END =            new PacketType(PROTOCOL, SENDER, 0x40, "PlayerCombatEnd");
            public static final PacketType PLAYER_COMBAT_ENTER =          new PacketType(PROTOCOL, SENDER, 0x41, "PlayerCombatEnter");
            public static final PacketType PLAYER_COMBAT_KILL =           new PacketType(PROTOCOL, SENDER, 0x42, "PlayerCombatKill");
            public static final PacketType PLAYER_INFO_REMOVE =           new PacketType(PROTOCOL, SENDER, 0x43, "PlayerInfoRemove");
            public static final PacketType PLAYER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x44, "PlayerInfoUpdate", "PlayerInfo");
            public static final PacketType LOOK_AT =                      new PacketType(PROTOCOL, SENDER, 0x45, "PlayerLookAt", "LookAt", "SPacketPlayerPosLook");
            public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x46, "PlayerPosition", "Position");
            public static final PacketType PLAYER_ROTATION =              new PacketType(PROTOCOL, SENDER, 0x47, "PlayerRotation");
            public static final PacketType RECIPE_BOOK_ADD =              new PacketType(PROTOCOL, SENDER, 0x48, "RecipeBookAdd");
            public static final PacketType RECIPE_BOOK_REMOVE =           new PacketType(PROTOCOL, SENDER, 0x49, "RecipeBookRemove");
            public static final PacketType RECIPE_BOOK_SETTINGS =         new PacketType(PROTOCOL, SENDER, 0x4A, "RecipeBookSettings");
            public static final PacketType ENTITY_DESTROY =               new PacketType(PROTOCOL, SENDER, 0x4B, "RemoveEntities", "EntityDestroy", "SPacketDestroyEntities");
            public static final PacketType REMOVE_ENTITY_EFFECT =         new PacketType(PROTOCOL, SENDER, 0x4C, "RemoveMobEffect", "RemoveEntityEffect", "SPacketRemoveEntityEffect");
            public static final PacketType RESET_SCORE =                  new PacketType(PROTOCOL, SENDER, 0x4D, "ResetScore", "ResetScorePacket");
            public static final PacketType REMOVE_RESOURCE_PACK =         new PacketType(PROTOCOL, SENDER, 0x4E, "ResourcePackPop", "ResourcePackPopPacket");
            public static final PacketType ADD_RESOURCE_PACK =            new PacketType(PROTOCOL, SENDER, 0x4F, "ResourcePackPush", "ResourcePackPushPacket");
            public static final PacketType RESPAWN =                      new PacketType(PROTOCOL, SENDER, 0x50, "Respawn", "SPacketRespawn");
            public static final PacketType ENTITY_HEAD_ROTATION =         new PacketType(PROTOCOL, SENDER, 0x51, "RotateHead", "EntityHeadRotation", "SPacketEntityHeadLook");
            public static final PacketType MULTI_BLOCK_CHANGE =           new PacketType(PROTOCOL, SENDER, 0x52, "SectionBlocksUpdate", "MultiBlockChange", "SPacketMultiBlockChange");
            public static final PacketType SELECT_ADVANCEMENT_TAB =       new PacketType(PROTOCOL, SENDER, 0x53, "SelectAdvancementsTab", "SelectAdvancementTab", "SPacketSelectAdvancementsTab");
            public static final PacketType SERVER_DATA =                  new PacketType(PROTOCOL, SENDER, 0x54, "ServerData");
            public static final PacketType SET_ACTION_BAR_TEXT =          new PacketType(PROTOCOL, SENDER, 0x55, "SetActionBarText");
            public static final PacketType SET_BORDER_CENTER =            new PacketType(PROTOCOL, SENDER, 0x56, "SetBorderCenter");
            public static final PacketType SET_BORDER_LERP_SIZE =         new PacketType(PROTOCOL, SENDER, 0x57, "SetBorderLerpSize");
            public static final PacketType SET_BORDER_SIZE =              new PacketType(PROTOCOL, SENDER, 0x58, "SetBorderSize");
            public static final PacketType SET_BORDER_WARNING_DELAY =     new PacketType(PROTOCOL, SENDER, 0x59, "SetBorderWarningDelay");
            public static final PacketType SET_BORDER_WARNING_DISTANCE =  new PacketType(PROTOCOL, SENDER, 0x5A, "SetBorderWarningDistance");
            public static final PacketType CAMERA =                       new PacketType(PROTOCOL, SENDER, 0x5B, "SetCamera", "Camera", "SPacketCamera");
            public static final PacketType VIEW_CENTRE =                  new PacketType(PROTOCOL, SENDER, 0x5C, "SetChunkCacheCenter", "ViewCentre");
            public static final PacketType VIEW_DISTANCE =                new PacketType(PROTOCOL, SENDER, 0x5D, "SetChunkCacheRadius", "ViewDistance");
            public static final PacketType SET_CURSOR_ITEM =              new PacketType(PROTOCOL, SENDER, 0x5E, "SetCursorItem");
            public static final PacketType SPAWN_POSITION =               new PacketType(PROTOCOL, SENDER, 0x5F, "SetDefaultSpawnPosition", "SpawnPosition", "SPacketSpawnPosition");
            public static final PacketType SCOREBOARD_DISPLAY_OBJECTIVE = new PacketType(PROTOCOL, SENDER, 0x60, "SetDisplayObjective", "ScoreboardDisplayObjective", "SPacketDisplayObjective");
            public static final PacketType ENTITY_METADATA =              new PacketType(PROTOCOL, SENDER, 0x61, "SetEntityData", "EntityMetadata", "SPacketEntityMetadata");
            public static final PacketType ATTACH_ENTITY =                new PacketType(PROTOCOL, SENDER, 0x62, "SetEntityLink", "AttachEntity", "SPacketEntityAttach");
            public static final PacketType ENTITY_VELOCITY =              new PacketType(PROTOCOL, SENDER, 0x63, "SetEntityMotion", "EntityVelocity", "SPacketEntityVelocity");
            public static final PacketType ENTITY_EQUIPMENT =             new PacketType(PROTOCOL, SENDER, 0x64, "SetEquipment", "EntityEquipment", "SPacketEntityEquipment");
            public static final PacketType EXPERIENCE =                   new PacketType(PROTOCOL, SENDER, 0x65, "SetExperience", "Experience", "SPacketSetExperience");
            public static final PacketType UPDATE_HEALTH =                new PacketType(PROTOCOL, SENDER, 0x66, "SetHealth", "UpdateHealth", "SPacketUpdateHealth");
            public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x67, "SetHeldSlot", "SetCarriedItem", "HeldItemSlot", "SPacketHeldItemChange");
            public static final PacketType SCOREBOARD_OBJECTIVE =         new PacketType(PROTOCOL, SENDER, 0x68, "SetObjective", "ScoreboardObjective", "SPacketScoreboardObjective");
            public static final PacketType MOUNT =                        new PacketType(PROTOCOL, SENDER, 0x69, "SetPassengers", "Mount", "SPacketSetPassengers");
            public static final PacketType SET_PLAYER_INVENTORY =         new PacketType(PROTOCOL, SENDER, 0x6A, "SetPlayerInventory");
            public static final PacketType SCOREBOARD_TEAM =              new PacketType(PROTOCOL, SENDER, 0x6B, "SetPlayerTeam", "ScoreboardTeam", "SPacketTeams");
            public static final PacketType SCOREBOARD_SCORE =             new PacketType(PROTOCOL, SENDER, 0x6C, "SetScore", "ScoreboardScore", "SPacketUpdateScore");
            public static final PacketType UPDATE_SIMULATION_DISTANCE =   new PacketType(PROTOCOL, SENDER, 0x6D, "SetSimulationDistance");
            public static final PacketType SET_SUBTITLE_TEXT =            new PacketType(PROTOCOL, SENDER, 0x6E, "SetSubtitleText");
            public static final PacketType UPDATE_TIME =                  new PacketType(PROTOCOL, SENDER, 0x6F, "SetTime", "UpdateTime", "SPacketTimeUpdate");
            public static final PacketType SET_TITLE_TEXT =               new PacketType(PROTOCOL, SENDER, 0x70, "SetTitleText");
            public static final PacketType SET_TITLES_ANIMATION =         new PacketType(PROTOCOL, SENDER, 0x71, "SetTitlesAnimation");
            public static final PacketType ENTITY_SOUND =                 new PacketType(PROTOCOL, SENDER, 0x72, "SoundEntity", "EntitySound", "SPacketSoundEffect");
            public static final PacketType NAMED_SOUND_EFFECT =           new PacketType(PROTOCOL, SENDER, 0x73, "Sound", "NamedSoundEffect");
            public static final PacketType START_CONFIGURATION =          new PacketType(PROTOCOL, SENDER, 0x74, "StartConfiguration");
            public static final PacketType STOP_SOUND =                   new PacketType(PROTOCOL, SENDER, 0x75, "StopSound");
            public static final PacketType STORE_COOKIE =                 new PacketType(PROTOCOL, SENDER, 0x76, "StoreCookie");
            public static final PacketType SYSTEM_CHAT =                  new PacketType(PROTOCOL, SENDER, 0x77, "SystemChat");
            public static final PacketType PLAYER_LIST_HEADER_FOOTER =    new PacketType(PROTOCOL, SENDER, 0x78, "TabList", "PlayerListHeaderFooter", "SPacketPlayerListHeaderFooter");
            public static final PacketType NBT_QUERY =                    new PacketType(PROTOCOL, SENDER, 0x79, "TagQuery", "NBTQuery");
            public static final PacketType COLLECT =                      new PacketType(PROTOCOL, SENDER, 0x7A, "TakeItemEntity", "Collect", "SPacketCollectItem");
            public static final PacketType ENTITY_TELEPORT =              new PacketType(PROTOCOL, SENDER, 0x7B, "TeleportEntity", "EntityTeleport", "SPacketEntityTeleport");
            public static final PacketType TEST_INSTANCE_BLOCK_STATUS =   new PacketType(PROTOCOL, SENDER, 0x7C, "ClientboundTestInstanceBlockStatus");
            public static final PacketType TICKING_STATE =                new PacketType(PROTOCOL, SENDER, 0x7D, "TickingState", "TickingStatePacket");
            public static final PacketType TICKING_STEP_STATE =           new PacketType(PROTOCOL, SENDER, 0x7E, "TickingStep", "TickingStepPacket");
            public static final PacketType TRANSFER =                     new PacketType(PROTOCOL, SENDER, 0x7F, "Transfer");
            public static final PacketType ADVANCEMENTS =                 new PacketType(PROTOCOL, SENDER, 0x80, "UpdateAdvancements", "Advancements", "SPacketAdvancementInfo");
            public static final PacketType UPDATE_ATTRIBUTES =            new PacketType(PROTOCOL, SENDER, 0x81, "UpdateAttributes", "SPacketEntityProperties");
            public static final PacketType ENTITY_EFFECT =                new PacketType(PROTOCOL, SENDER, 0x82, "UpdateMobEffect", "EntityEffect", "SPacketEntityEffect");
            public static final PacketType RECIPE_UPDATE =                new PacketType(PROTOCOL, SENDER, 0x83, "UpdateRecipes", "RecipeUpdate");
            public static final PacketType TAGS =                         new PacketType(PROTOCOL, SENDER, 0x84, "UpdateTags", "Tags");
            public static final PacketType PROJECTILE_POWER =             new PacketType(PROTOCOL, SENDER, 0x85, "ProjectilePower");
            public static final PacketType REPORT_DETAILS =               new PacketType(PROTOCOL, SENDER, 0x86, "CustomReportDetails");
            public static final PacketType SERVER_LINKS =                 new PacketType(PROTOCOL, SENDER, 0x87, "ServerLinks");
            public static final PacketType TRACKED_WAYPOINT =             new PacketType(PROTOCOL, SENDER, 0x88, "TrackedWaypoint");
            public static final PacketType CLEAR_DIALOG =                 new PacketType(PROTOCOL, SENDER, 0x89, "ClearDialog");
            public static final PacketType SHOW_DIALOG =                  new PacketType(PROTOCOL, SENDER, 0x8A, "ShowDialog");

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

            /**
             * @deprecated Replaced by {@link #TILE_ENTITY_DATA}
             */
            @Deprecated
            public static final PacketType UPDATE_SIGN =                  MinecraftReflection.signUpdateExists() ? new PacketType(PROTOCOL, SENDER, 252, "UpdateSign") :
                                                                              TILE_ENTITY_DATA.clone();

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
            public static final PacketType CHAT_PREVIEW =                 new PacketType(PROTOCOL, SENDER, 240, "ChatPreview");

            /**
             * @deprecated Removed in 1.19.3
             */
            @Deprecated
            public static final PacketType PLAYER_CHAT_HEADER =           new PacketType(PROTOCOL, SENDER, 239, "PlayerChatHeader");

            /**
             * @deprecated Removed in 1.19.3
             */
            @Deprecated
            public static final PacketType SET_DISPLAY_CHAT_PREVIEW =     new PacketType(PROTOCOL, SENDER, 238, "SetDisplayChatPreview");

            /**
             * @deprecated Removed in 1.19.3
             */
            @Deprecated
            public static final PacketType CUSTOM_SOUND_EFFECT =          new PacketType(PROTOCOL, SENDER, 237, "CustomSoundEffect", "SPacketCustomSound");

            /**
             * @deprecated Removed in 1.20.2: moved to configuration phase packets
             */
            @Deprecated
            public static final PacketType UPDATE_ENABLED_FEATURES =      new PacketType(PROTOCOL, SENDER, 236, "UpdateEnabledFeatures");

            /**
             * @deprecated Removed in 1.20.2
             */
            @Deprecated
            public static final PacketType NAMED_ENTITY_SPAWN =           new PacketType(PROTOCOL, SENDER, 235, "NamedEntitySpawn", "SPacketSpawnPlayer");

            /**
             * @deprecated Removed in 1.20.4: replaced with new packets for removing and sending resource packs
             */
            @Deprecated
            public static final PacketType RESOURCE_PACK_SEND =           new PacketType(PROTOCOL, SENDER, 234, "ResourcePackSend", "SPacketResourcePackSend");

            /**
             * @deprecated Removed in 1.21.2: replaced with individual packets for adding/removing recipes
             */
            @Deprecated
            public static final PacketType RECIPES =                      new PacketType(PROTOCOL, SENDER, 233, "Recipe", "Recipes", "SPacketRecipeBook");

            /**
             * @deprecated Removed in 1.21.5; replace by SPAWN_ENTITY
             */
            @Deprecated
            public static final PacketType SPAWN_ENTITY_EXPERIENCE_ORB =  new PacketType(PROTOCOL, SENDER, 232, "AddExperienceOrb", "SpawnEntityExperienceOrb", "SPacketSpawnExperienceOrb");

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

            public static final PacketType TELEPORT_ACCEPT =              new PacketType(PROTOCOL, SENDER, 0x00, "AcceptTeleportation", "TeleportAccept", "CPacketConfirmTeleport");
            public static final PacketType TILE_NBT_QUERY =               new PacketType(PROTOCOL, SENDER, 0x01, "BlockEntityTagQuery", "TileNBTQuery");
            public static final PacketType SELECT_BUNDLE_ITEM =           new PacketType(PROTOCOL, SENDER, 0x02, "SelectBundleItem");
            public static final PacketType DIFFICULTY_CHANGE =            new PacketType(PROTOCOL, SENDER, 0x03, "ChangeDifficulty", "DifficultyChange");
            public static final PacketType CHANGE_GAME_MODE =             new PacketType(PROTOCOL, SENDER, 0x04, "ChangeGameMode");
            public static final PacketType CHAT_ACK =                     new PacketType(PROTOCOL, SENDER, 0x05, "ChatAck");
            public static final PacketType CHAT_COMMAND =                 new PacketType(PROTOCOL, SENDER, 0x06, "ChatCommand");
            public static final PacketType CHAT_COMMAND_SIGNED =          new PacketType(PROTOCOL, SENDER, 0x07, "ChatCommandSigned");
            public static final PacketType CHAT =                         new PacketType(PROTOCOL, SENDER, 0x08, "Chat", "CPacketChatMessage");
            public static final PacketType CHAT_SESSION_UPDATE =          new PacketType(PROTOCOL, SENDER, 0x09, "ChatSessionUpdate");
            public static final PacketType CHUNK_BATCH_RECEIVED =         new PacketType(PROTOCOL, SENDER, 0x0A, "ChunkBatchReceived");
            public static final PacketType CLIENT_COMMAND =               new PacketType(PROTOCOL, SENDER, 0x0B, "ClientCommand", "CPacketClientStatus");
            public static final PacketType CLIENT_TICK_END =              new PacketType(PROTOCOL, SENDER, 0x0C, "ClientTickEnd");
            public static final PacketType SETTINGS =                     new PacketType(PROTOCOL, SENDER, 0x0D, "ClientInformation", "Settings", "CPacketClientSettings");
            public static final PacketType TAB_COMPLETE =                 new PacketType(PROTOCOL, SENDER, 0x0E, "CommandSuggestion", "TabComplete", "CPacketTabComplete");
            public static final PacketType CONFIGURATION_ACK =            new PacketType(PROTOCOL, SENDER, 0x0F, "ConfigurationAcknowledged");
            public static final PacketType ENCHANT_ITEM =                 new PacketType(PROTOCOL, SENDER, 0x10, "ContainerButtonClick", "EnchantItem", "CPacketEnchantItem");
            public static final PacketType WINDOW_CLICK =                 new PacketType(PROTOCOL, SENDER, 0x11, "ContainerClick", "WindowClick", "CPacketClickWindow");
            public static final PacketType CLOSE_WINDOW =                 new PacketType(PROTOCOL, SENDER, 0x12, "ContainerClose", "CloseWindow", "CPacketCloseWindow");
            public static final PacketType CONTAINER_SLOT_STATE_CHANGED = new PacketType(PROTOCOL, SENDER, 0x13, "ContainerSlotStateChanged", "ContainerSlotStateChangedPacket");
            public static final PacketType COOKIE_RESPONSE =              new PacketType(PROTOCOL, SENDER, 0x14, "CookieResponse");
            public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x15, "CustomPayload", "CPacketCustomPayload");
            public static final PacketType DEBUG_SUBSCRIPTION_REQUEST =   new PacketType(PROTOCOL, SENDER, 0x16, "DebugSubscriptionRequest");
            public static final PacketType B_EDIT =                       new PacketType(PROTOCOL, SENDER, 0x17, "EditBook", "BEdit");
            public static final PacketType ENTITY_NBT_QUERY =             new PacketType(PROTOCOL, SENDER, 0x18, "EntityTagQuery", "EntityNBTQuery");
            public static final PacketType USE_ENTITY =                   new PacketType(PROTOCOL, SENDER, 0x19, "Interact", "UseEntity", "CPacketUseEntity");
            public static final PacketType JIGSAW_GENERATE =              new PacketType(PROTOCOL, SENDER, 0x1A, "JigsawGenerate");
            public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x1B, "KeepAlive", "CPacketKeepAlive");
            public static final PacketType DIFFICULTY_LOCK =              new PacketType(PROTOCOL, SENDER, 0x1C, "LockDifficulty", "DifficultyLock");
            public static final PacketType POSITION =                     new PacketType(PROTOCOL, SENDER, 0x1D, "net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos", "Flying$Position", "Flying$PacketPlayInPosition", "CPacketPlayer$Position");
            public static final PacketType POSITION_LOOK =                new PacketType(PROTOCOL, SENDER, 0x1E, "net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot", "Flying$PositionLook", "Flying$PacketPlayInPositionLook", "CPacketPlayer$PositionRotation");
            public static final PacketType LOOK =                         new PacketType(PROTOCOL, SENDER, 0x1F, "net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot", "Flying$Look", "Flying$PacketPlayInLook", "CPacketPlayer$Rotation");
            public static final PacketType GROUND =                       new PacketType(PROTOCOL, SENDER, 0x20, "net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$StatusOnly", "Flying$d");
            public static final PacketType VEHICLE_MOVE =                 new PacketType(PROTOCOL, SENDER, 0x21, "MoveVehicle", "VehicleMove", "CPacketVehicleMove");
            public static final PacketType BOAT_MOVE =                    new PacketType(PROTOCOL, SENDER, 0x22, "PaddleBoat", "BoatMove", "CPacketSteerBoat");
            public static final PacketType PICK_ITEM_FROM_BLOCK =         new PacketType(PROTOCOL, SENDER, 0x23, "PickItemFromBlock");
            public static final PacketType PICK_ITEM =                    new PacketType(PROTOCOL, SENDER, 0x24, "PickItemFromEntity", "PickItem");
            public static final PacketType PING_REQUEST =                 new PacketType(PROTOCOL, SENDER, 0x25, "PingRequest");
            public static final PacketType AUTO_RECIPE =                  new PacketType(PROTOCOL, SENDER, 0x26, "PlaceRecipe", "AutoRecipe", "CPacketPlaceRecipe");
            public static final PacketType ABILITIES =                    new PacketType(PROTOCOL, SENDER, 0x27, "PlayerAbilities", "Abilities", "CPacketPlayerAbilities");
            public static final PacketType BLOCK_DIG =                    new PacketType(PROTOCOL, SENDER, 0x28, "PlayerAction", "BlockDig", "CPacketPlayerDigging");
            public static final PacketType ENTITY_ACTION =                new PacketType(PROTOCOL, SENDER, 0x29, "PlayerCommand", "EntityAction", "CPacketEntityAction");
            public static final PacketType STEER_VEHICLE =                new PacketType(PROTOCOL, SENDER, 0x2A, "PlayerInput", "SteerVehicle", "CPacketInput");
            public static final PacketType PLAYER_LOADED =                new PacketType(PROTOCOL, SENDER, 0x2B, "PlayerLoaded");
            public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x2C, "Pong");
            public static final PacketType RECIPE_SETTINGS =              new PacketType(PROTOCOL, SENDER, 0x2D, "RecipeBookChangeSettings", "RecipeSettings");
            public static final PacketType RECIPE_DISPLAYED =             new PacketType(PROTOCOL, SENDER, 0x2E, "RecipeBookSeenRecipe", "RecipeDisplayed", "CPacketRecipeInfo");
            public static final PacketType ITEM_NAME =                    new PacketType(PROTOCOL, SENDER, 0x2F, "RenameItem", "ItemName");
            public static final PacketType RESOURCE_PACK_STATUS =         new PacketType(PROTOCOL, SENDER, 0x30, "ResourcePack", "ResourcePackStatus", "CPacketResourcePackStatus");
            public static final PacketType ADVANCEMENTS =                 new PacketType(PROTOCOL, SENDER, 0x31, "SeenAdvancements", "Advancements", "CPacketSeenAdvancements");
            public static final PacketType TR_SEL =                       new PacketType(PROTOCOL, SENDER, 0x32, "SelectTrade", "TrSel");
            public static final PacketType BEACON =                       new PacketType(PROTOCOL, SENDER, 0x33, "SetBeacon", "Beacon");
            public static final PacketType HELD_ITEM_SLOT =               new PacketType(PROTOCOL, SENDER, 0x34, "SetCarriedItem", "HeldItemSlot", "CPacketHeldItemChange");
            public static final PacketType SET_COMMAND_BLOCK =            new PacketType(PROTOCOL, SENDER, 0x35, "SetCommandBlock");
            public static final PacketType SET_COMMAND_MINECART =         new PacketType(PROTOCOL, SENDER, 0x36, "SetCommandMinecart");
            public static final PacketType SET_CREATIVE_SLOT =            new PacketType(PROTOCOL, SENDER, 0x37, "SetCreativeModeSlot", "SetCreativeSlot", "CPacketCreativeInventoryAction");
            public static final PacketType SET_JIGSAW =                   new PacketType(PROTOCOL, SENDER, 0x38, "SetJigsawBlock", "SetJigsaw");
            public static final PacketType STRUCT =                       new PacketType(PROTOCOL, SENDER, 0x39, "SetStructureBlock", "Struct");
            public static final PacketType SET_TEST_BLOCK =               new PacketType(PROTOCOL, SENDER, 0x3A, "SetTestBlock");
            public static final PacketType UPDATE_SIGN =                  new PacketType(PROTOCOL, SENDER, 0x3B, "SignUpdate", "UpdateSign", "CPacketUpdateSign");
            public static final PacketType ARM_ANIMATION =                new PacketType(PROTOCOL, SENDER, 0x3C, "Swing", "ArmAnimation", "CPacketAnimation");
            public static final PacketType SPECTATE =                     new PacketType(PROTOCOL, SENDER, 0x3D, "TeleportToEntity", "Spectate", "CPacketSpectate");
            public static final PacketType TEST_INSTANCE_BLOCK_ACTION =   new PacketType(PROTOCOL, SENDER, 0x3E, "TestInstanceBlockAction");
            public static final PacketType USE_ITEM_ON =                  new PacketType(PROTOCOL, SENDER, 0x3F, "UseItemOn", "BlockPlace", "CPacketPlayerTryUseItemOnBlock");
            public static final PacketType USE_ITEM =                     new PacketType(PROTOCOL, SENDER, 0x40, "UseItem", "CPacketPlayerTryUseItem");
            public static final PacketType CUSTOM_CLICK_ACTION =          new PacketType(PROTOCOL, SENDER, 0x41, "CustomClickAction");

            /**
             * @deprecated Removed in 1.17
             */
            @Deprecated
            public static final PacketType TRANSACTION =                  new PacketType(PROTOCOL, SENDER, 255, "Transaction", "CPacketConfirmTransaction");

            /**
             * @deprecated Removed in 1.17
             */
            @Deprecated
            public static final PacketType FLYING =                       new PacketType(PROTOCOL, SENDER, 254, "Flying", "CPacketPlayer");

            /**
             * @deprecated Removed in 1.19.3
             */
            @Deprecated
            public static final PacketType CHAT_PREVIEW =                 new PacketType(PROTOCOL, SENDER, 253, "ChatPreview");

            /**
             * deprecated Removed in 1.21.9
             */
            @Deprecated
            public static final PacketType DEBUG_SAMPLE_SUBSCRIPTION =    new PacketType(PROTOCOL, SENDER, 0x16, "DebugSampleSubscription");

            /**
             * @deprecated Renamed to USE_ITEM_ON
             */
            @Deprecated
            public static final PacketType BLOCK_PLACE = USE_ITEM_ON.clone();

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
            public static final PacketType SERVER_INFO =                  new PacketType(PROTOCOL, SENDER, 0x00, "StatusResponse", "ServerInfo", "SPacketServerInfo");
            @ForceAsync
            public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x01, "PongResponse", "Pong", "SPacketPong");

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

            public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, "StatusRequest", "Start", "CPacketServerQuery");
            @ForceAsync
            public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x01, "PingRequest", "Ping", "CPacketPing");

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
            public static final PacketType DISCONNECT =                   new PacketType(PROTOCOL, SENDER, 0x00, "LoginDisconnect", "Disconnect", "SPacketDisconnect");
            public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, "Hello", "EncryptionBegin", "SPacketEncryptionRequest");
            public static final PacketType SUCCESS =                      new PacketType(PROTOCOL, SENDER, 0x02, "LoginFinished", "GameProfile", "Success", "SPacketLoginSuccess");
            public static final PacketType SET_COMPRESSION =              new PacketType(PROTOCOL, SENDER, 0x03, "LoginCompression", "SetCompression", "SPacketEnableCompression");
            public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x04, "CustomQuery", "CustomPayload", "SPacketCustomPayload");
            public static final PacketType COOKIE_REQUEST =               new PacketType(PROTOCOL, SENDER, 0x05, "CookieRequest");

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

            public static final PacketType START =                        new PacketType(PROTOCOL, SENDER, 0x00, "Hello", "Start", "CPacketLoginStart");
            public static final PacketType ENCRYPTION_BEGIN =             new PacketType(PROTOCOL, SENDER, 0x01, "Key", "EncryptionBegin", "CPacketEncryptionResponse");
            public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x02, "CustomQueryAnswer", "CustomPayload", "CPacketCustomPayload");
            public static final PacketType LOGIN_ACK =                    new PacketType(PROTOCOL, SENDER, 0x03, "LoginAcknowledged");
            public static final PacketType COOKIE_RESPONSE =              new PacketType(PROTOCOL, SENDER, 0x04, "CookieResponse");

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
     * Packets sent and received during client configuration.
     * @author Pasqual Koschmieder
     */
    public static class Configuration {
        static final Protocol PROTOCOL = Protocol.CONFIGURATION;

        /**
         * Outgoing packets.
         */
        public static class Server extends PacketTypeEnum {
            private static final Sender SENDER = Sender.SERVER;

            public static final PacketType COOKIE_REQUEST =               new PacketType(PROTOCOL, SENDER, 0x00, "CookieRequest");
            public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x01, "CustomPayload");
            public static final PacketType DISCONNECT =                   new PacketType(PROTOCOL, SENDER, 0x02, "Disconnect");
            public static final PacketType FINISH_CONFIGURATION =         new PacketType(PROTOCOL, SENDER, 0x03, "FinishConfiguration");
            public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x04, "KeepAlive");
            public static final PacketType PING =                         new PacketType(PROTOCOL, SENDER, 0x05, "Ping");
            public static final PacketType RESET_CHAT =                   new PacketType(PROTOCOL, SENDER, 0x06, "ResetChat");
            public static final PacketType REGISTRY_DATA =                new PacketType(PROTOCOL, SENDER, 0x07, "RegistryData");
            public static final PacketType REMOVE_RESOURCE_PACK =         new PacketType(PROTOCOL, SENDER, 0x08, "ResourcePackPop");
            public static final PacketType ADD_RESOURCE_PACK =            new PacketType(PROTOCOL, SENDER, 0x09, "ResourcePackPush");
            public static final PacketType STORE_COOKIE =                 new PacketType(PROTOCOL, SENDER, 0x0A, "StoreCookie");
            public static final PacketType TRANSFER =                     new PacketType(PROTOCOL, SENDER, 0x0B, "Transfer");
            public static final PacketType UPDATE_ENABLED_FEATURES =      new PacketType(PROTOCOL, SENDER, 0x0C, "UpdateEnabledFeatures");
            public static final PacketType UPDATE_TAGS =                  new PacketType(PROTOCOL, SENDER, 0x0D, "UpdateTags");
            public static final PacketType SELECT_KNOWN_PACKS =           new PacketType(PROTOCOL, SENDER, 0x0E, "ClientboundSelectKnownPacks");
            public static final PacketType REPORT_DETAILS =               new PacketType(PROTOCOL, SENDER, 0x0F, "CustomReportDetails");
            public static final PacketType SERVER_LINKS =                 new PacketType(PROTOCOL, SENDER, 0x10, "ServerLinks");
            public static final PacketType CLEAR_DIALOG =                 new PacketType(PROTOCOL, SENDER, 0x11, "ClearDialog");
            public static final PacketType SHOW_DIALOG =                  new PacketType(PROTOCOL, SENDER, 0x12, "ShowDialog");
            public static final PacketType CODE_OF_CONDUCT =              new PacketType(PROTOCOL, SENDER, 0x13, "CodeOfConduct");

            /**
             * @deprecated Removed in 1.20.4: replaced with new packets for removing and sending resource packs
             */
            @Deprecated
            public static final PacketType RESOURCE_PACK =                new PacketType(PROTOCOL, SENDER, 255, "ResourcePack");
            
            private static final Server INSTANCE = new Server();

            // Prevent accidental construction
            private Server() { super(); }

            public static Server getInstance() {
                return INSTANCE;
            }
        }

        /**
         * Incoming packets.
         */
        public static class Client extends PacketTypeEnum {
            private static final Sender SENDER = Sender.CLIENT;

            public static final PacketType CLIENT_INFORMATION =           new PacketType(PROTOCOL, SENDER, 0x00, "ClientInformation");
            public static final PacketType COOKIE_RESPONSE =              new PacketType(PROTOCOL, SENDER, 0x01, "CookieResponse");
            public static final PacketType CUSTOM_PAYLOAD =               new PacketType(PROTOCOL, SENDER, 0x02, "CustomPayload");
            public static final PacketType FINISH_CONFIGURATION =         new PacketType(PROTOCOL, SENDER, 0x03, "FinishConfiguration");
            public static final PacketType KEEP_ALIVE =                   new PacketType(PROTOCOL, SENDER, 0x04, "KeepAlive");
            public static final PacketType PONG =                         new PacketType(PROTOCOL, SENDER, 0x05, "Pong");
            public static final PacketType RESOURCE_PACK_ACK =            new PacketType(PROTOCOL, SENDER, 0x06, "ResourcePack");
            public static final PacketType SELECT_KNOWN_PACKS =           new PacketType(PROTOCOL, SENDER, 0x07, "ServerboundSelectKnownPacks");
            public static final PacketType CUSTOM_CLICK_ACTION =          new PacketType(PROTOCOL, SENDER, 0x08, "CustomClickAction");
            public static final PacketType ACCEPT_CODE_OF_CONDUCT =       new PacketType(PROTOCOL, SENDER, 0x09, "AcceptCodeOfConduct");

            private static final Client INSTANCE = new Client();

            // Prevent accidental construction
            private Client() { super(); }

            public static Client getInstance() {
                return INSTANCE;
            }
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
        CONFIGURATION("Configuration", "configuration"),
        UNKNOWN("", "");

        private final String packetName;
        private final String mojangName;

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
            if (vanilla == null) {
                return UNKNOWN;
            }

            switch (vanilla.name()) {
                case "HANDSHAKING": return HANDSHAKING;
                case "PLAY": return PLAY;
                case "STATUS": return STATUS;
                case "LOGIN": return LOGIN;
                case "CONFIGURATION": return CONFIGURATION;
                default: return UNKNOWN;
            }
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

        private final String mojangName;
        private final String packetName;
        private final String mcpName;

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
     * Whether packets of this type must be handled asynchronously.
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
    private transient int hashCode;

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
                addPacketTypes(Configuration.Client.getInstance()).
                addPacketTypes(Configuration.Server.getInstance());
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
     * However, there are some valid uses for packet IDs. Please note that IDs
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

    private static String formatSimpleClassName(Protocol protocol, Sender sender, String name) {
        return "Packet" + protocol.getPacketName() + sender.getPacketName() + name;
    }

    private static String formatSimpleMojangClassName(Protocol protocol, Sender sender, String name) {
        return sender.getMojangName() + name + "Packet";
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
     * The packet will automatically be registered if it is missing.
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

    static BiConsumer<PacketType, String> onDynamicCreate = (type, className) -> {};
    static BiConsumer<PacketType, Integer> onIdMismatch = (type, newId) -> {};

    /**
     * Retrieve a packet type from a protocol, sender, ID, and class for 1.8+
     * <p>
     * The packet will automatically be registered if it is missing.
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
        PacketType type = find(map, packetClass);
        if (type == null) {
            // Guess we don't support this packet :/
            type = new PacketType(protocol, sender, packetId, PROTOCOL_VERSION, className);
            type.dynamic = true;

            // Many may be scheduled, but only the first will be executed
            scheduleRegister(type, "Dynamic-" + UUID.randomUUID().toString());
            onDynamicCreate.accept(type, className);
        } else if (packetId != type.getCurrentId()) {
        	onIdMismatch.accept(type, packetId);
        }

        return type;
    }

    private static PacketType find(Map<String, PacketType> map, Class<?> packetClass) {
    	String className = packetClass.getName();
        PacketType ret = map.get(className);
        if (ret != null) {
            return ret;
        }

        // Check any aliases
        for (PacketType check : map.values()) {
            List<String> aliases = check.getClassNames();
            if (aliases.size() > 1) {
                for (String alias : aliases) {
                    if (alias.equals(className) || alias.equals(packetClass.getSimpleName())) {
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
     * @return The corresponding packet type, never null.
     * @throws IllegalArgumentException if the given packet class is not a registered packet.
     * @deprecated since 1.20.2 there are packet classes that are shared between protocol states, therefore the result can be invalid.
     */
    @Deprecated
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
        UniversalRunnable runnable = new UniversalRunnable() {
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
        for (String classname : names) {
            if (isMcpPacketName(classname)) { // Minecraft MCP packets
                classNames.add(formatMcpClassName(protocol, sender, classname));
            } else {
                classNames.add(formatClassName(protocol, sender, classname));
                classNames.add(formatMojangClassName(protocol, sender, classname));
                classNames.add(formatSimpleClassName(protocol, sender, classname));
                classNames.add(formatSimpleMojangClassName(protocol, sender, classname));
                classNames.add(classname);
            }
        }

        this.names = names;
    }

    /**
     * Determine if this packet is supported on the current server.
     * @return Whether the packet is supported.
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
     * Whether this packet is deprecated. Deprecated packet types have either been renamed, replaced, or removed.
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
     * Whether the processing of this packet must take place on a thread different from the main thread. You don't
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
     * Whether this packet was dynamically created (i.e. we don't have it registered)
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
