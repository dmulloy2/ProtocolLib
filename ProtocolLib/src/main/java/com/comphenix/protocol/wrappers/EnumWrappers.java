package com.comphenix.protocol.wrappers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

/**
 * Represents a generic enum converter.
 * @author Kristian
 */
public abstract class EnumWrappers {
	public enum ClientCommand {
		PERFORM_RESPAWN,
		REQUEST_STATS,
		OPEN_INVENTORY_ACHIEVEMENT;
	}

	public enum ChatVisibility {
		FULL,
		SYSTEM,
		HIDDEN;
	}

	public enum Difficulty {
		PEACEFUL,
		EASY,
		NORMAL,
		HARD;
	}

	public enum EntityUseAction {
		INTERACT,
		ATTACK,
		INTERACT_AT;
	}

	/**
	 * Represents a native game mode in Minecraft.
	 * <p>
	 * Not to be confused with {@link GameMode} in Bukkit.
	 * 
	 * @author Kristian
	 */
	public enum NativeGameMode {
		NOT_SET,
		SURVIVAL,
		CREATIVE,
		ADVENTURE,
		SPECTATOR,

		/**
		 * @deprecated Replaced by NOT_SET
		 */
		NONE;

		/**
		 * Gets this NativeGameMode's Bukkit equivalent.
		 * <p>
		 * Note: There is not a Bukkit equivalent for NOT_SET or NONE
		 * 
		 * @return The Bukkit equivalent, or null if one does not exist.
		 */
		public GameMode toBukkit() {
			switch (this) {
				case ADVENTURE:
					return GameMode.ADVENTURE;
				case CREATIVE:
					return GameMode.CREATIVE;
				case SPECTATOR:
					return GameMode.SPECTATOR;
				case SURVIVAL:
					return GameMode.SURVIVAL;
				default:
					return null;
			}
		}

		/**
		 * Obtains the given GameMode's NativeGameMode equivalent.
		 * 
		 * @param mode Bukkit GameMode
		 * @return The NativeGameMode equivalent, or null if one does not exist.
		 */
		public static NativeGameMode fromBukkit(GameMode mode) {
			switch (mode) {
				case ADVENTURE:
					return ADVENTURE;
				case CREATIVE:
					return CREATIVE;
				case SPECTATOR:
					return SPECTATOR;
				case SURVIVAL:
					return SURVIVAL;
				default:
					return null;
			}
		}
	}

	public enum ResourcePackStatus {
		SUCCESSFULLY_LOADED,
		DECLINED,
		FAILED_DOWNLOAD,
		ACCEPTED;
	}

	public enum PlayerInfoAction {
		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER;
	}

	public enum TitleAction {
		TITLE,
		SUBTITLE,
		TIMES,
		CLEAR,
		RESET;
	}

	public enum WorldBorderAction {
		SET_SIZE,
		LERP_SIZE,
		SET_CENTER,
		INITIALIZE,
		SET_WARNING_TIME,
		SET_WARNING_BLOCKS;
	}

	public enum CombatEventType {
		ENTER_COMBAT,
		END_COMBAT,
		ENTITY_DIED;
	}

	public enum PlayerDigType {
		START_DESTROY_BLOCK,
		ABORT_DESTROY_BLOCK,
		STOP_DESTROY_BLOCK,
		DROP_ALL_ITEMS,
		DROP_ITEM,
		RELEASE_USE_ITEM;
	}

	public enum PlayerAction {
		START_SNEAKING,
		STOP_SNEAKING,
		STOP_SLEEPING,
		START_SPRINTING,
		STOP_SPRINTING,
		RIDING_JUMP,
		OPEN_INVENTORY;
	}

	public enum ScoreboardAction {
		CHANGE,
		REMOVE;
	}

