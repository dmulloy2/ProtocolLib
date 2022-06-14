package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.bukkit.entity.Player;

/**
 * A wrapper around the profile public key.
 *
 * @since 1.19
 */
public class WrappedProfilePublicKey extends AbstractWrapper {

	private static ConstructorAccessor CONSTRUCTOR;
	private static FieldAccessor DATA_ACCESSOR;

	// lazy initialized when needed
	private static FieldAccessor PROFILE_KEY_ACCESSOR;

	/**
	 * Constructs a new profile public key wrapper directly from a nms ProfilePublicKey object.
	 *
	 * @param handle the handle to create the wrapper from.
	 */
	public WrappedProfilePublicKey(Object handle) {
		super(MinecraftReflection.getProfilePublicKeyClass());
		this.setHandle(handle);
	}

	/**
	 * Constructs a new profile public key wrapper holding the given key data.
	 *
	 * @param keyData the data of the key.
	 */
	public WrappedProfilePublicKey(WrappedProfileKeyData keyData) {
		super(MinecraftReflection.getProfilePublicKeyClass());

		if (CONSTRUCTOR == null) {
			CONSTRUCTOR = Accessors.getConstructorAccessor(this.getHandleType(),
					MinecraftReflection.getProfilePublicKeyDataClass());
		}
		this.setHandle(CONSTRUCTOR.invoke(keyData.getHandle()));
	}

	/**
	 * Retrieves the profile public key from the given player instance.
	 *
	 * @param player the player to get the key of.
	 * @return a wrapper around the public key of the given player.
	 */
	public static WrappedProfilePublicKey ofPlayer(Player player) {
		FieldAccessor accessor = PROFILE_KEY_ACCESSOR;
		if (accessor == null) {
			accessor = Accessors.getFieldAccessor(MinecraftReflection.getEntityHumanClass(),
					MinecraftReflection.getProfilePublicKeyClass(), true);
			PROFILE_KEY_ACCESSOR = accessor;
		}

		Object nmsPlayer = BukkitUnwrapper.getInstance().unwrapItem(player);
		return new WrappedProfilePublicKey(accessor.get(nmsPlayer));
	}

	/**
	 * Get a wrapper around the key data stored in this public key.
	 *
	 * @return the data of this key.
	 */
	public WrappedProfileKeyData getKeyData() {
		if (DATA_ACCESSOR == null) {
			DATA_ACCESSOR = Accessors.getFieldAccessor(this.getHandleType(),
					MinecraftReflection.getProfilePublicKeyDataClass(), true);
		}
		return new WrappedProfileKeyData(DATA_ACCESSOR.get(this.getHandle()));
	}

	/**
	 * Sets the data of this key. Note that changing the key data might lead to unexpected issues with the server, for
	 * example chat message validation to fail.
	 *
	 * @param keyData the new data of this key.
	 */
	public void setKeyData(WrappedProfileKeyData keyData) {
		if (DATA_ACCESSOR == null) {
			DATA_ACCESSOR = Accessors.getFieldAccessor(this.getHandleType(),
					MinecraftReflection.getProfilePublicKeyDataClass(), true);
		}
		DATA_ACCESSOR.set(this.getHandle(), keyData.getHandle());
	}

	/**
	 * A wrapper around the data stored in a profile key.
	 *
	 * @since 1.19
	 */
	public static class WrappedProfileKeyData extends AbstractWrapper {

		private static ConstructorAccessor CONSTRUCTOR;
		private static final Encoder MIME_ENCODER = Base64.getMimeEncoder(76, "\n".getBytes(StandardCharsets.UTF_8));

		private final StructureModifier<Object> modifier;

		/**
		 * Constructs a new key data instance directly from the given nms KeyData object.
		 *
		 * @param handle the handle to create the wrapper from.
		 */
		public WrappedProfileKeyData(Object handle) {
			super(MinecraftReflection.getProfilePublicKeyDataClass());
			this.setHandle(handle);
			this.modifier = new StructureModifier<>(MinecraftReflection.getProfilePublicKeyDataClass()).withTarget(handle);
		}

		/**
		 * Constructs a new data wrapper instance using the given parameters.
		 *
		 * @param expireTime the instant when the key data expires.
		 * @param key        the public key used to verify incoming data.
		 * @param signature  the signature of the public key.
		 */
		public WrappedProfileKeyData(Instant expireTime, PublicKey key, byte[] signature) {
			super(MinecraftReflection.getProfilePublicKeyDataClass());

			if (CONSTRUCTOR == null) {
				CONSTRUCTOR = Accessors.getConstructorAccessor(
						this.getHandleType(),
						Instant.class, PublicKey.class, byte[].class);
			}

			this.setHandle(CONSTRUCTOR.invoke(expireTime, key, signature));
			this.modifier = new StructureModifier<>(MinecraftReflection.getProfilePublicKeyDataClass()).withTarget(this.handle);
		}

		/**
		 * Get the time instant when this key data expires.
		 *
		 * @return the time instant when this key data expires.
		 */
		public Instant getExpireTime() {
			return this.modifier.<Instant>withType(Instant.class).read(0);
		}

		/**
		 * Sets the time when this key data expires.
		 *
		 * @param expireTime the new time when this key data expires.
		 */
		public void setExpireTime(Instant expireTime) {
			this.modifier.<Instant>withType(Instant.class).write(0, expireTime);
		}

		/**
		 * Checks if this key data is expired.
		 *
		 * @return true if this key data is expired, false otherwise.
		 */
		public boolean isExpired() {
			return this.getExpireTime().isBefore(Instant.now());
		}

		/**
		 * Get the signed payload of this key data. The signature of this data must verify against the payload data of
		 * the key. If it does not, the key data is considered unsigned.
		 * <p>
		 * Note that this method takes the expiry time of the key into accountability, if this key is expired it will no
		 * longer match the key data signature.
		 *
		 * @return the signed payload version of this profile key data.
		 */
		public String getSignedPayload() {
			String rsaString = "-----BEGIN RSA PUBLIC KEY-----\n"
					+ MIME_ENCODER.encodeToString(this.getKey().getEncoded())
					+ "\n-----END RSA PUBLIC KEY-----\n";
			return this.getExpireTime().toEpochMilli() + rsaString;
		}

		/**
		 * Get the public key of this key data.
		 *
		 * @return the public key of this key data.
		 */
		public PublicKey getKey() {
			return this.modifier.<PublicKey>withType(PublicKey.class).read(0);
		}

		/**
		 * Sets the public key of this key data.
		 *
		 * @param key the new public key of this key data.
		 */
		public void setKey(PublicKey key) {
			this.modifier.<PublicKey>withType(PublicKey.class).write(0, key);
		}

		/**
		 * Get the signature of this key data.
		 *
		 * @return the key data of this signature.
		 * @see #getSignedPayload()
		 */
		public byte[] getSignature() {
			return this.modifier.<byte[]>withType(byte[].class).read(0);
		}

		/**
		 * Sets the signature of this key data.
		 *
		 * @param signature the new signature of this key data.
		 * @see #getSignedPayload()
		 */
		public void setSignature(byte[] signature) {
			this.modifier.<byte[]>withType(byte[].class).write(0, signature);
		}
	}
}
