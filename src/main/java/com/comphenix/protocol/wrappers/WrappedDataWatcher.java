/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2018 dmulloy2
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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityPose;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a DataWatcher
 * @author dmulloy2
 */
public class WrappedDataWatcher implements IDataWatcher {
    private static final boolean IN_MEMORY = MinecraftVersion.v1_20_4.atOrAbove();

    @NotNull
    private final IDataWatcher impl;

    // ---- Construction

    private WrappedDataWatcher(IDataWatcher impl) {
        this.impl = impl;
    }

    /**
     * Constructs a new DataWatcher wrapper around a NMS handle. The resulting
     * DataWatcher will likely have existing values that can be removed with
     * {@link #clear()}.
     * 
     * @param handle DataWatcher handle
     */
    public WrappedDataWatcher(Object handle) {
        this.impl = IN_MEMORY
            ? new InMemoryDataWatcher(handle)
            : new LegacyDataWatcher(handle);
    }

    /**
     * Constructs a new DataWatcher using a fake egg entity. The
     * resulting DataWatcher will not have any keys or values and new ones will
     * have to be added using watcher objects.
     */
    public WrappedDataWatcher() {
        this.impl = IN_MEMORY
            ? new InMemoryDataWatcher()
            : new LegacyDataWatcher();
    }

    /**
     * Constructs a new DataWatcher using a real entity. The resulting
     * DataWatcher will not have any keys or values and new ones will have to
     * be added using watcher objects.
     * 
     * @param entity The entity
     */
    public WrappedDataWatcher(Entity entity) {
        this.impl = IN_MEMORY
            ? new InMemoryDataWatcher(entity)
            : new LegacyDataWatcher(entity);
    }

    /**
     * Constructs a new DataWatcher using a fake egg entity and a given
     * list of watchable objects.
     * 
     * @param objects The list of objects
     */
    public WrappedDataWatcher(List<WrappedWatchableObject> objects) {
        this.impl = IN_MEMORY
            ? new InMemoryDataWatcher(objects)
            : new LegacyDataWatcher(objects);
    }

    /**
     * Gets the contents of this DataWatcher as a map.
     * @return The contents
     */
    public Map<Integer, WrappedWatchableObject> asMap() {
        return impl.asMap();
    }

    /**
     * Gets a set containing the registered indexes.
     * @return The set
     */
    @Deprecated
    public Set<Integer> getIndexes() {
        return impl.getIndexes();
    }

    /**
     * Gets a list of the contents of this DataWatcher.
     * @return The contents
     */
    @SuppressWarnings("unchecked")
    public List<WrappedWatchableObject> getWatchableObjects() {
        return impl.getWatchableObjects();
    }

    @Override
    public Iterator<WrappedWatchableObject> iterator() {
        return impl.iterator();
    }

    /**
     * Gets the size of this DataWatcher's contents.
     * @return The size
     */
    public int size() {
        return impl.size();
    }

    /**
     * Gets the item at a given index.
     * 
     * @param index Index to get
     * @return The watchable object, or null if none exists
     */
    public WrappedWatchableObject getWatchableObject(int index) {
        return impl.getWatchableObject(index);
    }

    /**
     * @deprecated Renamed to {@link #remove(int)}
     */
    @Deprecated
    public WrappedWatchableObject removeObject(int index) {
        return remove(index);
    }

    /**
     * Removes the item at a given index.
     * 
     * @param index Index to remove
     * @return The previous value, or null if none existed
     */
    public WrappedWatchableObject remove(int index) {
        return impl.remove(index);
    }

    /**
     * Whether or not this DataWatcher has an object at a given index.
     * 
     * @param index Index to check for
     * @return True if it does, false if not
     */
    public boolean hasIndex(int index) {
        return impl.hasIndex(index);
    }

    /**
     * Returns a set containing all the registered indexes
     * @return The set
     */
    @Deprecated
    public Set<Integer> indexSet() {
        return getIndexes();
    }

    /**
     * Clears the contents of this DataWatcher. The watcher will be empty after
     * this operation is called.
     */
    public void clear() {
        impl.clear();
    }

    // ---- 0: byte

    /**
     * Get a watched byte.
     * 
     * @param index - index of the watched byte.
     * @return The watched byte, or NULL if this value doesn't exist.
     */
    public Byte getByte(int index) {
        return (Byte) getObject(index);
    }

