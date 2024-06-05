/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 * Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a wrapper around registrable objects.
 *
 * @author portlek
 */
public final class WrappedRegistrable extends AbstractWrapper implements ClonableWrapper {

    @NotNull
    private final Factory factory;

    private WrappedRegistrable(
        @NotNull final Factory factory,
        @NotNull final Object handle
    ) {
        super(factory.registrableClass);
        this.factory = factory;
        setHandle(handle);
    }

    @NotNull
    public static WrappedRegistrable fromHandle(
        @NotNull final Factory factory,
        @NotNull final Object handle
    ) {
        return new WrappedRegistrable(factory, handle);
    }

    @NotNull
    public static WrappedRegistrable fromHandle(
        @NotNull final Class<?> registrableClass,
        @NotNull final Object handle
    ) {
        return fromHandle(Factory.getOrCreate(registrableClass), handle);
    }

    @NotNull
    public static WrappedRegistrable fromClassAndKey(
        @NotNull final Class<?> registrableClass,
        @NotNull final MinecraftKey key
    ) {
        final Factory factory = Factory.getOrCreate(registrableClass);
        return fromHandle(factory, factory.getHandle(key));
    }

    @NotNull
    public static WrappedRegistrable fromClassAndKey(
        @NotNull final Class<?> registrableClass,
        @NotNull final String key
    ) {
        return fromClassAndKey(registrableClass, new MinecraftKey(key));
    }

    @NotNull
    public static WrappedRegistrable blockEntityType(
        @NotNull final MinecraftKey key
    ) {
        return fromClassAndKey(MinecraftReflection.getBlockEntityTypeClass(), key);
    }

    @NotNull
    public static WrappedRegistrable blockEntityType(
        @NotNull final String key
    ) {
        return blockEntityType(new MinecraftKey(key));
    }

    /**
     * Gets this registrable object's Minecraft key
     *
     * @return The Minecraft key
     */
    public MinecraftKey getKey() {
        return factory.getKey(handle);
    }

    /**
     * Sets this registrable object's Minecraft key
     *
     * @param key Minecraft key
     */
    public void setKey(MinecraftKey key) {
        setHandle(factory.getHandle(key));
    }

    public WrappedRegistrable deepClone() {
        return fromHandle(factory, handle);
    }

    @Override
    public String toString() {
        return "WrappedRegistrable[handle=" + handle + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getKey().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WrappedRegistrable)) {
            return false;
        }
        final WrappedRegistrable that = (WrappedRegistrable) o;
        return handle.equals(that.handle) || (getKey() == that.getKey());
    }

    private static final class Factory {

        private static final Map<Class<?>, Factory> CACHE = new ConcurrentHashMap<>();

        @NotNull
        private final Class<?> registrableClass;

        @NotNull
        private final WrappedRegistry registry;

        private Factory(@NotNull final Class<?> registrableClass) {
            this.registrableClass = registrableClass;
            this.registry = WrappedRegistry.getRegistry(registrableClass);
        }

        @NotNull
        public static Factory getOrCreate(
            @NotNull final Class<?> registrableClass
        ) {
            return CACHE.computeIfAbsent(registrableClass, Factory::new);
        }

        @NotNull
        public MinecraftKey getKey(@NotNull final Object handle) {
            return registry.getKey(handle);
        }

        @NotNull
        public Object getHandle(MinecraftKey key) {
            return registry.get(key);
        }
    }
}