	public enum Particle {
		EXPLOSION_NORMAL("explode", 0, true),
		EXPLOSION_LARGE("largeexplode", 1, true),
		EXPLOSION_HUGE("hugeexplosion", 2, true),
		FIREWORKS_SPARK("fireworksSpark", 3, false),
		WATER_BUBBLE("bubble", 4, false),
		WATER_SPLASH("splash", 5, false),
		WATER_WAKE("wake", 6, false),
		SUSPENDED("suspended", 7, false),
		SUSPENDED_DEPTH("depthsuspend", 8, false),
		CRIT("crit", 9, false),
		CRIT_MAGIC("magicCrit", 10, false),
		SMOKE_NORMAL("smoke", 11, false),
		SMOKE_LARGE("largesmoke", 12, false),
		SPELL("spell", 13, false),
		SPELL_INSTANT("instantSpell", 14, false),
		SPELL_MOB("mobSpell", 15, false),
		SPELL_MOB_AMBIENT("mobSpellAmbient", 16, false),
		SPELL_WITCH("witchMagic", 17, false),
		DRIP_WATER("dripWater", 18, false),
		DRIP_LAVA("dripLava", 19, false),
		VILLAGER_ANGRY("angryVillager", 20, false),
		VILLAGER_HAPPY("happyVillager", 21, false),
		TOWN_AURA("townaura", 22, false),
		NOTE("note", 23, false),
		PORTAL("portal", 24, false),
		ENCHANTMENT_TABLE("enchantmenttable", 25, false),
		FLAME("flame", 26, false),
		LAVA("lava", 27, false),
		FOOTSTEP("footstep", 28, false),
		CLOUD("cloud", 29, false),
		REDSTONE("reddust", 30, false),
		SNOWBALL("snowballpoof", 31, false),
		SNOW_SHOVEL("snowshovel", 32, false),
		SLIME("slime", 33, false),
		HEART("heart", 34, false),
		BARRIER("barrier", 35, false),
		ITEM_CRACK("iconcrack_", 36, false, 2),
		BLOCK_CRACK("blockcrack_", 37, false, 1),
		BLOCK_DUST("blockdust_", 38, false, 1),
		WATER_DROP("droplet", 39, false),
		ITEM_TAKE("take", 40, false),
		MOB_APPEARANCE("mobappearance", 41, true);

		private static final Map<String, Particle> BY_NAME;
		private static final Map<Integer, Particle> BY_ID;

		static {
			BY_ID = new HashMap<Integer, Particle>();
			BY_NAME = new HashMap<String, Particle>();

			for (Particle particle : values()) {
				BY_NAME.put(particle.getName().toLowerCase(), particle);
				BY_ID.put(particle.getId(), particle);
			}
		}

		private final String name;
		private final int id;
		private final boolean longDistance;
		private final int dataLength;

		private Particle(String name, int id, boolean longDistance) {
			this(name, id, longDistance, 0);
		}

		private Particle(String name, int id, boolean longDistance, int dataLength) {
			this.name = name;
			this.id = id;
			this.longDistance = longDistance;
			this.dataLength = dataLength;
		}

		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		public boolean isLongDistance() {
			return longDistance;
		}

		public int getDataLength() {
			return dataLength;
		}

		public static Particle getByName(String name) {
			return BY_NAME.get(name.toLowerCase());
		}

		public static Particle getById(int id) {
			return BY_ID.get(id);
		}
	}

	private static Class<?> PROTOCOL_CLASS = null;
	private static Class<?> CLIENT_COMMAND_CLASS = null;
	private static Class<?> CHAT_VISIBILITY_CLASS = null;
	private static Class<?> DIFFICULTY_CLASS = null;
	private static Class<?> ENTITY_USE_ACTION_CLASS = null;
	private static Class<?> GAMEMODE_CLASS = null;
	private static Class<?> RESOURCE_PACK_STATUS_CLASS = null;
	private static Class<?> PLAYER_INFO_ACTION_CLASS = null;
	private static Class<?> TITLE_ACTION_CLASS = null;
	private static Class<?> WORLD_BORDER_ACTION_CLASS = null;
	private static Class<?> COMBAT_EVENT_TYPE_CLASS = null;
	private static Class<?> PLAYER_DIG_TYPE_CLASS = null;
	private static Class<?> PLAYER_ACTION_CLASS = null;
	private static Class<?> SCOREBOARD_ACTION_CLASS = null;
	private static Class<?> PARTICLE_CLASS = null;

	private static boolean INITIALIZED = false;
	private static Map<Class<?>, EquivalentConverter<?>> FROM_NATIVE = Maps.newHashMap();
	private static Map<Class<?>, EquivalentConverter<?>> FROM_WRAPPER = Maps.newHashMap();

