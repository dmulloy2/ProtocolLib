package com.comphenix.protocol.wrappers;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Optional;

import com.comphenix.protocol.wrappers.codecs.WrappedCodec;
import com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.bukkit.ChatColor;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftRegistryAccess;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

/**
 * Represents a chat component added in Minecraft 1.7.2
 * @author Kristian
 */
public class WrappedChatComponent extends AbstractWrapper implements ClonableWrapper {
    private static final Class<?> SERIALIZER = MinecraftReflection.getChatSerializerClass();
    private static final Class<?> COMPONENT = MinecraftReflection.getIChatBaseComponentClass();
    private static final Class<?> GSON_CLASS = MinecraftReflection.getMinecraftGsonClass();
	private static final Optional<Class<?>> MUTABLE_COMPONENT_CLASS
		= MinecraftReflection.getOptionalNMS("network.chat.IChatMutableComponent");

    private static Object GSON = null;
    private static MethodAccessor DESERIALIZE = null;

    private static MethodAccessor SERIALIZE_COMPONENT = null;
    private static MethodAccessor CONSTRUCT_COMPONENT = null;

    private static ConstructorAccessor CONSTRUCT_TEXT_COMPONENT = null;

    private static WrappedCodec CODEC;

    static {
        FuzzyReflection fuzzy = FuzzyReflection.fromClass(SERIALIZER, true);

        if (MinecraftVersion.v1_21_6.atOrAbove()) {
            Field codecField = fuzzy.getFieldByType("CODEC", MinecraftReflection.getCodecClass());
            CODEC = WrappedCodec.fromHandle(Accessors.getFieldAccessor(codecField).get(null));
        } else if (MinecraftVersion.v1_20_5.atOrAbove()) {
            SERIALIZE_COMPONENT = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
        			.returnTypeExact(String.class)
        			.parameterDerivedOf(COMPONENT)
        			.parameterDerivedOf(MinecraftReflection.getHolderLookupProviderClass())
        			.build()));
        } else {
            SERIALIZE_COMPONENT = Accessors.getMethodAccessor(fuzzy.getMethodByReturnTypeAndParameters("serialize", /* a */
                    String.class, new Class<?>[] { COMPONENT }));
        }

        if (!MinecraftVersion.v1_20_4.atOrAbove()) {
            GSON = Accessors.getFieldAccessor(fuzzy.getFieldByType("gson", GSON_CLASS)).get(null);
        }

        if (!MinecraftVersion.v1_21_6.atOrAbove()) {
            if (MinecraftVersion.v1_20_5.atOrAbove()) {
                DESERIALIZE = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
                        .returnDerivedOf(COMPONENT)
                        .parameterExactType(String.class)
                        .parameterDerivedOf(MinecraftReflection.getHolderLookupProviderClass())
                        .build()));
            } else if (MinecraftVersion.v1_20_4.atOrAbove()) {
                DESERIALIZE = Accessors.getMethodAccessor(fuzzy
                        .getMethodByReturnTypeAndParameters("fromJson", MUTABLE_COMPONENT_CLASS.get(), new Class[]{String.class}));
            } else {
                try {
                    DESERIALIZE = Accessors.getMethodAccessor(FuzzyReflection.fromClass(MinecraftReflection.getChatDeserializer(), true)
                            .getMethodByReturnTypeAndParameters("deserialize", Object.class, new Class<?>[]{GSON_CLASS, String.class, Class.class, boolean.class}));
                } catch (IllegalArgumentException ex) {
                    // We'll handle it in the ComponentParser
                    DESERIALIZE = null;
                }
            }
        }

        // Get a component from a standard Minecraft message
        CONSTRUCT_COMPONENT = Accessors.getMethodAccessor(MinecraftReflection.getCraftChatMessage(), "fromString", String.class, boolean.class);

        try {
            // And the component text constructor
            CONSTRUCT_TEXT_COMPONENT = Accessors.getConstructorAccessor(MinecraftReflection.getChatComponentTextClass(), String.class);
        } catch (Exception ignored) {
            // We don't need it
        }
    }

    private static Object serialize(Object handle) {
        if (CODEC != null) {
            Object jobj = CODEC.encode(handle, WrappedDynamicOps.json(false)).getOrThrow(JsonParseException::new);
            return jobj.toString();
        }

    	if (MinecraftVersion.v1_20_5.atOrAbove()) {
    		return SERIALIZE_COMPONENT.invoke(null, handle, MinecraftRegistryAccess.get());
    	}

		return SERIALIZE_COMPONENT.invoke(null, handle);
    }

    private static Object deserialize(String json) {
        if (CODEC != null) {
            return CODEC.parse(JsonParser.parseString(json), WrappedDynamicOps.json(false)).getOrThrow(JsonParseException::new);
        }

    	if (MinecraftVersion.v1_20_5.atOrAbove()) {
    		return DESERIALIZE.invoke(null, json, MinecraftRegistryAccess.get());
    	}

		if (MinecraftVersion.v1_20_4.atOrAbove()) {
			return DESERIALIZE.invoke(null, json);
		}

        // Should be non-null on 1.9 and up
        if (DESERIALIZE != null) {
            return DESERIALIZE.invoke(null, GSON, json, COMPONENT, true);
        }

        // Mock leniency behavior in 1.8
        StringReader str = new StringReader(json);
        return ComponentParser.deserialize(GSON, COMPONENT, str);
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
        return new WrappedChatComponent(deserialize(json), json);
    }

    /**
     * Construct a wrapper around a new text chat component with the given text.
     * <p>
     * Note: {@link #fromLegacyText(String)} is preferred for text that contains
     * legacy formatting codes since it will translate them to the JSON equivalent.
     * @param text - the text of the text chat component.
     * @return The wrapper around the new chat component.
     */
    public static WrappedChatComponent fromText(String text) {
        Preconditions.checkNotNull(text, "text cannot be NULL.");

        if (CONSTRUCT_TEXT_COMPONENT != null) {
            return fromHandle(CONSTRUCT_TEXT_COMPONENT.invoke(text));
        }

        // this is a bit hacky, but it works good enough and has no need for additional magic
        JsonObject object = new JsonObject();
        object.addProperty("text", text);
        return fromJson(object.toString());
    }

    /**
     * Construct an array of chat components from a standard Minecraft message.
     * <p>
     * This uses {@link ChatColor} for formating.
     * @param message - the message.
     * @return The equivalent chat components.
     */
    public static WrappedChatComponent[] fromChatMessage(String message) {
        Object[] components = (Object[]) CONSTRUCT_COMPONENT.invoke(null, message, false);
        WrappedChatComponent[] result = new WrappedChatComponent[components.length];

        for (int i = 0; i < components.length; i++) {
            result[i] = fromHandle(components[i]);
        }
        return result;
    }

    /**
     * Construct a single chat component from a standard Minecraft message
     * (with legacy formatting codes), preserving multiple lines.
     * @param message - the message.
     * @return The equivalent chat component.
     */
    public static WrappedChatComponent fromLegacyText(String message) {
        // With keepNewlines = true (second parameter), only one component is returned
        Object[] components = (Object[]) CONSTRUCT_COMPONENT.invoke(null, message, true);
        return fromHandle(components[0]);
    }

    /**
     * Retrieve a copy of this component as a JSON string.
     * <p>
     * Note that any modifications to this JSON string will not update the current component.
     * @return The JSON representation of this object.
     */
    public String getJson() {
        if (cache == null) {
            cache = (String) serialize(handle);
        }
        return cache;
    }

    /**
     * Set the content of this component using a JSON object.
     * @param obj - the JSON that represents the new component.
     */
    public void setJson(String obj) {
        this.handle = deserialize(obj);
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
    public String toString() {
        return "WrappedChatComponent[json=" + getJson() + "]";
    }
}
