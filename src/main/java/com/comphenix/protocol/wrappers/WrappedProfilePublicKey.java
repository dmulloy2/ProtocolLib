package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.security.PublicKey;
import java.time.Instant;

public class WrappedProfilePublicKey extends AbstractWrapper {

	private static final Class<?> HANDLE_TYPE = MinecraftReflection.getProfilePublicKeyClass();
	private static final Class<?> KEY_DATA_TYPE = MinecraftReflection.getProfilePublicKeyDataClass();

	private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(HANDLE_TYPE, KEY_DATA_TYPE);
	private static final FieldAccessor DATA_ACCESSOR = Accessors.getFieldAccessor(HANDLE_TYPE, KEY_DATA_TYPE, true);

	public WrappedProfilePublicKey(Object handle) {
		super(HANDLE_TYPE);
		this.setHandle(handle);
	}

	public WrappedProfilePublicKey(WrappedProfileKeyData keyData) {
		this(CONSTRUCTOR.invoke(keyData.getHandle()));
	}

	public WrappedProfileKeyData getKeyData() {
		return new WrappedProfileKeyData(DATA_ACCESSOR.get(this.getHandle()));
	}

	public void setKeyData(WrappedProfileKeyData keyData) {
		DATA_ACCESSOR.set(this.getHandle(), keyData.getHandle());
	}

	public static class WrappedProfileKeyData extends AbstractWrapper {

		private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(
				KEY_DATA_TYPE,
				Instant.class, PublicKey.class, byte[].class);

		private final StructureModifier<Object> modifier;

		public WrappedProfileKeyData(Object handle) {
			super(KEY_DATA_TYPE);
			this.setHandle(handle);
			this.modifier = new StructureModifier<>(KEY_DATA_TYPE).withTarget(handle);
		}

		public WrappedProfileKeyData(Instant expireTime, PublicKey key, byte[] signature) {
			this(CONSTRUCTOR.invoke(expireTime, key, signature));
		}

		public Instant getExpireTime() {
			return this.modifier.<Instant>withType(Instant.class).read(0);
		}

		public void setExpireTime(Instant expireTime) {
			this.modifier.<Instant>withType(Instant.class).write(0, expireTime);
		}

		public boolean isExpired() {
			return this.getExpireTime().isBefore(Instant.now());
		}

		public PublicKey getKey() {
			return this.modifier.<PublicKey>withType(PublicKey.class).read(0);
		}

		public void setKey(PublicKey key) {
			this.modifier.<PublicKey>withType(PublicKey.class).write(0, key);
		}

		public byte[] getSignature() {
			return this.modifier.<byte[]>withType(byte[].class).read(0);
		}

		public void setSignature(byte[] signature) {
			this.modifier.<byte[]>withType(byte[].class).write(0, signature);
		}
	}
}