    public void setByte(int index, byte value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Byte.class)), value, update);
    }

    // ---- short (unused)

    /**
     * Get a watched short.
     * 
     * @param index - index of the watched short.
     * @return The watched short, or NULL if this value doesn't exist.
     */
    public Short getShort(int index) {
        return (Short) getObject(index);
    }

    // ---- 1: varint (integer)

    /**
     * Get a watched integer.
     * 
     * @param index - index of the watched integer.
     * @return The watched integer, or NULL if this value doesn't exist.
     */
    public Integer getInteger(int index) {
        return (Integer) getObject(index);
    }

    public void setInteger(int index, Integer value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Integer.class)), value, update);
    }

    // ---- 2: varlong (long)

    public Long getLong(int index) {
        return (Long) getObject(index);
    }

    public void setLong(int index, Long value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Long.class)), value, update);
    }

    // ---- 3: float

    /**
     * Get a watched float.
     * 
     * @param index - index of the watched float.
     * @return The watched float, or NULL if this value doesn't exist.
     */
    public Float getFloat(int index) {
        return (Float) getObject(index);
    }

    public void setFloat(int index, Float value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Float.class)), value, update);
    }

    // ---- 4: string

    /**
     * Get a watched string.
     * 
     * @param index - index of the watched string.
     * @return The watched string, or NULL if this value doesn't exist.
     */
    public String getString(int index) {
        return (String) getObject(index);
    }

    public void setString(int index, String value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(String.class)), value, update);
    }

    // ---- 5: text component

    public WrappedChatComponent getChatComponent(int index) {
        return (WrappedChatComponent) getObject(index);
    }

    public void setChatComponent(int index, WrappedChatComponent value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getChatComponentSerializer()), value, update);
    }

    // ---- 6: optional text component

    public Optional<WrappedChatComponent> getOptionalChatComponent(int index) {
        return (Optional<WrappedChatComponent>) getObject(index);
    }

    public void setOptionalChatComponent(int index, Optional<WrappedChatComponent> value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getChatComponentSerializer(true)), value, update);
    }

    // ---- 7: slot (item stack)

    /**
     * Get a watched string.
     * 
     * @param index - index of the watched string.
     * @return The watched string, or NULL if this value doesn't exist.
     */
    public ItemStack getItemStack(int index) {
        return (ItemStack) getObject(index);
    }

    public void setItemStack(int index, ItemStack itemStack, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getItemStackSerializer(false)), itemStack, update);
    }

    // ---- 8: boolean

    public Boolean getBoolean(int index) {
        return (Boolean) getObject(index);
    }

    public void setBoolean(int index, Boolean value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Boolean.class)), value, update);
    }

    // ---- 9: rotations
    // TODO

    // ---- 10: position

    public BlockPosition getPosition(int index) {
        return (BlockPosition) getObject(index);
    }

    public void setPosition(int index, BlockPosition position, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getBlockPositionSerializer(false)), position, update);
    }

    // ---- 11: optional position

    public Optional<BlockPosition> getOptionalPosition(int index) {
        return (Optional<BlockPosition>) getObject(index);
    }

    public void setOptionalPosition(int index, java.util.Optional<BlockPosition> position, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getBlockPositionSerializer(true)), position, update);
    }

    // ---- 12: direction

    public Direction getDirection(int index) {
        return (Direction) getObject(index);
    }

    public void setDirection(int index, Direction direction, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getDirectionSerializer()), direction, update);
    }

    // ---- 13: optional uuid

    public Optional<UUID> getOptionalUUID(int index) {
        return (Optional<UUID>) getObject(index);
    }

    public void setOptionalUUID(int index, Optional<UUID> uuid, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getUUIDSerializer(true)), uuid, update);
    }

    // ---- 14: block state

    public WrappedBlockData getBlockState(int index) {
        return (WrappedBlockData) getObject(index);
    }

    public void setBlockState(int index, WrappedBlockData blockData, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getBlockDataSerializer(false)), blockData, update);
    }

    // ---- 15: optional block state

    public Optional<WrappedBlockData> getOptionalBlockState(int index) {
        return (Optional<WrappedBlockData>) getObject(index);
    }

    public void setOptionalBlockState(int index, Optional<WrappedBlockData> value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getBlockDataSerializer(true)), value, update);
    }

    // ---- 16: NBT

    public NbtCompound getNBTCompound(int index) {
        return (NbtCompound) getObject(index);
    }

    public void setNBTCompound(int index, NbtCompound nbt, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.getNBTCompoundSerializer()), nbt, update);
    }

    // ---- 17: particle

    public WrappedParticle<?> getParticle(int index) {
        return (WrappedParticle<?>) getObject(index);
    }

    public void setParticle(int index, WrappedParticle<?> particle, boolean update) {
        // TODO: is ParticleParam correct?
        setObject(new WrappedDataWatcherObject(index, Registry.get(MinecraftReflection.getParticleParam(), false)), particle, update);
    }

    // ---- 18: villager data

    public WrappedVillagerData getVillagerData(int index) {
        return (WrappedVillagerData) getObject(index);
    }

    public void setVillagerData(int index, WrappedVillagerData data, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(WrappedVillagerData.getNmsClass(), false)), data, update);
    }

    // ---- 19: optional varint (int)

    public Optional<Integer> getOptionalInteger(int index) {
        return (Optional<Integer>) getObject(index);
    }

    public void setOptionalInteger(int index, Optional<Integer> value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Integer.class, true)), value, update);
    }

    // ---- 20: pose

    public EntityPose getPose(int index) {
        return (EntityPose) getObject(index);
    }

    public void setPose(int index, EntityPose pose, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(EnumWrappers.getEntityPoseClass(), false)), pose, update);
    }

    // TODO: 21-25, 27

    // ---- 21: cat variant

    // ---- 22: frog variant

    // ---- 23: optional global position

    // ---- 24: painting variant

    // ---- 25: sniffer state

    // ---- 26: vector3

    public Vector3F getVector3F(int index) {
        return (Vector3F) getObject(index);
    }

    public void setVector3F(int index, Vector3F vector, boolean update) {
        setObject(new WrappedDataWatcherObject(index, Registry.get(Vector3F.getMinecraftClass(), false)), vector, update);
    }

    // ---- 27: quaternion

    /**
     * Retrieve a watchable object by index.
     * 
     * @param index Index of the object to retrieve.
     * @return The watched object or null if it doesn't exist.
     */
    public Object getObject(int index) {
        return getObject(WrappedDataWatcherObject.fromIndex(index));
    }

    /**
     * Retrieve a watchable object by watcher object.
     * 
     * @param object The watcher object
     * @return The watched object or null if it doesn't exist.
     */
    public Object getObject(WrappedDataWatcherObject object) {
        return impl.getObject(object);
    }

    // ---- Object Setters

    /**
     * Sets the DataWatcher Item at a given index to a new value. In 1.9 and up,
     * you cannot register objects without a watcher object.
     * 
     * @param index Index of the object to set
     * @param value New value
     * @param update Whether or not to inform the client
     * 
     * @see WrappedDataWatcher#setObject(WrappedDataWatcherObject, Object, boolean)
     * @throws IllegalArgumentException in 1.9 and up if there isn't already an
     *      object at this index
     */
    public void setObject(int index, Object value, boolean update) {
        if (MinecraftReflection.watcherObjectExists() && !hasIndex(index)) {
            throw new IllegalArgumentException("You cannot register objects without a watcher object!");
        }

        setObject(WrappedDataWatcherObject.fromIndex(index), value, update);
    }

    /**
     * Shortcut for {@link #setObject(int, Object, boolean)}
     */
    public void setObject(int index, Object value) {
        setObject(index, value, false);
    }

    /**
     * Sets the DataWatcher Item at a given index to a new value.
     * 
     * @param index Index of the object to set
     * @param serializer Serializer from {@link Registry#get(Class)}
     * @param value New value
     * @param update Whether or not to inform the client
     * 
     * @see WrappedDataWatcher#setObject(WrappedDataWatcherObject, Object)
     */
    public void setObject(int index, Serializer serializer, Object value, boolean update) {
        setObject(new WrappedDataWatcherObject(index, serializer), value, update);
    }

    /**
     * Alias for {@link #setObject(int, Serializer, Object, boolean)}
     */
    public void setObject(int index, Serializer serializer, Object value) {
        setObject(new WrappedDataWatcherObject(index, serializer), value, false);
    }

    /**
     * Sets the DataWatcher Item at a given index to a new value.
     * 
     * @param index Index of the object to set
     * @param value New value
     * @param update Whether or not to inform the client
     * 
     * @see WrappedDataWatcher#setObject(int, Object, boolean)
     */
    public void setObject(int index, WrappedWatchableObject value, boolean update) {
        setObject(index, value.getRawValue(), update);
    }

    /**
     * Alias for {@link #setObject(int, WrappedWatchableObject, boolean)}
     */
    public void setObject(int index, WrappedWatchableObject value) {
        setObject(index, value.getRawValue(), false);
    }

    /**
     * Sets the DataWatcher Item associated with a given watcher object to a new value.
     * 
     * @param object Associated watcher object
     * @param value Wrapped value
     * @param update Whether or not to inform the client
     * 
     * @see #setObject(WrappedDataWatcherObject, Object)
     */
    public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value, boolean update) {
        setObject(object, value.getRawValue(), update);
    }

    /**
     * Shortcut for {@link #setObject(WrappedDataWatcherObject, WrappedWatchableObject, boolean)}
     */
    public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value) {
        setObject(object, value.getRawValue(), false);
    }

    /**
     * Sets the DataWatcher Item associated with a given watcher object to a
     * new value. If there is not already an object at this index, the
     * specified watcher object must have a serializer.
     * 
     * @param object Associated watcher object
     * @param value New value
     * 
     * @throws IllegalArgumentException If the watcher object is null or must
     *          have a serializer and does not have one.
     */
    public void setObject(WrappedDataWatcherObject object, Object value, boolean update) {
        impl.setObject(object, value, update);
    }

    /**
     * Shortcut for {@link #setObject(WrappedDataWatcherObject, Object, boolean)}
     */
    public void setObject(WrappedDataWatcherObject object, Object value) {
        setObject(object, value, false);
    }

    // ---- Utility Methods

    /**
     * Clone the content of the current DataWatcher.
     * 
     * @return A cloned data watcher.
     */
    public WrappedDataWatcher deepClone() {
        return new WrappedDataWatcher(impl.deepClone());
    }

    /**
     * @return
     */
    @Override
    public Object getHandle() {
        return impl.getHandle();
    }

    /**
     * Retrieve the data watcher associated with an entity.
     * 
     * @param entity - the entity to read from.
     * @return Associated data watcher.
     */
    public static WrappedDataWatcher getEntityWatcher(Entity entity) {
        return new WrappedDataWatcher(entity);
    }

    /**
     * Retrieve the entity associated with this data watcher.
     * @return The entity, or NULL.
     */
    public Entity getEntity() {
        return impl.getEntity();
    }

    /**
     * Set the entity associated with this data watcher.
     * @param entity - the new entity.
     */
    public void setEntity(Entity entity) {
        impl.setEntity(entity);
    }

    /**
     * Exports the contents of this data watcher to a list of WrappedDataValues
     * for use in the ENTITY_METADATA packet
     * @return The data value collection
     */
    public List<WrappedDataValue> toDataValueCollection() {
        List<WrappedWatchableObject> objects = impl.getWatchableObjects();
        List<WrappedDataValue> values = new ArrayList<>(objects.size());
        for (WrappedWatchableObject object : objects) {
            WrappedDataWatcherObject watcherObj = object.getWatcherObject();
            Object value = WrappedWatchableObject.getUnwrapped(object.getRawValue());
            values.add(new WrappedDataValue(watcherObj.getIndex(), watcherObj.getSerializer(), value));
        }
        return values;
    }

    private static final ImmutableBiMap<Class<?>, Integer> CLASS_TO_ID = new ImmutableBiMap.Builder<Class<?>, Integer>()
            .put(Byte.class, 0)
            .put(Short.class, 1)
            .put(Integer.class, 2)
            .put(Float.class, 3)
            .put(String.class, 4)
            .put(MinecraftReflection.getItemStackClass(), 5)
            .put(MinecraftReflection.getBlockPositionClass(), 6)
            .put(Vector3F.getMinecraftClass(), 7)
            .build();

    /**
     * Retrieves the type ID associated with a given class. No longer supported
     * in 1.9 and up due to the removal of type IDs.
     * 
     * @param clazz Class to find ID for
     * @return The ID, or null if not found
     */
    public static Integer getTypeID(Class<?> clazz) {
        return CLASS_TO_ID.get(clazz);
    }

    /**
     * Retrieves the class associated with a given type ID. No longer
     * supported in 1.9 and up due to the removal of type IDs.
     * 
     * @param typeID ID to find Class for
     * @return The Class, or null if not found
     */
    public static Class<?> getTypeClass(int typeID) {
        return CLASS_TO_ID.inverse().get(typeID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;

        if (obj instanceof WrappedDataWatcher) {
            WrappedDataWatcher other = (WrappedDataWatcher) obj;
            return this.impl.equals(other.impl);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return impl.hashCode();
    }

    @Override
    public String toString() {
        return impl.toString();
    }

    // ---- 1.9 classes

    /**
     * Represents a DataWatcherObject in 1.9. In order to register an object,
     * the serializer must be specified.
     * 
     * @author dmulloy2
     */
    public static class WrappedDataWatcherObject {
        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherObjectClass();
        private static ConstructorAccessor constructor = null;
        private static MethodAccessor getSerializer = null;

        private StructureModifier<Object> modifier;
        private Object handle;

        protected WrappedDataWatcherObject() {
        }

        /**
         * Creates a new watcher object from a NMS handle.
         * 
         * @param handle The handle
         */
        public WrappedDataWatcherObject(Object handle) {
            this.handle = handle;
            this.modifier = new StructureModifier<>(HANDLE_TYPE).withTarget(handle);
        }

        /**
         * Creates a new watcher object from an index and serializer.
         * 
         * @param index Index
         * @param serializer Serializer, see {@link Registry}
         */
        public WrappedDataWatcherObject(int index, Serializer serializer) {
            this(newHandle(index, serializer));
        }

        static WrappedDataWatcherObject fromIndex(int index) {
            if (MinecraftReflection.watcherObjectExists()) {
                return new WrappedDataWatcherObject(newHandle(index));
            } else {
                return new DummyWatcherObject(index);
            }
        }

        private static Object newHandle(int index) {
            Validate.isTrue(index >= 0, "index cannot be negative!");

            if (constructor == null) {
                constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
            }

            return constructor.invoke(index, null);
        }

        private static Object newHandle(int index, Serializer serializer) {
            Validate.isTrue(index >= 0, "index cannot be negative!");
            Validate.notNull(serializer, "serializer cannot be null!");

            if (constructor == null) {
                constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
            }

            Object handle = serializer.getHandle();
            return constructor.invoke(index, handle);
        }

        /**
         * Gets this watcher object's index.
         * 
         * @return The index
         */
        public int getIndex() {
            return (int) modifier.read(0);
        }

        /**
         * Gets this watcher object's serializer. Will return null if the
         * serializer was never specified.
         * 
         * @return The serializer, or null
         */
        public Serializer getSerializer() {
            if (getSerializer == null) {
                getSerializer = Accessors.getMethodAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getMethodByReturnTypeAndParameters("getSerializer", MinecraftReflection.getDataWatcherSerializerClass(), new Class[0]));
            }

            Object serializer = getSerializer.invoke(handle);
            if (serializer != null) {
                Serializer wrapper = Registry.fromHandle(serializer);
                if (wrapper != null) {
                    return wrapper;
                } else {
                    return new Serializer(null, serializer, false);
                }
            } else {
                return null;
            }
        }

        public void checkSerializer() {
            Validate.notNull(getSerializer(), "You must specify a serializer to register an object!");
        }

        public Object getHandle() {
            return handle;
        }

        public Class<?> getHandleType() {
            return HANDLE_TYPE;
        }

        @Override
        public String toString() {
            return "DataWatcherObject[index=" + getIndex() + ", serializer=" + getSerializer() + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;

            if (obj instanceof WrappedDataWatcherObject) {
                WrappedDataWatcherObject other = (WrappedDataWatcherObject) obj;
                return handle.equals(other.handle);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return handle.hashCode();
        }
    }

    private static class DummyWatcherObject extends WrappedDataWatcherObject {
        private final int index;

        public DummyWatcherObject(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public Serializer getSerializer() {
            return null;
        }

        @Override
        public Object getHandle() {
            return getIndex();
        }

        @Override
        public Class<?> getHandleType() {
            return int.class;
        }

        @Override
        public void checkSerializer() {
            // Do nothing
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;

            if (obj instanceof DummyWatcherObject) {
                DummyWatcherObject that = (DummyWatcherObject) obj;
                return this.index == that.index;
            }

            return false;
        }
    }

    /**
     * Represents a DataWatcherSerializer in 1.9. If a Serializer is optional,
     * values must be wrapped in a {@link Optional}.
     * 
     * @author dmulloy2
     */
    public static class Serializer extends AbstractWrapper {
        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherSerializerClass();

        private final Class<?> type;
        private final boolean optional;

        /**
         * Constructs a new Serializer
         * 
         * @param type Type it serializes
         * @param handle NMS handle
         * @param optional Whether or not it's {@link Optional}
         */
        public Serializer(Class<?> type, Object handle, boolean optional) {
            super(HANDLE_TYPE);
            this.type = type;
            this.optional = optional;

            setHandle(handle);
        }

        /**
         * Gets the type this serializer serializes.
         * 
         * @return The type
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * Whether or not this serializer is optional, that is whether or not
         * the return type is wrapped in a {@link Optional}.
         * 
         * @return True if it is, false if not
         */
        public boolean isOptional() {
            return optional;
        }

        @Override
        public String toString() {
            return "Serializer[type=" + type + ", handle=" + handle + ", optional=" + optional + "]";
        }
    }

    /**
     * Represents a DataWatcherRegistry containing the supported
     * {@link Serializer}s in 1.9.
     *
     * <ul>
     *   <li>Byte</li>
     *   <li>Integer</li>
     *   <li>Float</li>
     *   <li>String</li>
     *   <li>IChatBaseComponent</li>
     *   <li>ItemStack</li>
     *   <li>Optional&lt;IBlockData&gt;</li>
     *   <li>Boolean</li>
     *   <li>Vector3f</li>
     *   <li>BlockPosition</li>
     *   <li>Optional&lt;BlockPosition&gt;</li>
     *   <li>EnumDirection</li>
     *   <li>Optional&lt;UUID&gt;</li>
     *   <li>NBTTagCompound</li>
     * </ul>
     *
     * @author dmulloy2
     */
    public static class Registry {
        private static boolean INITIALIZED = false;

        private static Map<Class<?>, Serializer> RAW_REGISTRY = null;
        private static Map<Class<?>, Serializer> OPTIONAL_REGISTRY = null;

        /**
         * Gets the first serializer associated with a given class.
         *
         * <p><b>Note</b>: If {@link Serializer#isOptional() the serializer is optional},
         *   values <i>must</i> be wrapped in an {@link Optional}.</p>
         *
         * <p>If there are multiple serializers for a given class (i.e. BlockPosition),
         *   you should use {@link #get(Class, boolean)} for more precision.</p>
         *
         * @param clazz Class to find serializer for
         * @return The serializer, or null if none exists
         */
        public static Serializer get(Class<?> clazz) {
            Validate.notNull(clazz,"Class cannot be null!");
            initialize();

            Serializer serializer = RAW_REGISTRY.getOrDefault(clazz,
                OPTIONAL_REGISTRY.getOrDefault(clazz, null));
            if (serializer == null) {
                throw new IllegalArgumentException("No serializer found for " + clazz);
            }

            return serializer;
        }

        /**
         * Gets the first serializer associated with a given class and optional state.
         * <br/>
         * <b>Note</b>: If the serializer is optional, values <i>must</i> be
         * wrapped in an {@link Optional}
         *
         * @param clazz Class to find serializer for
         * @param optional Optional state
         * @return The serializer, or null if none exists
         */
        public static Serializer get(Class<?> clazz, boolean optional) {
            Validate.notNull(clazz, "Class cannot be null!");
            initialize();

            Serializer serializer = optional ? OPTIONAL_REGISTRY.get(clazz) : RAW_REGISTRY.get(clazz);
            if (serializer == null) {
                throw new IllegalArgumentException("No serializer found for " + (optional ? "Optional<" + clazz + ">" : clazz));
            }

            return serializer;
        }

        /**
         * Gets the serializer associated with a given NMS handle.
         * @param handle The handle
         * @return The serializer, or null if none exists
         */
        public static Serializer fromHandle(Object handle) {
            Validate.notNull(handle, "handle cannot be null!");
            initialize();

            for (Serializer serializer : RAW_REGISTRY.values()) {
                if (serializer.getHandle().equals(handle)) {
                    return serializer;
                }
            }

            for (Serializer serializer : OPTIONAL_REGISTRY.values()) {
                if (serializer.getHandle().equals(handle)) {
                    return serializer;
                }
            }

            return null;
        }

        private static void initialize() {
            if (!INITIALIZED) {
                INITIALIZED = true;
            } else {
                return;
            }

            Map<Class<?>, Serializer> rawRegistry = new HashMap<>();
            Map<Class<?>, Serializer> optionalRegistry = new HashMap<>();

            List<Field> candidates = FuzzyReflection.fromClass(MinecraftReflection.getDataWatcherRegistryClass(), true)
                    .getFieldListByType(MinecraftReflection.getDataWatcherSerializerClass());
            for (Field candidate : candidates) {
                Type generic = candidate.getGenericType();
                if (generic instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) generic;
                    Type[] args = type.getActualTypeArguments();
                    Type arg = args[0];

                    Class<?> innerClass;
                    boolean optional = false;

                    if (arg instanceof Class<?>) {
                        innerClass = (Class<?>) arg;
                    } else if (arg instanceof ParameterizedType) {
                        innerClass = (Class<?>) ((ParameterizedType) arg).getActualTypeArguments()[0];
                        optional = true;
                    } else {
                        throw new IllegalStateException("Failed to find inner class of field " + candidate);
                    }

                    Object serializer;

                    try {
                        serializer = candidate.get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to read field " + candidate);
                    }

                    if (serializer == null) {
                        throw new RuntimeException("Failed to read serializer: " + candidate.getName());
                    }

                    if (optional) {
                        optionalRegistry.put(innerClass, new Serializer(innerClass, serializer, true));
                    } else {
                        rawRegistry.put(innerClass, new Serializer(innerClass, serializer, false));
                    }
                }
            }

            RAW_REGISTRY = ImmutableMap.copyOf(rawRegistry);
            OPTIONAL_REGISTRY = ImmutableMap.copyOf(optionalRegistry);
        }

        // ---- Helper methods

        /**
         * Gets the serializer for IChatBaseComponents
         * @return The serializer
         */
        public static Serializer getChatComponentSerializer() {
            return getChatComponentSerializer(false);
        }

        /**
         * Gets the serializer for IChatBaseComponents
         * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
         * @return The serializer
         */
        public static Serializer getChatComponentSerializer(boolean optional) {
            return get(MinecraftReflection.getIChatBaseComponentClass(), optional);
        }

        /**
         * Gets the serializer for ItemStacks
         * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
         * @return The serializer
         */
        public static Serializer getItemStackSerializer(boolean optional) {
            return get(MinecraftReflection.getItemStackClass(), optional);
        }

        /**
         * Gets the serializer for BlockData
         * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
         * @return The serializer
         */
        public static Serializer getBlockDataSerializer(boolean optional) {
            return get(MinecraftReflection.getIBlockDataClass(), optional);
        }

        /**
         * Gets the serializer for Vector3Fs
         * @return The serializer
         */
        public static Serializer getVectorSerializer() {
            return get(Vector3F.getMinecraftClass());
        }

        /**
         * Gets the serializer for BlockPositions
         * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
         * @return The serializer
         */
        public static Serializer getBlockPositionSerializer(boolean optional) {
            return get(MinecraftReflection.getBlockPositionClass(), optional);
        }

        /**
         * Gets the serializer for Directions
         * @return The serializer
         */
        public static Serializer getDirectionSerializer() {
            return get(EnumWrappers.getDirectionClass());
        }

        /**
         * Gets the serializer for UUIDs
         * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
         * @return The serializer
         */
        public static Serializer getUUIDSerializer(boolean optional) {
            return get(UUID.class, optional);
        }

        /**
         * Gets the serializer for NBT Compound tags
         * @return The serializer
         */
        public static Serializer getNBTCompoundSerializer() {
            return get(MinecraftReflection.getNBTCompoundClass(), false);
        }
    }
}
