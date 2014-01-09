package com.comphenix.protocol.wrappers;

import org.bukkit.ChatColor;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Preconditions;

/**
 * Represents a chat component added in Minecraft 1.7.2
 * @author Kristian
 */
public class WrappedChatComponent extends AbstractWrapper {
	private static final Class<?> SERIALIZER = MinecraftReflection.getChatSerializerClass();
	private static final Class<?> COMPONENT = MinecraftReflection.getIChatBaseComponentClass();
	private static MethodAccessor SERIALIZE_COMPONENT = null;
	private static MethodAccessor DESERIALIZE_COMPONENT = null;
	private static MethodAccessor CONSTRUCT_COMPONENT = null;
	private static ConstructorAccessor CONSTRUCT_TEXT_COMPONENT = null;
	
	static {
		FuzzyReflection fuzzy = FuzzyReflection.fromClass(SERIALIZER);
		
		// Retrieve the correct methods
		SERIALIZE_COMPONENT = Accessors.getMethodAccessor(
			fuzzy.getMethodByParameters("serialize", String.class, new Class<?>[] { COMPONENT }));
		DESERIALIZE_COMPONENT = Accessors.getMethodAccessor(
				fuzzy.getMethodByParameters("serialize", COMPONENT, new Class<?>[] { String.class }));
	
		// Get a component from a standard Minecraft message
		CONSTRUCT_COMPONENT = Accessors.getMethodAccessor(
			MinecraftReflection.getCraftChatMessage(), "fromString", String.class);
		
		// And the component text constructor
		CONSTRUCT_TEXT_COMPONENT = Accessors.getConstructorAccessor(
				MinecraftReflection.getChatComponentTextClass(), String.class);
	}
	
	private transient String cache;
	
	private WrappedChatComponent(Object handle, String cache) {
		super(MinecraftReflection.getIChatBaseComponentClass());
		setHandle(handle);
		this.cache = cache;
	}
	
	/**
	 * Construct a new chat component wrapper around the given NMS object.
	 * @param handle - the NMS object. 
	 * @return The wrapper.
	 */
	public static WrappedChatComponent fromHandle(Object handle) {
		return new WrappedChatComponent(handle, null);
	}
	
	/**
	 * Construct a new chat component wrapper from the given JSON string.
	 * @param json - the json.
	 * @return The chat component wrapper.
	 */
	public static WrappedChatComponent fromJson(String json) {
		return new WrappedChatComponent(DESERIALIZE_COMPONENT.invoke(null, json), json);
	}
	
	/**
	 * Construct a wrapper around a new text chat component with the given text.
	 * @param text - the text of the text chat component.
	 * @return The wrapper around the new chat component.
	 */
	public static WrappedChatComponent fromText(String text) {
		Preconditions.checkNotNull(text, "text cannot be NULL.");
		return fromHandle(CONSTRUCT_TEXT_COMPONENT.invoke(text));
	}
	
	/**
	 * Construct an array of chat components from a standard Minecraft message.
	 * <p>
	 * This uses {@link ChatColor} for formating.
	 * @param message - the message.
	 * @return The equivalent chat components.
	 */
	public static WrappedChatComponent[] fromChatMessage(String message) {
		Object[] components = (Object[]) CONSTRUCT_COMPONENT.invoke(null, message);
		WrappedChatComponent[] result = new WrappedChatComponent[components.length];
		
		for (int i = 0; i < components.length; i++) {
			result[i] = fromHandle(components[i]);
		}
		return result;
	}
	
	/**
	 * Retrieve a copy of this component as a JSON string.
	 * <p>
	 * Note that any modifications to this JSON string will not update the current component.
	 * @return The JSON representation of this object.
	 */
	public String getJson() {
		if (cache == null) {
			cache = (String) SERIALIZE_COMPONENT.invoke(null, handle);
		}
		return cache;
 	}
	
	/**
	 * Set the content of this component using a JSON object.
	 * @param obj - the JSON that represents the new component.
	 */
	public void setJson(String obj) {
		this.handle = DESERIALIZE_COMPONENT.invoke(null, obj);
		this.cache = obj;
	}

	/**
	 * Retrieve a deep copy of the current chat component.
	 * @return A copy of the current component.
	 */
	public WrappedChatComponent deepClone() {
		return fromJson(getJson());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof WrappedChatComponent) {
			return ((WrappedChatComponent) obj).handle.equals(handle);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return handle.hashCode();
	}
}
