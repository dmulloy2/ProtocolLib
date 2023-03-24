package com.comphenix.protocol.wrappers.ping;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.comphenix.protocol.events.AbstractStructure;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.*;

import org.bukkit.Bukkit;

public class ServerPingRecord implements ServerPingImpl {
	private static Class<?> SERVER_PING;
	private static Class<?> PLAYER_SAMPLE_CLASS;
	private static Class<?> SERVER_DATA_CLASS;

	private static WrappedChatComponent DEFAULT_DESCRIPTION;

	private static ConstructorAccessor PING_CTOR;

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

	private Object description;
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

	public ServerPingRecord(Object handle) {
		initialize();

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
	public Object getMotD() {
		return description;
	}

	@Override
	public void setMotD(Object description) {
		this.description = description;
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
	public Object getHandle() {
		Optional<Object> playersHandle = Optional.ofNullable(playerSample != null ? SAMPLE_WRAPPER.unwrap(playerSample) : null);
		Optional<Object> versionHandle = Optional.ofNullable(serverData != null ? DATA_WRAPPER.unwrap(serverData) : null);
		Optional<Object> favHandle = Optional.ofNullable(favicon != null ? FAVICON_WRAPPER.unwrap(favicon) : null);

		return PING_CTOR.invoke(description, playersHandle, versionHandle, favHandle, enforceSafeChat);
	}
}
