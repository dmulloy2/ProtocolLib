package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

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
}
