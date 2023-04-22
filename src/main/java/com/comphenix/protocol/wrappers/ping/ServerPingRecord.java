package com.comphenix.protocol.wrappers.ping;

import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.codecs.WrappedCodec;
import com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ServerPingRecord implements ServerPingImpl {
	private static Class<?> SERVER_PING;
	private static Class<?> PLAYER_SAMPLE_CLASS;
	private static Class<?> SERVER_DATA_CLASS;

	private static Class<?> GSON_CLASS;
	private static MethodAccessor GSON_TO_JSON;
	private static MethodAccessor GSON_FROM_JSON;
	private static FieldAccessor DATA_SERIALIZER_GSON;
	private static Class<?> JSON_ELEMENT_CLASS;

	private static WrappedChatComponent DEFAULT_DESCRIPTION;

	private static ConstructorAccessor PING_CTOR;
	private static WrappedCodec CODEC;

	private static boolean initialized = false;

	private static void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;

		try {
			SERVER_PING = MinecraftReflection.getServerPingClass();
			PLAYER_SAMPLE_CLASS = MinecraftReflection.getServerPingPlayerSampleClass();
			SERVER_DATA_CLASS = MinecraftReflection.getServerPingServerDataClass();

			PING_CTOR = Accessors.getConstructorAccessor(SERVER_PING.getConstructors()[0]);

			DATA_WRAPPER = AutoWrapper.wrap(ServerData.class, SERVER_DATA_CLASS);
			SAMPLE_WRAPPER = AutoWrapper.wrap(PlayerSample.class, PLAYER_SAMPLE_CLASS);
			FAVICON_WRAPPER = AutoWrapper.wrap(Favicon.class, MinecraftReflection.getMinecraftClass("network.protocol.status.ServerPing$a"));

			DEFAULT_DESCRIPTION = WrappedChatComponent.fromLegacyText("A Minecraft Server");

			GSON_CLASS = MinecraftReflection.getMinecraftGsonClass();
			GSON_TO_JSON = Accessors.getMethodAccessor(GSON_CLASS, "toJson", Object.class);
			GSON_FROM_JSON = Accessors.getMethodAccessor(GSON_CLASS, "fromJson", String.class, Class.class);
			DATA_SERIALIZER_GSON = Accessors.getFieldAccessor(MinecraftReflection.getPacketDataSerializerClass(), GSON_CLASS, true);
			JSON_ELEMENT_CLASS = MinecraftReflection.getLibraryClass("com.google.gson.JsonElement");
			CODEC = WrappedCodec.fromHandle(Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getCodecClass(), false).get(null));
		} catch (Exception ex) {
			ex.printStackTrace(); // TODO
		}
	}

	public static final class PlayerSample {
		public int max;
		public int online;
		public Object sample;
	}

	public static final class ServerData {
		public String name;
		public int protocol;
	}

	public static final class Favicon {
		public byte[] iconBytes;
	}

	private static AutoWrapper<PlayerSample> SAMPLE_WRAPPER;

	private static AutoWrapper<ServerData> DATA_WRAPPER;

	private static AutoWrapper<Favicon> FAVICON_WRAPPER;

	private WrappedChatComponent description;
	private PlayerSample playerSample;
	private ServerData serverData;
	private Favicon favicon;
	private boolean enforceSafeChat;
	private boolean playersVisible = true;

	private static ServerData defaultData() {
		ServerData data = new ServerData();
		data.name = MinecraftVersion.getCurrentVersion().toString();
		data.protocol = MinecraftProtocolVersion.getCurrentVersion();
		return data;
	}

	private static PlayerSample defaultSample() {
		PlayerSample sample = new PlayerSample();
		sample.max = Bukkit.getMaxPlayers();
		sample.online = Bukkit.getOnlinePlayers().size();
		sample.sample = null;
		return sample;
	}

	private static Favicon defaultFavicon() {
		Favicon favicon = new Favicon();
		favicon.iconBytes = new byte[0];
		return favicon;
	}

	public static ServerPingRecord fromJson(String json) {

		Object jsonElement = GSON_FROM_JSON.invoke(DATA_SERIALIZER_GSON.get(null), json, JSON_ELEMENT_CLASS);

		Object decoded = CODEC.parse(jsonElement, WrappedDynamicOps.json(false)).getOrThrow(e -> new IllegalStateException("Failed to decode: " + e));
		return new ServerPingRecord(decoded);
	}

	public ServerPingRecord(Object handle) {
		initialize();
		if(handle.getClass() != SERVER_PING) {
			throw new IllegalArgumentException("Expected handle of type " + SERVER_PING.getName() + " but got " + handle.getClass().getName());
		}

		StructureModifier<Object> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);
		InternalStructure structure = new InternalStructure(handle, modifier);

		this.description = structure.getChatComponents().readSafely(0);

		StructureModifier<Optional<Object>> optionals = structure.getOptionals(Converters.passthrough(Object.class));

		Optional<Object> sampleHandle = optionals.readSafely(0);
		if (sampleHandle.isPresent()) {
			this.playerSample = SAMPLE_WRAPPER.wrap(sampleHandle.get());
		} else {
			this.playerSample = defaultSample();
		}

		Optional<Object> dataHandle = optionals.readSafely(1);
		if (dataHandle.isPresent()) {
			this.serverData = DATA_WRAPPER.wrap(dataHandle.get());
		} else {
			this.serverData = defaultData();
		}

		Optional<Object> faviconHandle = optionals.readSafely(2);
		if (faviconHandle.isPresent()) {
			this.favicon = FAVICON_WRAPPER.wrap(faviconHandle.get());
		} else {
			this.favicon = defaultFavicon();
		}

		this.enforceSafeChat = structure.getBooleans().readSafely(0);
	}

	public ServerPingRecord() {
		initialize();

		this.description = DEFAULT_DESCRIPTION;
		this.favicon = defaultFavicon();
	}

	@Override
	public WrappedChatComponent getMotD() {
		return description;
	}

	@Override
	public void setMotD(Object description) {
		if(description instanceof WrappedChatComponent) {
			this.description = (WrappedChatComponent) description;
		} else {
			this.description = WrappedChatComponent.fromHandle(description);
		}
	}

	@Override
	public int getPlayersMaximum() {
		return playerSample.max;
	}

	@Override
	public void setPlayersMaximum(int maxPlayers) {
		playerSample.max = maxPlayers;
	}

	@Override
	public int getPlayersOnline() {
		return playerSample.online;
	}

	@Override
	public void setPlayersOnline(int onlineCount) {
		playerSample.online = onlineCount;
	}

	@Override
	public Object getPlayers() {
		return playerSample;
	}

	@Override
	public void setPlayers(Object playerSample) {
		this.playerSample.sample = playerSample;
	}

	@Override
	public String getVersionName() {
		return serverData.name;
	}

	@Override
	public void setVersionName(String versionName) {
		serverData.name = versionName;
	}

	@Override
	public int getVersionProtocol() {
		return serverData.protocol;
	}

	@Override
	public void setVersionProtocol(int protocolVersion) {
		serverData.protocol = protocolVersion;
	}

	@Override
	public String getFavicon() {
		return new String(favicon.iconBytes, StandardCharsets.UTF_8);
	}

	@Override
	public void setFavicon(String favicon) {
		this.favicon.iconBytes = favicon.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public boolean isEnforceSecureChat() {
		return enforceSafeChat;
	}

	@Override
	public void setEnforceSecureChat(boolean safeChat) {
		this.enforceSafeChat = safeChat;
	}

	@Override
	public void resetPlayers() {
		this.playerSample = defaultSample();
	}

	@Override
	public void resetVersion() {
		this.serverData = defaultData();
	}

	@Override
	public boolean arePlayersVisible() {
		return playersVisible;
	}

	@Override
	public void setPlayersVisible(boolean visible) {
		this.playersVisible = visible;
	}

	@Override
	public String getJson() {
		Object encoded = CODEC.encode(getHandle(), WrappedDynamicOps.json(false)).getOrThrow(e -> new IllegalStateException("Failed to encode: " + e));
		return (String) GSON_TO_JSON.invoke(DATA_SERIALIZER_GSON.get(null), encoded);
	}

	@Override
	public Object getHandle() {
		Optional<Object> playersHandle = Optional.ofNullable(playerSample != null ? SAMPLE_WRAPPER.unwrap(playerSample) : null);
		Optional<Object> versionHandle = Optional.ofNullable(serverData != null ? DATA_WRAPPER.unwrap(serverData) : null);
		Optional<Object> favHandle = Optional.ofNullable(favicon != null ? FAVICON_WRAPPER.unwrap(favicon) : null);

		return PING_CTOR.invoke(description.getHandle(), playersHandle, versionHandle, favHandle, enforceSafeChat);
	}
}
