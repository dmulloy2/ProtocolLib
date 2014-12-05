package com.comphenix.protocol.wrappers;

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
		PLAYER_DIG_TYPE_CLASS = getEnum(PacketType.Play.Client.BLOCK_DIG.getPacketClass(), 0);
		PLAYER_ACTION_CLASS = getEnum(PacketType.Play.Client.ENTITY_ACTION.getPacketClass(), 0);
		SCOREBOARD_ACTION_CLASS = getEnum(PacketType.Play.Server.SCOREBOARD_SCORE.getPacketClass(), 0);

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
		INITIALIZED = true;
	}

	private static void associate(Class<?> nativeClass, Class<?> wrapperClass, EquivalentConverter<?> converter) {
		FROM_NATIVE.put(nativeClass, converter);
		FROM_WRAPPER.put(wrapperClass, converter);
	}

	/**
	 * Retrieve the enum field with the given declaration index (in relation to the other enums).
	 * @param clazz - the declaration class.
	 * @param index - the enum index.
	 * @return The type of the enum field.
	 */
	private static Class<?> getEnum(Class<?> clazz, int index) {
		return FuzzyReflection.fromClass(clazz, true).getFieldListByType(Enum.class).get(index).getType();
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
