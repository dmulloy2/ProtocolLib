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
		ATTACK;
	}
	
	/**
	 * Represents a native game mode in Minecraft.
	 * <p>
	 * Not to be confused with {@link GameMode} in Bukkit.
	 * @author Kristian
	 */
	public enum NativeGameMode {
		NONE, 
		SURVIVAL, 
		CREATIVE, 
		ADVENTURE;
	}

	private static Class<?> PROTOCOL_CLASS = null;
	private static Class<?> CLIENT_COMMAND_CLASS = null;
	private static Class<?> CHAT_VISIBILITY_CLASS = null;
	private static Class<?> DIFFICULTY_CLASS = null;
	private static Class<?> ENTITY_USE_ACTION_CLASS = null;
	private static Class<?> GAMEMODE_CLASS = null;
	
	private static Map<Class<?>, EquivalentConverter<?>> FROM_NATIVE = Maps.newHashMap();
	private static Map<Class<?>, EquivalentConverter<?>> FROM_WRAPPER = Maps.newHashMap();
	
	/**
	 * Initialize the wrappers, if we haven't already.
	 */
	private static void initialize() {
		if (!MinecraftReflection.isUsingNetty())
			throw new IllegalArgumentException("Not supported on 1.6.4 and earlier.");
		
		PROTOCOL_CLASS = getEnum(PacketType.Handshake.Client.SET_PROTOCOL.getPacketClass(), 0);
		CLIENT_COMMAND_CLASS = getEnum(PacketType.Play.Client.CLIENT_COMMAND.getPacketClass(), 0);
		CHAT_VISIBILITY_CLASS = getEnum(PacketType.Play.Client.SETTINGS.getPacketClass(), 0);
		DIFFICULTY_CLASS = getEnum(PacketType.Play.Client.SETTINGS.getPacketClass(), 1);
		ENTITY_USE_ACTION_CLASS = getEnum(PacketType.Play.Client.USE_ENTITY.getPacketClass(), 0);
		GAMEMODE_CLASS = getEnum(PacketType.Play.Server.LOGIN.getPacketClass(), 0);
		
		associate(PROTOCOL_CLASS, Protocol.class, getClientCommandConverter());
		associate(CLIENT_COMMAND_CLASS, ClientCommand.class, getClientCommandConverter());
		associate(CHAT_VISIBILITY_CLASS, ChatVisibility.class, getChatVisibilityConverter());
		associate(DIFFICULTY_CLASS, Difficulty.class, getDifficultyConverter());
		associate(ENTITY_USE_ACTION_CLASS, EntityUseAction.class, getEntityUseActionConverter());
		associate(GAMEMODE_CLASS, NativeGameMode.class, getGameModeConverter());
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
	
	// The common enum converter
	@SuppressWarnings({"rawtypes", "unchecked"})
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