	/**
	 * Initialize the wrappers, if we haven't already.
	 */
	private static void initialize() {
		if (!MinecraftReflection.isUsingNetty())
			throw new IllegalArgumentException("Not supported on 1.6.4 and earlier.");

		if (INITIALIZED)
			return;

		INITIALIZED = true;

		PROTOCOL_CLASS = getEnum(PacketType.Handshake.Client.SET_PROTOCOL.getPacketClass(), 0);
		CLIENT_COMMAND_CLASS = getEnum(PacketType.Play.Client.CLIENT_COMMAND.getPacketClass(), 0);
		CHAT_VISIBILITY_CLASS = getEnum(PacketType.Play.Client.SETTINGS.getPacketClass(), 0);
		DIFFICULTY_CLASS = getEnum(PacketType.Play.Server.LOGIN.getPacketClass(), 1);
		ENTITY_USE_ACTION_CLASS = getEnum(PacketType.Play.Client.USE_ENTITY.getPacketClass(), 0);
		GAMEMODE_CLASS = getEnum(PacketType.Play.Server.LOGIN.getPacketClass(), 0);
		RESOURCE_PACK_STATUS_CLASS = getEnum(PacketType.Play.Client.RESOURCE_PACK_STATUS.getPacketClass(), 0);
		PLAYER_INFO_ACTION_CLASS = getEnum(PacketType.Play.Server.PLAYER_INFO.getPacketClass(), 0);
		TITLE_ACTION_CLASS = getEnum(PacketType.Play.Server.TITLE.getPacketClass(), 0);
		WORLD_BORDER_ACTION_CLASS = getEnum(PacketType.Play.Server.WORLD_BORDER.getPacketClass(), 0);
		COMBAT_EVENT_TYPE_CLASS = getEnum(PacketType.Play.Server.COMBAT_EVENT.getPacketClass(), 0);
		PLAYER_DIG_TYPE_CLASS = getEnum(PacketType.Play.Client.BLOCK_DIG.getPacketClass(), 1);
		PLAYER_ACTION_CLASS = getEnum(PacketType.Play.Client.ENTITY_ACTION.getPacketClass(), 0);
		SCOREBOARD_ACTION_CLASS = getEnum(PacketType.Play.Server.SCOREBOARD_SCORE.getPacketClass(), 0);
		PARTICLE_CLASS = getEnum(PacketType.Play.Server.WORLD_PARTICLES.getPacketClass(), 0);

		associate(PROTOCOL_CLASS, Protocol.class, getClientCommandConverter());
		associate(CLIENT_COMMAND_CLASS, ClientCommand.class, getClientCommandConverter());
		associate(CHAT_VISIBILITY_CLASS, ChatVisibility.class, getChatVisibilityConverter());
		associate(DIFFICULTY_CLASS, Difficulty.class, getDifficultyConverter());
		associate(ENTITY_USE_ACTION_CLASS, EntityUseAction.class, getEntityUseActionConverter());
		associate(GAMEMODE_CLASS, NativeGameMode.class, getGameModeConverter());
		associate(RESOURCE_PACK_STATUS_CLASS, ResourcePackStatus.class, getResourcePackStatusConverter());
		associate(PLAYER_INFO_ACTION_CLASS, PlayerInfoAction.class, getPlayerInfoActionConverter());
		associate(TITLE_ACTION_CLASS, TitleAction.class, getTitleActionConverter());
		associate(WORLD_BORDER_ACTION_CLASS, WorldBorderAction.class, getWorldBorderActionConverter());
		associate(COMBAT_EVENT_TYPE_CLASS, CombatEventType.class, getCombatEventTypeConverter());
		associate(PLAYER_DIG_TYPE_CLASS, PlayerDigType.class, getPlayerDiggingActionConverter());
		associate(PLAYER_ACTION_CLASS, PlayerAction.class, getEntityActionConverter());
		associate(SCOREBOARD_ACTION_CLASS, ScoreboardAction.class, getUpdateScoreActionConverter());
		associate(PARTICLE_CLASS, Particle.class, getParticleConverter());
		INITIALIZED = true;
	}

	private static void associate(Class<?> nativeClass, Class<?> wrapperClass, EquivalentConverter<?> converter) {
		if (nativeClass != null) {
			FROM_NATIVE.put(nativeClass, converter);
			FROM_WRAPPER.put(wrapperClass, converter);
		}
	}

	/**
	 * Retrieve the enum field with the given declaration index (in relation to the other enums).
	 * @param clazz - the declaration class.
	 * @param index - the enum index.
	 * @return The type of the enum field.
	 */
	private static Class<?> getEnum(Class<?> clazz, int index) {
		try {
			return FuzzyReflection.fromClass(clazz, true).getFieldListByType(Enum.class).get(index).getType();
		} catch (Throwable ex) {
			return null; // Unsupported in this version
		}
	}

	public static Map<Class<?>, EquivalentConverter<?>> getFromNativeMap() {
		return FROM_NATIVE;
	}

	public static Map<Class<?>, EquivalentConverter<?>> getFromWrapperMap() {
		return FROM_WRAPPER;
	}

	// Get the native enum classes
	public static Class<?> getProtocolClass() {
		initialize();
		return PROTOCOL_CLASS;
	}

	public static Class<?> getClientCommandClass() {
		initialize();
		return CLIENT_COMMAND_CLASS;
	}

	public static Class<?> getChatVisibilityClass() {
		initialize();
		return CHAT_VISIBILITY_CLASS;
	}

	public static Class<?> getDifficultyClass() {
		initialize();
		return DIFFICULTY_CLASS;
	}

