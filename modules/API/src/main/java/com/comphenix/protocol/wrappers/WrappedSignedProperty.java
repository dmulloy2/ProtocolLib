package com.comphenix.protocol.wrappers;

import java.security.PublicKey;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.google.common.base.Objects;

/**
 * Represents a wrapper over a signed property.
 * @author Kristian
 */
public class WrappedSignedProperty extends AbstractWrapper {
	private static final String CLASS_NAME = "com.mojang.authlib.properties.Property";
	private static final String UTIL_PACKAGE = "net.minecraft.util.";

	private static Class<?> PROPERTY;
	private static ConstructorAccessor CONSTRUCTOR;
	private static MethodAccessor GET_NAME;
	private static MethodAccessor GET_SIGNATURE;
	private static MethodAccessor GET_VALUE;
	private static MethodAccessor HAS_SIGNATURE;
	private static MethodAccessor IS_SIGNATURE_VALID;

	static {
		try {
			PROPERTY = Class.forName(CLASS_NAME);
		} catch (ClassNotFoundException ex) {
			try {
				PROPERTY = Class.forName(UTIL_PACKAGE + CLASS_NAME);
			} catch (ClassNotFoundException ex1) {
				throw new RuntimeException("Failed to obtain Property class.", ex1);
			}
		}

		try {
			CONSTRUCTOR = Accessors.getConstructorAccessor(PROPERTY, String.class, String.class, String.class);
			GET_NAME = Accessors.getMethodAccessor(PROPERTY, "getName");
			GET_SIGNATURE = Accessors.getMethodAccessor(PROPERTY, "getSignature");
			GET_VALUE = Accessors.getMethodAccessor(PROPERTY, "getValue");
			HAS_SIGNATURE = Accessors.getMethodAccessor(PROPERTY, "hasSignature");
			IS_SIGNATURE_VALID = Accessors.getMethodAccessor(PROPERTY, "isSignatureValid", PublicKey.class);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to obtain methods for Property.", ex);
		}
	}
	
	/**
	 * Construct a new wrapped signed property from the given values.
	 * @param name - the name of the property.
	 * @param value - the value of the property.
	 * @param signature - the BASE64-encoded signature of the value.
	 */
	public WrappedSignedProperty(String name, String value, String signature) {
		this(CONSTRUCTOR.invoke(name, value, signature));
	}
	
	/**
	 * Construct a new wrapped signed property from a given handle.
	 * @param handle - the handle.
	 */
	private WrappedSignedProperty(Object handle) {
		super(PROPERTY);
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
	 * Construct a new wrapped signed property from the given values.
	 * @param name - the name of the property.
	 * @param value - the value of the property.
	 * @param signature - the BASE64-encoded signature of the value.
	 * @return The signed property.
	 */
	public static WrappedSignedProperty fromValues(String name, String value, String signature) {
		return new WrappedSignedProperty(name, value, signature);
	}

	/**
	 * Retrieve the name of the underlying property, such as "textures".
	 * @return Name of the property.
	 */
	public String getName() {
		return (String) GET_NAME.invoke(handle);
	}

	/**
	 * Retrieve the signature of the property (base64) as returned by the session server's /hasJoined.
	 * @return The signature of the property.
	 */
	public String getSignature() {
		return (String) GET_SIGNATURE.invoke(handle);
	}

	/**
	 * Retrieve the value of the property (base64) as return by the session server's /hasJoined
	 * @return  The value of the property.
	 */
	public String getValue() {
		return (String) GET_VALUE.invoke(handle);
	}

	/**
	 * Determine if this property has a signature.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasSignature() {
		return (Boolean) HAS_SIGNATURE.invoke(handle);
	}
	
	/**
	 * Determine if the signature of this property is valid and signed by the corresponding private key.
	 * @param key - the public key.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isSignatureValid(PublicKey key) {
		return (Boolean) IS_SIGNATURE_VALID.invoke(handle, key);
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
