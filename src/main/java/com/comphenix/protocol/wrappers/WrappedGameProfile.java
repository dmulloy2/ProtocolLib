package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.PluginContext;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.instances.MinecraftGenerator;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.collection.ConvertedMultimap;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents a wrapper for a game profile.
 * @author Kristian
 */
public class WrappedGameProfile extends AbstractWrapper {
    public static final ReportType REPORT_INVALID_UUID = new ReportType("Plugin %s created a profile with '%s' as an UUID.");

    private static final Class<?> GAME_PROFILE = MinecraftReflection.getGameProfileClass();

    private static final ConstructorAccessor CREATE_STRING_STRING = Accessors.getConstructorAccessorOrNull(
            GAME_PROFILE, String.class, String.class);
    private static final ConstructorAccessor CREATE_UUID_STRING = Accessors.getConstructorAccessorOrNull(
            GAME_PROFILE, UUID.class, String.class);
    private static final ConstructorAccessor CREATE_UUID_STRING_PROPERTIES = Accessors.getConstructorAccessorOrNull(
            GAME_PROFILE, UUID.class, String.class, MinecraftReflection.getGameProfilePropertyMapClass());

    private static final FieldAccessor GET_UUID_STRING = Accessors.getFieldAccessorOrNull(
            GAME_PROFILE, "id", String.class);

    private static MethodAccessor GET_ID;
    private static MethodAccessor GET_NAME;
    private static MethodAccessor GET_PROPERTIES;
    private static MethodAccessor IS_COMPLETE;

