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

/**
 * Represents a wrapper around BlockEntityType.
 *
 * @author portlek
 */
public class WrappedBlockEntityType extends AbstractWrapper implements ClonableWrapper {

    private static final WrappedRegistry BLOCK_ENTITY_TYPE_REGISTRY = WrappedRegistry.getRegistry(MinecraftReflection.getBlockEntityTypeClass());

    private static final FieldAccessor TYPE_ACCESSOR = Accessors.getFieldAccessor(
            FuzzyReflection.fromClass(MinecraftReflection.getBlockEntityTypeClass(), true)
                    .getField(
                            FuzzyFieldContract.newBuilder()
                                    .typeExact(MinecraftReflection.getBlockEntityTypeClass())
                                    .build()
                    )
    );

    private static final Class<?> BLOCK_ENTITY_TYPE_CLASS = MinecraftReflection.getBlockEntityTypeClass();

    public WrappedBlockEntityType(Object handle) {
        super(BLOCK_ENTITY_TYPE_CLASS);
        setHandle(handle);
    }

	public static WrappedBlockEntityType fromHandle(Object handle) {
		return new WrappedBlockEntityType(handle);
	}

    /**
     * Creates a new BlockEntityType instance with the given type and no data.
     * @param key BlockEntityType's Minecraft key
     * @return New BlockEntityType
     */
    public static WrappedBlockEntityType create(MinecraftKey key) {
        return fromHandle(BLOCK_ENTITY_TYPE_REGISTRY.get(key));
    }

    /**
     * Gets this BlockEntityType's Minecraft key
     * @return The Minecraft key
     */
    public MinecraftKey getKey() {
        return BLOCK_ENTITY_TYPE_REGISTRY.getKey(TYPE_ACCESSOR.get(handle));
    }

    /**
     * Sets this BlockEntityType's Minecraft key
     * @param key Minecraft key
     */
    public void setKey(MinecraftKey key) {
        setHandle(BLOCK_ENTITY_TYPE_REGISTRY.get(key));
    }

    public WrappedBlockEntityType deepClone() {
        return fromHandle(handle);
    }

    @Override
    public String toString() {
        return "WrappedBlockEntityType[handle=" + handle + "]";
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
        if (!(o instanceof WrappedBlockEntityType)) {
            return false;
        }
        final WrappedBlockEntityType that = (WrappedBlockEntityType) o;
        return this.handle.equals(that.handle) || (this.getKey() == that.getKey());
    }
}
