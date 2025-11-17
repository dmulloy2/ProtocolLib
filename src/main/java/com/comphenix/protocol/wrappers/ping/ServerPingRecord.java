package com.comphenix.protocol.wrappers.ping;

import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.MinecraftGenerator;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.codecs.WrappedCodec;
import com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ServerPingRecord implements ServerPingImpl {
    private static Class<?> SERVER_PING;
    private static Class<?> PLAYER_SAMPLE_CLASS;
    private static Class<?> SERVER_DATA_CLASS;
    private static Class<?> NAME_AND_ID_CLASS;

    private static Class<?> GSON_CLASS;
    private static MethodAccessor GSON_TO_JSON;
    private static MethodAccessor GSON_FROM_JSON;
    private static FieldAccessor DATA_SERIALIZER_GSON;
    private static Class<?> JSON_ELEMENT_CLASS;

    private static WrappedChatComponent DEFAULT_DESCRIPTION;

    private static ConstructorAccessor PING_CTOR;
    private static WrappedCodec CODEC;

    private static EquivalentConverter<List<WrappedGameProfile>> PROFILE_LIST_CONVERTER;

    private static boolean initialized = false;
    private static final Object lock = new Object();

    private static void initialize() {
        if (initialized) {
            return;
        }

        synchronized (lock) {
            // may have been initialized while waiting for the lock
            if (initialized) {
                return;
            }

            try {
                SERVER_PING = MinecraftReflection.getServerPingClass();
                PLAYER_SAMPLE_CLASS = MinecraftReflection.getServerPingPlayerSampleClass();
                SERVER_DATA_CLASS = MinecraftReflection.getServerPingServerDataClass();

                PING_CTOR = Accessors.getConstructorAccessor(SERVER_PING.getConstructors()[0]);

                DATA_WRAPPER = AutoWrapper.wrap(ServerData.class, SERVER_DATA_CLASS);
                SAMPLE_WRAPPER = AutoWrapper.wrap(PlayerSample.class, PLAYER_SAMPLE_CLASS);
                FAVICON_WRAPPER = AutoWrapper.wrap(Favicon.class, MinecraftReflection.getMinecraftClass("network.protocol.status.ServerPing$a", "network.protocol.status.ServerStatus$Favicon"));

                if (MinecraftVersion.v1_21_10.atOrAbove()) {
                    NAME_AND_ID_CLASS = MinecraftReflection.getMinecraftClass("server.players.NameAndId");
                    NAME_AND_ID_WRAPPER = AutoWrapper.wrap(NameAndId.class, NAME_AND_ID_CLASS);

                    PROFILE_LIST_CONVERTER = BukkitConverters.getListConverter(new EquivalentConverter<>() {
                        @Override
                        public Object getGeneric(WrappedGameProfile specific) {
                            NameAndId wrapper = new NameAndId(specific.getUUID(), specific.getName());
                            return NAME_AND_ID_WRAPPER.getGeneric(wrapper);
                        }

                        @Override
                        public WrappedGameProfile getSpecific(Object generic) {
                            NameAndId wrapper = NAME_AND_ID_WRAPPER.getSpecific(generic);
                            return new WrappedGameProfile(wrapper.id, wrapper.name);
                        }

                        @Override
                        public Class<WrappedGameProfile> getSpecificType() {
                            return WrappedGameProfile.class;
                        }
                    });
                } else {
                    PROFILE_LIST_CONVERTER = BukkitConverters.getListConverter(BukkitConverters.getWrappedGameProfileConverter());
                }

                DEFAULT_DESCRIPTION = WrappedChatComponent.fromLegacyText("A Minecraft Server");

                GSON_CLASS = MinecraftReflection.getMinecraftGsonClass();
                GSON_TO_JSON = Accessors.getMethodAccessor(GSON_CLASS, "toJson", Object.class);
                GSON_FROM_JSON = Accessors.getMethodAccessor(GSON_CLASS, "fromJson", String.class, Class.class);
                DATA_SERIALIZER_GSON = Accessors.getFieldAccessor(MinecraftReflection.getPacketDataSerializerClass(), GSON_CLASS, true);
                JSON_ELEMENT_CLASS = MinecraftReflection.getLibraryClass("com.google.gson.JsonElement");
                CODEC = WrappedCodec.fromHandle(Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getCodecClass(), false).get(null));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to initialize Server Ping", ex);
            } finally {
                initialized = true;
            }
        }
    }

    public static final class PlayerSample {
        public int max;
        public int online;
        public Object sample;

        public PlayerSample(int max, int online, Object sample) {
            this.max = max;
            this.online = online;
            this.sample = sample;
        }

        public PlayerSample() {
            this(0, 0, null);
        }
    }

    public static final class ServerData {
        public String name;
        public int protocol;

        public ServerData(String name, int protocol) {
            this.name = name;
            this.protocol = protocol;
        }

        public ServerData() {
            this("", 0);
        }
    }

    static final byte[] EMPTY_FAVICON = new byte[0];

    public static final class Favicon {
        public byte[] iconBytes;

        public Favicon(byte[] iconBytes) {
            this.iconBytes = iconBytes;
        }

        public Favicon() {
            this(EMPTY_FAVICON);
        }
    }

    public static final class NameAndId {
        public UUID id;
        public String name;

        public NameAndId(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public NameAndId() {
            this(MinecraftGenerator.SYS_UUID, "");
        }
    }

    private static AutoWrapper<PlayerSample> SAMPLE_WRAPPER;
    private static AutoWrapper<ServerData> DATA_WRAPPER;
    private static AutoWrapper<Favicon> FAVICON_WRAPPER;
    private static AutoWrapper<NameAndId> NAME_AND_ID_WRAPPER;

    private WrappedChatComponent description;
    private PlayerSample playerSample;
    private ServerData serverData;
    private Favicon favicon;
    private boolean enforceSafeChat;
    private boolean playersVisible = true;

    private static ServerData defaultData() {
        String name = MinecraftVersion.getCurrentVersion().toString();
        int protocol = MinecraftProtocolVersion.getCurrentVersion();

        return new ServerData(name, protocol);
    }

    private static PlayerSample defaultSample() {
        int max = Bukkit.getMaxPlayers();
        int online = Bukkit.getOnlinePlayers().size();

        return new PlayerSample(max, online, new ArrayList<>());
    }

    private static Favicon defaultFavicon() {
        return new Favicon();
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
        this.playerSample = sampleHandle.isPresent() ? SAMPLE_WRAPPER.wrap(sampleHandle.get()) : defaultSample();

        Optional<Object> dataHandle = optionals.readSafely(1);
        this.serverData = dataHandle.isPresent() ? DATA_WRAPPER.wrap(dataHandle.get()) : defaultData();

        Optional<Object> faviconHandle = optionals.readSafely(2);
        this.favicon = faviconHandle.isPresent() ? FAVICON_WRAPPER.wrap(faviconHandle.get()) : defaultFavicon();

        this.enforceSafeChat = structure.getBooleans().readSafely(0);
    }

    public ServerPingRecord() {
        initialize();

        this.description = DEFAULT_DESCRIPTION;
        this.playerSample = defaultSample();
        this.serverData = defaultData();
        this.favicon = defaultFavicon();
    }

    @Override
    public WrappedChatComponent getMotD() {
        return description;
    }

    @Override
    public void setMotD(WrappedChatComponent description) {
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
    public ImmutableList<WrappedGameProfile> getPlayers() {
        if (playerSample.sample == null) {
            return ImmutableList.of();
        }

        List<WrappedGameProfile> list = PROFILE_LIST_CONVERTER.getSpecific(playerSample.sample);
        if (list == null) {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(list);
    }

    @Override
    public void setPlayers(Iterable<? extends WrappedGameProfile> playerSample) {
        if (playerSample == null) {
            this.playerSample.sample = null;
            return;
        }

        List<WrappedGameProfile> list = Converters.toList(playerSample);
        this.playerSample.sample = PROFILE_LIST_CONVERTER.getGeneric(list);
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
    public WrappedServerPing.CompressedImage getFavicon() {
        return new WrappedServerPing.CompressedImage("data:image/png;base64", favicon.iconBytes);
    }

    @Override
    public void setFavicon(WrappedServerPing.CompressedImage favicon) {
        this.favicon.iconBytes = favicon.getDataCopy();
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
        WrappedChatComponent wrappedDescription = description != null ? description : DEFAULT_DESCRIPTION;
        Object descHandle = wrappedDescription.getHandle();

        Optional<Object> playersHandle = Optional.ofNullable(SAMPLE_WRAPPER.unwrap(playerSample != null ? playerSample : new ArrayList<>())); // sample has to be non-null in handle
        Optional<Object> versionHandle = Optional.ofNullable(serverData != null ? DATA_WRAPPER.unwrap(serverData) : null);
        Optional<Object> favHandle = Optional.ofNullable(favicon != null ? FAVICON_WRAPPER.unwrap(favicon) : null);

        return PING_CTOR.invoke(descHandle, playersHandle, versionHandle, favHandle, enforceSafeChat);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ServerPingRecord)) {
            return false;
        }
        ServerPingRecord other = (ServerPingRecord) obj;

        return Objects.equals(description, other.description)
                && Objects.equals(playerSample, other.playerSample)
                && Objects.equals(serverData, other.serverData)
                && ((favicon == null && other.favicon.iconBytes == null)
                || ((favicon != null) == (other.favicon != null) && Arrays.equals(favicon.iconBytes, other.favicon.iconBytes)));
    }
}