	public static Class<?> getEntityUseActionClass() {
		initialize();
		return ENTITY_USE_ACTION_CLASS;
	}

	public static Class<?> getGameModeClass() {
		initialize();
		return GAMEMODE_CLASS;
	}

	public static Class<?> getResourcePackStatusClass() {
		initialize();
		return RESOURCE_PACK_STATUS_CLASS;
	}

	public static Class<?> getPlayerInfoActionClass() {
		initialize();
		return PLAYER_INFO_ACTION_CLASS;
	}

	public static Class<?> getTitleActionClass() {
		initialize();
		return TITLE_ACTION_CLASS;
	}

	public static Class<?> getWorldBorderActionClass() {
		initialize();
		return WORLD_BORDER_ACTION_CLASS;
	}

	public static Class<?> getCombatEventTypeClass() {
		initialize();
		return COMBAT_EVENT_TYPE_CLASS;
	}

	public static Class<?> getPlayerDigTypeClass() {
		initialize();
		return PLAYER_DIG_TYPE_CLASS;
	}

	public static Class<?> getPlayerActionClass() {
		initialize();
		return PLAYER_ACTION_CLASS;
	}

	public static Class<?> getScoreboardActionClass() {
		initialize();
		return SCOREBOARD_ACTION_CLASS;
	}

	public static Class<?> getParticleClass() {
		initialize();
		return PARTICLE_CLASS;
	}

	// Get the converters
	public static EquivalentConverter<Protocol> getProtocolConverter() {
		return new EnumConverter<Protocol>(Protocol.class);
	}

	public static EquivalentConverter<ClientCommand> getClientCommandConverter() {
		return new EnumConverter<ClientCommand>(ClientCommand.class);
	}

	public static EquivalentConverter<ChatVisibility> getChatVisibilityConverter() {
		return new EnumConverter<ChatVisibility>(ChatVisibility.class);
	}

	public static EquivalentConverter<Difficulty> getDifficultyConverter() {
		return new EnumConverter<Difficulty>(Difficulty.class);
	}

	public static EquivalentConverter<EntityUseAction> getEntityUseActionConverter() {
		return new EnumConverter<EntityUseAction>(EntityUseAction.class);
	}

	public static EquivalentConverter<NativeGameMode> getGameModeConverter() {
		return new EnumConverter<NativeGameMode>(NativeGameMode.class);
	}

	public static EquivalentConverter<ResourcePackStatus> getResourcePackStatusConverter() {
		return new EnumConverter<ResourcePackStatus>(ResourcePackStatus.class);
	}

	public static EquivalentConverter<PlayerInfoAction> getPlayerInfoActionConverter() {
		return new EnumConverter<PlayerInfoAction>(PlayerInfoAction.class);
	}

	public static EquivalentConverter<TitleAction> getTitleActionConverter() {
		return new EnumConverter<TitleAction>(TitleAction.class);
	}

	public static EquivalentConverter<WorldBorderAction> getWorldBorderActionConverter() {
		return new EnumConverter<WorldBorderAction>(WorldBorderAction.class);
	}

	public static EquivalentConverter<CombatEventType> getCombatEventTypeConverter() {
		return new EnumConverter<CombatEventType>(CombatEventType.class);
	}

	public static EquivalentConverter<PlayerDigType> getPlayerDiggingActionConverter() {
		return new EnumConverter<PlayerDigType>(PlayerDigType.class);
	}

	public static EquivalentConverter<PlayerAction> getEntityActionConverter() {
		return new EnumConverter<PlayerAction>(PlayerAction.class);
	}

	public static EquivalentConverter<ScoreboardAction> getUpdateScoreActionConverter() {
		return new EnumConverter<ScoreboardAction>(ScoreboardAction.class);
	}

	public static EquivalentConverter<Particle> getParticleConverter() {
		return new EnumConverter<Particle>(Particle.class);
	}

	/**
	 * Retrieve a generic enum converter for use with StructureModifiers.
	 * @param enumClass - Enum class
	 * @return A generic enum converter
	 */
	public static <T extends Enum<T>> EquivalentConverter<T> getGenericConverter(Class<T> enumClass) {
		return new EnumConverter<T>(enumClass);
	}

	// The common enum converter
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static class EnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
		private Class<T> specificType;

		public EnumConverter(Class<T> specificType) {
			this.specificType = specificType;
		}

		@Override
		public T getSpecific(Object generic) {
			// We know its an enum already!
			return Enum.valueOf(specificType, ((Enum) generic).name());
		}

		@Override
		public Object getGeneric(Class<?> genericType, T specific) {
			return Enum.valueOf((Class) genericType, specific.name());
		}

		@Override
		public Class<T> getSpecificType() {
			return specificType;
		}
	}
}
