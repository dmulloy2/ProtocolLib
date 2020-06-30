package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;

/**
 * Represents a generic enum converter.
 * @author Kristian
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class EnumWrappers {
	public enum ClientCommand {
		PERFORM_RESPAWN,
		REQUEST_STATS,
		OPEN_INVENTORY_ACHIEVEMENT
	}

	public enum ChatVisibility {
		FULL,
		SYSTEM,
		HIDDEN
	}

	public enum Difficulty {
		PEACEFUL,
		EASY,
		NORMAL,
		HARD
	}

	public enum EntityUseAction {
		INTERACT,
		ATTACK,
		INTERACT_AT
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
		ACCEPTED
	}

	public enum PlayerInfoAction {
		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER
	}

	public enum TitleAction {
		TITLE,
		SUBTITLE,
		ACTIONBAR,
		TIMES,
		CLEAR,
		RESET
	}

	public enum WorldBorderAction {
		SET_SIZE,
		LERP_SIZE,
		SET_CENTER,
		INITIALIZE,
		SET_WARNING_TIME,
		SET_WARNING_BLOCKS
	}

	public enum CombatEventType {
		ENTER_COMBAT,
		END_COMBAT,
		ENTITY_DIED
	}

	public enum PlayerDigType implements AliasedEnum{
		START_DESTROY_BLOCK,
		ABORT_DESTROY_BLOCK,
		STOP_DESTROY_BLOCK,
		DROP_ALL_ITEMS,
		DROP_ITEM,
		RELEASE_USE_ITEM,
		SWAP_HELD_ITEMS("SWAP_ITEM_WITH_OFFHAND");

		String[] aliases;
		PlayerDigType(String... aliases) {
			this.aliases = aliases;
		}
		@Override
		public String[] getAliases() {
			return aliases;
		}
	}

	public enum PlayerAction implements AliasedEnum {
		START_SNEAKING("PRESS_SHIFT_KEY"),
		STOP_SNEAKING("RELEASE_SHIFT_KEY"),
		STOP_SLEEPING,
		START_SPRINTING,
		STOP_SPRINTING,
		START_RIDING_JUMP,
		STOP_RIDING_JUMP,
		OPEN_INVENTORY,
		START_FALL_FLYING;

		String[] aliases;
		PlayerAction(String... aliases) {
			this.aliases = aliases;
		}

		@Override
		public String[] getAliases() {
			return aliases;
		}
	}

	public enum ScoreboardAction {
		CHANGE,
		REMOVE
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
		ITEM_CRACK("iconcrack", 36, false, 2),
		BLOCK_CRACK("blockcrack", 37, false, 1),
		BLOCK_DUST("blockdust", 38, false, 1),
		WATER_DROP("droplet", 39, false),
		ITEM_TAKE("take", 40, false),
		MOB_APPEARANCE("mobappearance", 41, true),
		DRAGON_BREATH("dragonbreath", 42, false),
		END_ROD("endRod", 43, false),
		DAMAGE_INDICATOR("damageIndicator", 44, true),
		SWEEP_ATTACK("sweepAttack", 45, true),
		FALLING_DUST("fallingdust", 46, false, 1),
		TOTEM("totem", 47, false),
		SPIT("spit", 48, true);

		private static final Map<String, Particle> BY_NAME;
		private static final Map<Integer, Particle> BY_ID;

		static {
			BY_ID = new HashMap<>();
			BY_NAME = new HashMap<>();

			for (Particle particle : values()) {
				BY_NAME.put(particle.getName().toLowerCase(Locale.ENGLISH), particle);
				BY_ID.put(particle.getId(), particle);
			}
		}

		private final String name;
		private final int id;
		private final boolean longDistance;
		private final int dataLength;

		Particle(String name, int id, boolean longDistance) {
			this(name, id, longDistance, 0);
		}

		Particle(String name, int id, boolean longDistance, int dataLength) {
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
			return BY_NAME.get(name.toLowerCase(Locale.ENGLISH));
		}

		public static Particle getById(int id) {
			return BY_ID.get(id);
		}
	}

	public enum SoundCategory {
		MASTER("master"),
		MUSIC("music"),
		RECORDS("record"),
		WEATHER("weather"),
		BLOCKS("block"),
		HOSTILE("hostile"),
		NEUTRAL("neutral"),
		PLAYERS("player"),
		AMBIENT("ambient"),
		VOICE("voice");

		private static final Map<String, SoundCategory> LOOKUP;
		static {
			LOOKUP = new HashMap<>();
			for (SoundCategory category : values()) {
				LOOKUP.put(category.key, category);
			}
		}

		private final String key;

		SoundCategory(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public static SoundCategory getByKey(String key) {
			return LOOKUP.get(key.toLowerCase(Locale.ENGLISH));
		}
	}

	public enum ItemSlot {
		MAINHAND,
		OFFHAND,
		FEET,
		LEGS,
		CHEST,
		HEAD
	}

	public enum Hand {
		MAIN_HAND,
		OFF_HAND
	}

	public enum Direction {
		DOWN,
		UP,
		NORTH,
		SOUTH,
		WEST,
		EAST
	}
	
	public enum ChatType {
		CHAT,
		SYSTEM,
		GAME_INFO;

		public byte getId() {
			return (byte) ordinal();
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
	private static Class<?> SOUND_CATEGORY_CLASS = null;
	private static Class<?> ITEM_SLOT_CLASS = null;
	private static Class<?> HAND_CLASS = null;
	private static Class<?> DIRECTION_CLASS = null;
	private static Class<?> CHAT_TYPE_CLASS = null;

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

		try {
			DIFFICULTY_CLASS = getEnum(PacketType.Play.Server.SERVER_DIFFICULTY.getPacketClass(), 0);
		} catch (Exception ex) {
			DIFFICULTY_CLASS = getEnum(PacketType.Play.Server.LOGIN.getPacketClass(), 1);
		}

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
		SOUND_CATEGORY_CLASS = getEnum(PacketType.Play.Server.CUSTOM_SOUND_EFFECT.getPacketClass(), 0);
		ITEM_SLOT_CLASS = getEnum(PacketType.Play.Server.ENTITY_EQUIPMENT.getPacketClass(), 0);
		HAND_CLASS = getEnum(PacketType.Play.Client.USE_ENTITY.getPacketClass(), 1);
		DIRECTION_CLASS = getEnum(PacketType.Play.Server.SPAWN_ENTITY_PAINTING.getPacketClass(), 0);
		CHAT_TYPE_CLASS = getEnum(PacketType.Play.Server.CHAT.getPacketClass(), 0);

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
		associate(SOUND_CATEGORY_CLASS, SoundCategory.class, getSoundCategoryConverter());
		associate(ITEM_SLOT_CLASS, ItemSlot.class, getItemSlotConverter());
		associate(HAND_CLASS, Hand.class, getHandConverter());
		associate(DIRECTION_CLASS, Direction.class, getDirectionConverter());
		associate(CHAT_TYPE_CLASS, ChatType.class, getChatTypeConverter());
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
			ProtocolLogger.debug("Exception getting enum from " + clazz + " at index " + index, ex);
			return null;
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

	public static Class<?> getSoundCategoryClass() {
		initialize();
		return SOUND_CATEGORY_CLASS;
	}

	public static Class<?> getItemSlotClass() {
		initialize();
		return ITEM_SLOT_CLASS;
	}

	public static Class<?> getHandClass() {
		initialize();
		return HAND_CLASS;
	}

	public static Class<?> getDirectionClass() {
		initialize();
		return DIRECTION_CLASS;
	}
	
	public static Class<?> getChatTypeClass() {
		initialize();
		return CHAT_TYPE_CLASS;
	}

	// Get the converters
	public static EquivalentConverter<Protocol> getProtocolConverter() {
		return new EnumConverter<>(getProtocolClass(), Protocol.class);
	}

	public static EquivalentConverter<ClientCommand> getClientCommandConverter() {
		return new EnumConverter<>(getClientCommandClass(), ClientCommand.class);
	}

	public static EquivalentConverter<ChatVisibility> getChatVisibilityConverter() {
		return new EnumConverter<>(getChatVisibilityClass(), ChatVisibility.class);
	}

	public static EquivalentConverter<Difficulty> getDifficultyConverter() {
		return new EnumConverter<>(getDifficultyClass(), Difficulty.class);
	}

	public static EquivalentConverter<EntityUseAction> getEntityUseActionConverter() {
		return new EnumConverter<>(getEntityUseActionClass(), EntityUseAction.class);
	}

	public static EquivalentConverter<NativeGameMode> getGameModeConverter() {
		return new EnumConverter<>(getGameModeClass(), NativeGameMode.class);
	}

	public static EquivalentConverter<ResourcePackStatus> getResourcePackStatusConverter() {
		return new EnumConverter<>(getResourcePackStatusClass(), ResourcePackStatus.class);
	}

	public static EquivalentConverter<PlayerInfoAction> getPlayerInfoActionConverter() {
		return new EnumConverter<>(getPlayerInfoActionClass(), PlayerInfoAction.class);
	}

	public static EquivalentConverter<TitleAction> getTitleActionConverter() {
		return new EnumConverter<>(getTitleActionClass(), TitleAction.class);
	}

	public static EquivalentConverter<WorldBorderAction> getWorldBorderActionConverter() {
		return new EnumConverter<>(getWorldBorderActionClass(), WorldBorderAction.class);
	}

	public static EquivalentConverter<CombatEventType> getCombatEventTypeConverter() {
		return new EnumConverter<>(getCombatEventTypeClass(), CombatEventType.class);
	}

	public static EquivalentConverter<PlayerDigType> getPlayerDiggingActionConverter() {
		return new AliasedEnumConverter<>(getPlayerDigTypeClass(), PlayerDigType.class);
	}

	public static EquivalentConverter<PlayerAction> getEntityActionConverter() {
		return new AliasedEnumConverter<>(getPlayerActionClass(), PlayerAction.class);
	}

	public static EquivalentConverter<ScoreboardAction> getUpdateScoreActionConverter() {
		return new EnumConverter<>(getScoreboardActionClass(), ScoreboardAction.class);
	}

	public static EquivalentConverter<Particle> getParticleConverter() {
		return new EnumConverter<>(getParticleClass(), Particle.class);
	}

	public static EquivalentConverter<SoundCategory> getSoundCategoryConverter() {
		return new EnumConverter<>(getSoundCategoryClass(), SoundCategory.class);
	}

	public static EquivalentConverter<ItemSlot> getItemSlotConverter() {
		return new EnumConverter<>(getItemSlotClass(), ItemSlot.class);
	}

	public static EquivalentConverter<Hand> getHandConverter() {
		return new EnumConverter<>(getHandClass(), Hand.class);
	}

	public static EquivalentConverter<Direction> getDirectionConverter() {
		return new EnumConverter<>(getDirectionClass(), Direction.class);
	}
	
	public static EquivalentConverter<ChatType> getChatTypeConverter() {
		return new EnumConverter<>(getChatTypeClass(), ChatType.class);
	}

	/**
	 * Retrieve a generic enum converter for use with StructureModifiers.
	 * @param genericClass - Generic nms enum class
	 * @param specificType - Specific enum class
	 * @return A generic enum converter
	 */
	public static <T extends Enum<T>> EquivalentConverter<T> getGenericConverter(Class<?> genericClass, Class<T> specificType) {
		return new EnumConverter<>(genericClass, specificType);
	}

	/**
	 * @deprecated Replaced with {@link #getGenericConverter(Class, Class)}
	 */
	@Deprecated
	public static <T extends Enum<T>> EquivalentConverter<T> getGenericConverter(Class<T> specificType) {
		return new EnumConverter<>(null, specificType);
	}

	/**
	 * The common Enum converter
	 */
	public static class EnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
		private Class<?> genericType;
		private Class<T> specificType;

		public EnumConverter(Class<?> genericType, Class<T> specificType) {
			this.genericType = genericType;
			this.specificType = specificType;
		}

		@Override
		public T getSpecific(Object generic) {
			return Enum.valueOf(specificType, ((Enum) generic).name());
		}

		@Override
		public Object getGeneric(T specific) {
			return Enum.valueOf((Class) genericType, specific.name());
		}

		@Override
		public Class<T> getSpecificType() {
			return specificType;
		}

		void setGenericType(Class<?> genericType) {
			this.genericType = genericType;
		}
	}

	public interface AliasedEnum {
		String[] getAliases();
	}

	/**
	 * Enums whose name has changed across NMS versions. Enums using this must also implement {@link AliasedEnum}
	 */
	public static class AliasedEnumConverter<T extends Enum<T> & AliasedEnum> implements EquivalentConverter<T> {
		private Class<?> genericType;
		private Class<T> specificType;

		private Map<T, Object> genericMap = new ConcurrentHashMap<>();
		private Map<Object, T> specificMap = new ConcurrentHashMap<>();

		public AliasedEnumConverter(Class<?> genericType, Class<T> specificType) {
			this.genericType = genericType;
			this.specificType = specificType;
		}

		@Override
		public T getSpecific(Object generic) {
			return specificMap.computeIfAbsent(generic, x -> {
				String name = ((Enum) generic).name();

				try {
					return Enum.valueOf(specificType, name);
				} catch (Exception ex) {
					for (T elem : specificType.getEnumConstants()) {
						for (String alias : elem.getAliases()) {
							if (alias.equals(name)) {
								return elem;
							}
						}
					}
				}

				throw new IllegalArgumentException("Unknown enum constant " + name);
			});
		}

		@Override
		public Object getGeneric(T specific) {
			return genericMap.computeIfAbsent(specific, x -> {
				String name = specific.name();

				try {
					return Enum.valueOf((Class) genericType, specific.name());
				} catch (Exception ex) {
					for (Object rawElem : genericType.getEnumConstants()) {
						Enum elem = (Enum) rawElem;
						for (String alias : specific.getAliases()) {
							if (alias.equals(elem.name())) {
								return elem;
							}
						}
					}
				}

				throw new IllegalArgumentException("Unknown enum constant " + name);
			});
		}

		@Override
		public Class<T> getSpecificType() {
			return specificType;
		}
	}

	/**
	 * Used for classes where it's an enum in everything but name
	 * @param <T> Generic type
	 */
	public static class FauxEnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
		private final Class<T> specificClass;
		private final Class<?> genericClass;
		private final Map<Object, T> lookup;

		public FauxEnumConverter(Class<T> specific, Class<?> generic) {
			Validate.notNull(specific,"specific class cannot be null");
			Validate.notNull(generic,"generic class cannot be null");

			this.specificClass = specific;
			this.genericClass = generic;
			this.lookup = new HashMap<>();
		}

		@Override
		public Object getGeneric(T specific) {
			Validate.notNull(specific, "specific object cannot be null");

			return Accessors
					.getFieldAccessor(genericClass, specific
							.name(), false)
					.get(null);
		}

		@Override
		public T getSpecific(Object generic) {
			Validate.notNull(generic, "generic object cannot be null");

			return lookup.computeIfAbsent(generic, x -> {
				for (Field field : genericClass.getFields()) {
					try {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}

						if (field.get(null) == generic) {
							return Enum.valueOf(specificClass, field.getName().toUpperCase());
						}
					} catch (ReflectiveOperationException ignored) { }
				}

				throw new IllegalArgumentException("Could not find ProtocolLib wrapper for " + generic);
			});
		}

		@Override
		public Class<T> getSpecificType() {
			return specificClass;
		}
	}

	public static class IndexedEnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
		private Class<T> specificClass;
		private Class<?> genericClass;

		public IndexedEnumConverter(Class<T> specificClass, Class<?> genericClass) {
			this.specificClass = specificClass;
			this.genericClass = genericClass;
		}

		@Override
		public Object getGeneric(T specific) {
			int ordinal = specific.ordinal();
			for (Object elem : genericClass.getEnumConstants()) {
				if (((Enum<?>) elem).ordinal() == ordinal) {
					return elem;
				}
			}

			return null;
		}

		@Override
		public T getSpecific(Object generic) {
			int ordinal = ((Enum<?>) generic).ordinal();
			for (T elem : specificClass.getEnumConstants()) {
				if (elem.ordinal() == ordinal) {
					return elem;
				}
			}

			return null;
		}

		@Override
		public Class<T> getSpecificType() {
			return specificClass;
		}
	}
}
