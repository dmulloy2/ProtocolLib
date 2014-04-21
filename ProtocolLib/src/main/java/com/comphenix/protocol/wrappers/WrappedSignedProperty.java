package com.comphenix.protocol.wrappers;

import java.security.PublicKey;

import net.minecraft.util.com.mojang.authlib.properties.Property;
import com.google.common.base.Objects;

/**
 * Represents a wrapper over a signed property.
 * @author Kristian
 */
public class WrappedSignedProperty extends AbstractWrapper {
	/**
	 * Construct a new wrapped signed property from a given handle.
	 * @param handle - the handle.
	 */
	private WrappedSignedProperty(Object handle) {
		super(Property.class);
		setHandle(handle);
	}
	
	/**
	 * Construct a new signed property from a given NMS property.
	 * @param handle - the property.
	 * @return The wrapped signed property.
	 */
	public static WrappedSignedProperty fromHandle(Object handle) {
		return new WrappedSignedProperty(handle);
	}
	
	/**
	 * Retrieve the underlying signed property.
	 * @return The GameProfile.
	 */
	private Property getProfile() {
		return (Property) handle;
	}

	/**
	 * Retrieve the name of the underlying property, such as "textures".
	 * @return Name of the property.
	 */
	public String getName() {
		return getProfile().getName();
	}

	/**
	 * Retrieve the signature of the property (base64) as returned by the session server's /hasJoined. 
	 * @return The signature of the property.
	 */
	public String getSignature() {
		return getProfile().getSignature();
	}

	/**
	 * Retrieve the value of the property (base64) as return by the session server's /hasJoined 
	 * @return  The value of the property.
	 */
	public String getValue() {
		return getProfile().getValue();
	}

	/**
	 * Determine if this property has a signature.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasSignature() {
		return getProfile().hasSignature();
	}
	
	/**
	 * Determine if the signature of this property is valid and signed by the corresponding private key.
	 * @param key - the public key.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isSignatureValid(PublicKey key) {
		return getProfile().isSignatureValid(key);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getValue(), getSignature());
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof WrappedSignedProperty) {
			if (!super.equals(object)) 
				return false;
			WrappedSignedProperty that = (WrappedSignedProperty) object;
			return Objects.equal(this.getName(), that.getName())
				&& Objects.equal(this.getValue(), that.getValue())
				&& Objects.equal(this.getSignature(), that.getSignature());
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", getName())
			.add("value", getValue())
			.add("signature", getSignature())
			.toString();
	}
}