    static {
        GET_ID = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "getId");
        if (GET_ID == null) {
            GET_ID = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "id");
            GET_NAME = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "name");
            GET_PROPERTIES = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "properties");
            IS_COMPLETE = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "complete");
        } else {
            GET_NAME = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "getName");
            GET_PROPERTIES = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "getProperties");
            IS_COMPLETE = Accessors.getMethodAccessorOrNull(GAME_PROFILE, "isComplete");
        }
    }

    // Fetching game profile
    private static FieldAccessor PLAYER_PROFILE;
    private static FieldAccessor OFFLINE_PROFILE;

    // Property map
    private Multimap<String, WrappedSignedProperty> propertyMap;

    // Parsed UUID
    private volatile UUID parsedUUID;

    // Profile from a handle
    private WrappedGameProfile(Object profile) {
        super(GAME_PROFILE);
        setHandle(profile);
    }

    /**
     * Retrieve the associated game profile of a player.
     * <p>
     * Note that this may not exist in the current Minecraft version.
     * 
     * @param player - the player.
     * @return The game profile.
     */
    public static WrappedGameProfile fromPlayer(Player player) {
        FieldAccessor accessor = PLAYER_PROFILE;
        if (accessor == null) {
            accessor = Accessors.getFieldAccessor(MinecraftReflection.getEntityHumanClass(), GAME_PROFILE, true);
            PLAYER_PROFILE = accessor;
        }

        Object nmsPlayer = BukkitUnwrapper.getInstance().unwrapItem(player);
        return WrappedGameProfile.fromHandle(PLAYER_PROFILE.get(nmsPlayer));
    }

    /**
     * Retrieve the associated game profile of an offline player.
     * <p>
     * Note that this may not exist in the current Minecraft version.
     * 
     * @param player - the offline player.
     * @return The game profile.
     */
    public static WrappedGameProfile fromOfflinePlayer(OfflinePlayer player) {
        FieldAccessor accessor = OFFLINE_PROFILE;
        if (accessor == null) {
            accessor = Accessors.getFieldAccessor(player.getClass(), GAME_PROFILE, true);
            OFFLINE_PROFILE = accessor;
        }

        return WrappedGameProfile.fromHandle(OFFLINE_PROFILE.get(player));
    }

    /**
     * Construct a new game profile with the given properties.
     * <p>
     * Note that this constructor is very lenient when parsing UUIDs for backwards compatibility reasons.
     * IDs that cannot be parsed as an UUID will be hashed and form a version 3 UUID instead.
     * <p>
     * This method is deprecated for Minecraft 1.7.8 and above.
     * 
     * @param id - the UUID of the player.
     * @param name - the name of the player.
     */
    @Deprecated
    public WrappedGameProfile(String id, String name) {
        this(parseUUID(id), name);
    }

    private static Object createHandle(UUID uuid, String name, Multimap<String, WrappedSignedProperty> properties) {
        if (CREATE_STRING_STRING != null) {
            return CREATE_STRING_STRING.invoke(uuid != null ? uuid.toString() : null, name);
        }

        if (CREATE_UUID_STRING == null) {
            throw new IllegalArgumentException("Unsupported GameProfile constructor.");
        }

        if (!MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
            return CREATE_UUID_STRING.invoke(uuid, name);
        }

        // 1.20.2+ requires all fields to have a value: null uuid -> UUID(0,0), null name -> empty name
        // it's not allowed to pass null for both, so we need to pre-check that
        if (uuid == null && (name == null || name.isEmpty())) {
            throw new IllegalArgumentException("Name and ID cannot both be blank");
        }

        // 1.21.9+ made PropertyMap's underlying map immutable, so we need to override it with a mutable map
        if (MinecraftVersion.v1_21_9.atOrAbove()) {
            return CREATE_UUID_STRING_PROPERTIES.invoke(uuid == null ? MinecraftGenerator.SYS_UUID : uuid, name == null ? "" : name,
                    convertPropertyMap(properties));
        }

        return CREATE_UUID_STRING.invoke(uuid == null ? MinecraftGenerator.SYS_UUID : uuid, name == null ? "" : name);
    }

    private static Object convertPropertyMap(Multimap<String, WrappedSignedProperty> properties) {
        com.comphenix.protocol.wrappers.MutablePropertyMap map =
                new com.comphenix.protocol.wrappers.MutablePropertyMap();

        if (properties == null || properties.isEmpty()) {
            return map;
        }

        for (String key : properties.keySet()) {
            for (WrappedSignedProperty property : properties.get(key)) {
                map.put(key, (com.mojang.authlib.properties.Property) property.getHandle());
            }
        }

        return map;
    }

    /**
     * Construct a new game profile with the given properties.
     * <p>
     * Note that at least one of the parameters must be non-null.
     * 
     * @param uuid - the UUID of the player, or NULL.
     * @param name - the name of the player, or NULL.
     */
    public WrappedGameProfile(UUID uuid, String name) {
        super(GAME_PROFILE);
        setHandle(createHandle(uuid, name, null));
    }

    public WrappedGameProfile(UUID uuid, String name, Multimap<String, WrappedSignedProperty> properties) {
        super(GAME_PROFILE);
        setHandle(createHandle(uuid, name, properties));
    }

    /**
     * Construct a wrapper around an existing game profile.
     * 
     * @param handle - the underlying profile, or NULL.
     * @return A wrapper around an existing game profile.
     */
    public static WrappedGameProfile fromHandle(Object handle) {
        if (handle == null)
            return null;

        WrappedGameProfile delegate = new WrappedGameProfile(handle);
        if (MinecraftVersion.v1_21_9.atOrAbove()) {
            return new WrappedGameProfile(delegate.getUUID(), delegate.getName(), delegate.getProperties());
        } else {
            return delegate;
        }
    }

    /**
     * Parse an UUID using very lax rules, as specified in {@link #WrappedGameProfile(UUID, String)}.
     * 
     * @param id - text.
     * @return The corresponding UUID.
     * @throws IllegalArgumentException If we cannot parse the text.
     */
    private static UUID parseUUID(String id) {
        if (id == null) return null;

        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // Warn once every hour (per plugin)
            ProtocolLibrary.getErrorReporter()
                .reportWarning(WrappedGameProfile.class, Report.newBuilder(REPORT_INVALID_UUID)
                .rateLimit(1, TimeUnit.HOURS)
                .messageParam(PluginContext.getPluginCaller(new Exception()), id));
            return UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Retrieve the UUID of the player.
     * <p>
     * Note that Minecraft 1.7.5 and earlier doesn't use UUIDs internally, and it may not be possible to convert the string to an UUID.
     * <p>
     * We use the same lax conversion as in {@link #WrappedGameProfile(String, String)}.
     * 
     * @return The UUID, or NULL if the UUID is NULL.
     * @throws IllegalStateException If we cannot parse the internal ID as an UUID.
     */
    public UUID getUUID() {
        UUID uuid = parsedUUID;

        if (uuid == null) {
            try {
                if (GET_UUID_STRING != null) {
                    uuid = parseUUID(getId());
                } else if (GET_ID != null) {
                    uuid = (UUID) GET_ID.invoke(handle);
                    if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove() && MinecraftGenerator.SYS_UUID.equals(uuid)) {
                        // see CraftPlayerProfile
                        uuid = null;
                    }
                } else {
                    throw new IllegalStateException("Unsupported getId() method");
                }

                // Cache for later
                parsedUUID = uuid;
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Cannot parse ID " + getId() + " as an UUID in player profile " + getName(), e);
            }
        }

        return uuid;
    }

    /**
     * Retrieve the textual representation of the player's UUID.
     * <p>
     * Note that there's nothing stopping plugins from creating non-standard UUIDs.
     * <p>
     * In Minecraft 1.7.8 and later, this simply returns the string form of {@link #getUUID()}.
     * 
     * @return The UUID of the player, or NULL if not computed.
     */
    public String getId() {
        if (GET_UUID_STRING != null) {
            return (String) GET_UUID_STRING.get(handle);
        } else if (GET_ID != null) {
            UUID uuid = getUUID();
            return uuid != null ? uuid.toString() : null;
        } else {
            throw new IllegalStateException("Unsupported getId() method");
        }
    }

    /**
     * Retrieve the name of the player.
     * 
     * @return The player name.
     */
    public String getName() {
        if (GET_NAME != null) {
            String name = (String) GET_NAME.invoke(handle);
            if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove() && name != null && name.isEmpty()) {
                // see CraftPlayerProfile
                name = null;
            }
            return name;
        } else {
            throw new IllegalStateException("Unsupported getName() method");
        }
    }

    /**
     * Retrieve the property map of signed values.
     * 
     * @return Property map.
     */
    // In the protocol hack and 1.8 it is a ForwardingMultimap
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Multimap<String, WrappedSignedProperty> getProperties() {
        Multimap<String, WrappedSignedProperty> result = propertyMap;

        if (result == null) {
            Multimap properties = (Multimap) GET_PROPERTIES.invoke(handle);

            Multimap inner;
            if (MinecraftVersion.v1_21_9.atOrAbove()) {
                inner = properties;
            } else {
                inner = GuavaWrappers.getBukkitMultimap(properties);
            }

            result = new ConvertedMultimap<String, Object, WrappedSignedProperty>(inner) {
                @Override
                protected Object toInner(WrappedSignedProperty outer) {
                    return outer.handle;
                }

                @Override
                protected Object toInnerObject(Object outer) {
                    if (outer instanceof WrappedSignedProperty) {
                        return toInner((WrappedSignedProperty) outer);
                    }
                    return outer;
                }

                @Override
                protected WrappedSignedProperty toOuter(Object inner) {
                    return WrappedSignedProperty.fromHandle(inner);
                }
            };
            propertyMap = result;
        }
        return result;
    }

    /**
     * Construct a new game profile with the same ID, but different name.
     * 
     * @param name - the new name of the profile to create.
     * @return The new game profile.
     */
    public WrappedGameProfile withName(String name) {
        return new WrappedGameProfile(getId(), name);
    }

    /**
     * Construct a new game profile with the same name, but different id.
     * 
     * @param id - the new id of the profile to create.
     * @return The new game profile.
     */
    public WrappedGameProfile withId(String id) {
        return new WrappedGameProfile(id, getName());
    }

    /**
     * Determine if the game profile contains both an UUID and a name.
     * 
     * @return TRUE if it does, FALSE otherwise.
     */
    public boolean isComplete() {
        return (Boolean) IS_COMPLETE.invoke(handle);
    }

    @Override
    public String toString() {
        return String.valueOf(getHandle());
    }

    @Override
    public int hashCode() {
        // Mojang's hashCode() is broken, it doesn't handle NULL id or name. So we implement our own
        return Objects.hashCode(getId(), getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof WrappedGameProfile) {
            WrappedGameProfile other = (WrappedGameProfile) obj;
            return Objects.equal(getHandle(), other.getHandle());
        }

        return false;
    }
}
