package com.comphenix.protocol.wrappers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.io.StringReader;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a chat component added in Minecraft 1.7.2
 * 
 * @author Kristian
 */
public class WrappedChatComponent extends AbstractWrapper implements ClonableWrapper {
	
	private static final Class<?> COMPONENT = MinecraftReflection.getIChatBaseComponentClass();
	
	private static Object GSON = null;
	
	private static MethodAccessor DESERIALIZE = null;
	private static MethodAccessor SERIALIZE_COMPONENT = null;
	private static MethodAccessor CONSTRUCT_COMPONENT = null;
	
	static {
		FuzzyReflection fuzzy = FuzzyReflection
				.fromClass(MinecraftReflection.getChatSerializerClass(), true);
		
		// Retrieve the correct methods
		SERIALIZE_COMPONENT = Accessors
				.getMethodAccessor(fuzzy.getMethodByReturnTypeAndParameters("serialize", /* a */
						String.class, new Class<?>[] { COMPONENT }));
		
		GSON = Accessors
				.getFieldAccessor(
						fuzzy.getFieldByType("gson", MinecraftReflection.getMinecraftGsonClass()))
				.get(null);
		
		try {
			DESERIALIZE = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getChatDeserializer(), true)
							.getMethodByReturnTypeAndParameters("deserialize", Object.class,
									new Class<?>[] { GSON.getClass(), String.class, Class.class,
											boolean.class }));
		}
		catch (IllegalArgumentException ex) {
			// We'll handle it in the ComponentParser
			DESERIALIZE = null;
		}
		
		// Get a component from a standard Minecraft message
		CONSTRUCT_COMPONENT = Accessors.getMethodAccessor(MinecraftReflection.getCraftChatMessage(),
				"fromString", String.class, boolean.class);
	}
	
	private static Object deserialize(String json) {
		// Should be non-null on 1.9 and up
		if (DESERIALIZE != null) {
			return DESERIALIZE.invoke(null, GSON, json, COMPONENT, true);
		}
		// Mock leniency behavior in 1.8
		return ComponentParser.deserialize(GSON, COMPONENT, new StringReader(json));
	}
	
	/**
	 * The JSON String representation of this chat component. This is used to be able to transform
	 * this chat component into a {@link BaseComponent}.
	 */
	private transient String json;
	
	private WrappedChatComponent(Object handle, String json) {
		super(MinecraftReflection.getIChatBaseComponentClass());
		setHandle(handle);
		this.json = json;
	}
	
	/**
	 * Construct a new chat component wrapper around the given NMS object.
	 * 
	 * @param handle - the NMS object.
	 * 
	 * @return The wrapper.
	 */
	public static WrappedChatComponent fromHandle(Object handle) {
		return new WrappedChatComponent(handle, (String) SERIALIZE_COMPONENT.invoke(null, handle));
	}
	
	/**
	 * Construct a new chat component wrapper from the given JSON string.
	 * 
	 * @param json - the json.
	 * 
	 * @return The chat component wrapper.
	 */
	public static WrappedChatComponent fromJson(String json) {
		return new WrappedChatComponent(deserialize(json), json);
	}
	
	/**
	 * Construct a wrapper around a new text chat component with the given text.
	 * <p>
	 * Note: {@link #fromLegacyText(String)} is preferred for text that contains legacy formatting
	 * codes since it will translate them to the JSON equivalent.
	 * 
	 * @param text - the text of the text chat component.
	 * 
	 * @return The wrapper around the new chat component.
	 * 
	 * @throws NullPointerException If the text is {@code null}.
	 */
	public static WrappedChatComponent fromText(String text) {
		if (text == null) {
			throw new NullPointerException("text cannot be NULL.");
		}
		return fromSpigotComponent(new TextComponent(text));
	}
	
	/**
	 * Construct an array of chat components from a standard Minecraft message.
	 * <p>
	 * This uses {@link ChatColor} for formating.
	 * 
	 * @param message - the message.
	 * 
	 * @return The equivalent chat components.
	 * 
	 * @throws NullPointerException If the message is {@code null}.
	 */
	public static WrappedChatComponent[] fromChatMessage(String message) {
		if (message == null) {
			throw new NullPointerException("message cannot be NULL.");
		}
		return new WrappedChatComponent[] {
				fromSpigotComponents(TextComponent.fromLegacyText(message)) };
	}
	
	/**
	 * Construct a single chat component from a standard Minecraft message (with legacy formatting
	 * codes), preserving multiple lines.
	 * 
	 * @param message - the message.
	 * 
	 * @return The equivalent chat component.
	 */
	public static WrappedChatComponent fromLegacyText(String message) {
		// With keepNewlines = true (second parameter), only one component is returned
		Object[] components = (Object[]) CONSTRUCT_COMPONENT.invoke(null, message, true);
		return fromHandle(components[0]);
	}
	
	/**
	 * Construct a single chat component from a spigot {@link BaseComponent}.
	 * 
	 * @param component - The component to be wrapped.
	 * 
	 * @return The equivalent chat component.
	 */
	public static WrappedChatComponent fromSpigotComponent(BaseComponent component) {
		return fromJson(ComponentSerializer.toString(component));
	}
	
	/**
	 * Construct a single chat component from a spigot {@link BaseComponent} array.
	 * 
	 * @param component - The component to be wrapped.
	 * 
	 * @return The equivalent chat component.
	 */
	public static WrappedChatComponent fromSpigotComponents(BaseComponent... components) {
		return fromJson(ComponentSerializer.toString(components));
	}
	
	/**
	 * Retrieve a copy of this component as a JSON string.
	 * 
	 * @return The JSON string representation of this object.
	 */
	public String getJson() {
		return json;
	}
	
	/**
	 * Retrive a copy of this component as a Spigot {@link BaseComponent} array.
	 * <p>
	 * Note that any modifications to the Spigot components or the array will not update the this
	 * component, for this use {@link #setJson(BaseComponent...)}.
	 * 
	 * @return The {@link BaseComponent} representation of this object.
	 */
	public BaseComponent[] getAsSpigotComponents() {
		return ComponentSerializer.parse(getJson());
	}
	
	/**
	 * Set the content of this component using a JSON object.
	 * 
	 * @param obj - the JSON String object that represents the new component.
	 */
	public void setJson(String obj) {
		this.handle = deserialize(obj);
		this.json = obj;
	}
	
	/**
	 * Set the content of this component using a Spigot {@link BaseComponent}.
	 * 
	 * @param component - The component to be wrapped.
	 */
	public void setJson(BaseComponent component) {
		setJson(ComponentSerializer.toString(component));
	}
	
	/**
	 * Set the content of this component using a Spigot {@link BaseComponent} array.
	 * 
	 * @param components - The components to be wrapped as single {@link TextComponent}.
	 */
	public void setJson(BaseComponent... components) {
		setJson(ComponentSerializer.toString(components));
	}
	
	/**
	 * Retrieve a deep copy of the current chat component.
	 * 
	 * @return A copy of the current component.
	 */
	public WrappedChatComponent deepClone() {
		return fromJson(json);
	}
	
	@Override
	public String toString() {
		return "WrappedChatComponent[json=" + json + "]";
	}
}
