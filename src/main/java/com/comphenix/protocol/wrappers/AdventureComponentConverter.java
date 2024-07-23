/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Utility class for converting between the Adventure API Component and ProtocolLib's wrapper
 * <p>
 * Note: The Adventure API Component is not included in CraftBukkit, Bukkit or Spigot and but is present in PaperMC.
 */
public class AdventureComponentConverter {
    private static final GsonComponentSerializer SERIALIZER;

    static {
        if (MinecraftVersion.NETHER_UPDATE.atOrAbove()) {
            SERIALIZER = GsonComponentSerializer.gson();
        } else {
            SERIALIZER = GsonComponentSerializer.colorDownsamplingGson();
        }
    }

    private AdventureComponentConverter() {
    }

    /**
     * Converts a {@link WrappedChatComponent} into a {@link Component}
     * @param wrapper ProtocolLib wrapper
     * @return Component
     */
    public static Component fromWrapper(WrappedChatComponent wrapper) {
        return SERIALIZER.deserialize(wrapper.getJson());
    }

    /**
     * Converts a {@link WrappedChatComponent} into a {@link Component}
     * @param wrapper ProtocolLib wrapper
     * @return Component
     */
    public static Object fromWrapperAsObject(WrappedChatComponent wrapper) {
            return fromWrapper(wrapper);
    }

    /**
     * Converts a JSON {@link String} into a Component
     * @param json Json String
     * @return Component
     */
    public static Component fromJson(final String json) {
            return GsonComponentSerializer.gson().deserialize(json);
    }

    /**
     * Converts a JSON {@link String} into a Component object
     * @param json Json String
     * @return Component object
     */
    public static Object fromJsonAsObject(final String json) {
            return fromJson(json);
    }

    /**
     * Converts a {@link Component} into a ProtocolLib wrapper
     * @param component Component
     * @return ProtocolLib wrapper
     */
    public static WrappedChatComponent fromComponent(Component component) {
        return WrappedChatComponent.fromJson(SERIALIZER.serialize(component));
    }

    /**
     * Converts an object that is instance of {@link Component} into a ProtocolLib wrapper
     * @param component Component
     * @return ProtocolLib wrapper
     */
    public static WrappedChatComponent fromComponentObject(Object component) {
        return fromComponent((Component) component);
    }

    /**
     * Converts a {@link WrappedComponentStyle} into a {@link Style}
     * @param wrapper ProtocolLib wrapper
     * @return Style
     */
    public static Style fromWrapper(WrappedComponentStyle wrapper) {
        return SERIALIZER.serializer().fromJson(wrapper.getJson(), Style.class);
    }

    /**
     * Converts a {@link Style} into a ProtocolLib wrapper
     * @param style Style
     * @return ProtocolLib wrapper
     */
    public static WrappedComponentStyle fromStyle(Style style) {
        return WrappedComponentStyle.fromJson((JsonObject) SERIALIZER.serializer().toJsonTree(style));
    }

    public static Class<?> getComponentClass() {
        return Component.class;
    }

    public static Component clone(Object component) {
        return (Component) component;
    }
}
