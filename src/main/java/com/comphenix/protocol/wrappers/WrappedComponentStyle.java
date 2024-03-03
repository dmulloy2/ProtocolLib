package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.codecs.WrappedCodec;
import com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * A wrapper around the component style NMS class.
 *
 * @author vytskalt
 */
public class WrappedComponentStyle extends AbstractWrapper {
    private static final WrappedCodec CODEC; // 1.20.4+
    private static final Gson GSON; // Below 1.20.4

    static {
        if (MinecraftVersion.v1_20_4.atOrAbove()) {
            FuzzyReflection fuzzySerializer = FuzzyReflection.fromClass(MinecraftReflection.getStyleSerializerClass(), true);
            Object codec = Accessors.getFieldAccessor(fuzzySerializer.getFieldByType("CODEC", MinecraftReflection.getCodecClass())).get(null);
            CODEC = WrappedCodec.fromHandle(codec);
            GSON = null;
        } else {
            FuzzyReflection fuzzySerializer = FuzzyReflection.fromClass(MinecraftReflection.getChatSerializerClass(), true);
            CODEC = null;
            GSON = (Gson) Accessors.getFieldAccessor(fuzzySerializer.getFieldByType("gson", Gson.class)).get(null);
        }
    }

    public WrappedComponentStyle(Object handle) {
        super(MinecraftReflection.getComponentStyleClass());
        setHandle(handle);
    }

    public JsonElement getJson() {
        if (CODEC != null) {
            return (JsonElement) CODEC.encode(handle, WrappedDynamicOps.json(false))
                    .getOrThrow(JsonParseException::new);
        } else {
            return GSON.toJsonTree(handle);
        }
    }

    public static WrappedComponentStyle fromJson(JsonElement json) {
        Object handle;
        if (CODEC != null) {
            handle = CODEC.parse(json, WrappedDynamicOps.json(false))
                    .getOrThrow(JsonParseException::new);
        } else {
            handle = GSON.fromJson(json, MinecraftReflection.getComponentStyleClass());
        }
        return new WrappedComponentStyle(handle);
    }
}
