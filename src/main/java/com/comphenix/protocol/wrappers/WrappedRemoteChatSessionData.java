package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.ExactReflection;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A wrapper around the remote chat session.
 *
 * @since 1.19.3
 */
public class WrappedRemoteChatSessionData extends AbstractWrapper {
    private final static Class<?> HANDLE_TYPE = MinecraftReflection.getRemoteChatSessionDataClass();
    private static ConstructorAccessor CONSTRUCTOR;
    private StructureModifier<Object> modifier;
    private static MethodAccessor PLAYER_GET_HANDLE;
    private static FieldAccessor REMOTE_CHAT_SESSION_FIELD;
    private static MethodAccessor AS_DATA_METHOD;

    /**
     * Constructs a new profile public key wrapper directly from a nms RemoteChatSession.Data/RemoteChatSession.a object.
     *
     * @param handle the handle to create the wrapper from.
     */
    public WrappedRemoteChatSessionData(Object handle) {
        super(HANDLE_TYPE);
        this.setHandle(handle);
    }

    public WrappedRemoteChatSessionData(UUID sessionId, WrappedProfilePublicKey.WrappedProfileKeyData profilePublicKey) {
        super(HANDLE_TYPE);
        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(
                    this.getHandleType(),
                    UUID.class, MinecraftReflection.getProfilePublicKeyDataClass());
        }

        this.setHandle(CONSTRUCTOR.invoke(sessionId, profilePublicKey.getHandle()));
    }

    /**
     * Retrieves the id of the current session
     * @return session id
     */
    public UUID getSessionID() {
        return this.modifier.<UUID>withType(UUID.class).read(0);
    }

    /**
     * Set the id for this session
     * @param sessionId new session id
     */
    public void setSessionID(UUID sessionId) {
        this.modifier.<UUID>withType(UUID.class).write(0, sessionId);
    }

    /**
     * Retrieves the ProfileKeyData
     * @return the public key data for this session
     */
    public WrappedProfilePublicKey.WrappedProfileKeyData getProfilePublicKey() {
        return this.modifier.withType(MinecraftReflection.getProfilePublicKeyDataClass(), BukkitConverters.getWrappedPublicKeyDataConverter()).read(0);
    }

    /**
     * Set the profile key data for this session
     * @param data ProfileKeyData
     */
    public void setProfilePublicKey(WrappedProfilePublicKey.WrappedProfileKeyData data) {
        this.modifier.withType(MinecraftReflection.getProfilePublicKeyDataClass(), BukkitConverters.getWrappedPublicKeyDataConverter()).write(0, data);
    }

    @Override
    protected void setHandle(Object handle) {
        super.setHandle(handle);
        this.modifier = new StructureModifier<>(HANDLE_TYPE).withTarget(handle);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WrappedRemoteChatSessionData) {
            return handle.equals(((WrappedRemoteChatSessionData) obj).getHandle());
        }
        return false;
    }

    @Override
    public String toString() {
        return handle.toString();
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    /**
     * Retrieves the current session data of the player
     * @since 1.19.4
     * @return Wrapper for session data or null if not present
     */
    @Nullable
    public static WrappedRemoteChatSessionData fromPlayer(Player player) {
        if(PLAYER_GET_HANDLE == null) {
            PLAYER_GET_HANDLE = Accessors.getMethodAccessor(MinecraftReflection.getCraftPlayerClass(), "getHandle");
        }
        if(REMOTE_CHAT_SESSION_FIELD == null) {
            REMOTE_CHAT_SESSION_FIELD = Accessors.getFieldAccessor(MinecraftReflection.getEntityPlayerClass(), MinecraftReflection.getRemoteChatSessionClass(), true);
        }
        if(AS_DATA_METHOD == null) {
            AS_DATA_METHOD = Accessors.getMethodAccessor(FuzzyReflection.fromClass(MinecraftReflection.getRemoteChatSessionClass(), true).getMethodListByParameters(HANDLE_TYPE).get(0));
        }
        Object handle = PLAYER_GET_HANDLE.invoke(player);
        Object remoteChatSession = REMOTE_CHAT_SESSION_FIELD.get(handle);
        if(remoteChatSession == null) {
            return null;
        }
        Object remoteChatSessionData = AS_DATA_METHOD.invoke(remoteChatSession);
        return remoteChatSessionData == null ? null : new WrappedRemoteChatSessionData(remoteChatSessionData);
    }
}
