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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.collection.ConvertedMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

/**
 * Represents a DataWatcher
 * @author dmulloy2
 */
public class LegacyDataWatcher extends AbstractWrapper implements IDataWatcher {
    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherClass();

    private static MethodAccessor GETTER = null;
    private static MethodAccessor SETTER = null;
    private static MethodAccessor REGISTER = null;

    private static FieldAccessor ENTITY_DATA_FIELD = null;
    private static FieldAccessor ENTITY_FIELD = null;
    private static FieldAccessor MAP_FIELD = null;

    private static ConstructorAccessor constructor = null;
    private static ConstructorAccessor eggConstructor = null;

    private static Object fakeEntity = null;

    // ---- Construction

    /**
     * Constructs a new DataWatcher wrapper around a NMS handle. The resulting
     * DataWatcher will likely have existing values that can be removed with
     * {@link #clear()}.
     * 
     * @param handle DataWatcher handle
     */
    public LegacyDataWatcher(Object handle) {
        super(HANDLE_TYPE);
        setHandle(handle);
    }

    /**
     * Constructs a new DataWatcher using a fake egg entity. The
     * resulting DataWatcher will not have any keys or values and new ones will
     * have to be added using watcher objects.
     */
    @Deprecated
    public LegacyDataWatcher() {
        this(new ArrayList<>());
    }

    /**
     * Constructs a new DataWatcher using a real entity. The resulting
     * DataWatcher will not have any keys or values and new ones will have to
     * be added using watcher objects.
     * 
     * @param entity The entity
     */
    @Deprecated
    public LegacyDataWatcher(Entity entity) {
        this(getHandleFromEntity(entity));
    }

    /**
     * Constructs a new DataWatcher using a fake egg entity and a given
     * list of watchable objects.
     * 
     * @param objects The list of objects
     */
    @Deprecated
    public LegacyDataWatcher(List<WrappedWatchableObject> objects) {
        this(newHandle(fakeEntity(), objects));
    }

    private static Object newHandle(Object entity, List<WrappedWatchableObject> objects) {
        if (constructor == null) {
            constructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getEntityClass(),
                MinecraftReflection.getArrayClass(MinecraftReflection.getDataWatcherItemClass()));
        }

        Object[] genericItems = new Object[0];// (Object[]) ITEMS_CONVERTER.getGeneric(objects);
        return constructor.invoke(entity, genericItems);
    }

    private static Object fakeEntity() {
        if (fakeEntity != null) {
            return fakeEntity;
        }

        // We can create a fake egg without it affecting anything
        // Mojang added difficulty to lightning strikes, so this'll have to do
        if (eggConstructor == null) {
            eggConstructor = Accessors.getConstructorAccessor(
                    MinecraftReflection.getMinecraftClass("world.entity.projectile.EntityEgg", "world.entity.projectile.ThrownEgg", "EntityEgg"),
                    MinecraftReflection.getNmsWorldClass(), double.class, double.class, double.class
            );
        }

        Object world = BukkitUnwrapper.getInstance().unwrapItem(Bukkit.getWorlds().get(0));
        return fakeEntity = eggConstructor.invoke(world, 0, 0, 0);
    }

    // ---- Collection Methods

    @SuppressWarnings("unchecked")
    @Deprecated
    private Map<Integer, Object> getMap() {
        if (MAP_FIELD == null) {
            try {
                FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
                MAP_FIELD = Accessors.getFieldAccessor(fuzzy.getField(FuzzyFieldContract
                        .newBuilder()
                        .banModifier(Modifier.STATIC)
                        .typeDerivedOf(Map.class)
                        .build()));
            } catch (IllegalArgumentException ex) {
                throw new FieldAccessException("Failed to find watchable object map", ex);
            }
        }

        return (Map<Integer, Object>) MAP_FIELD.get(handle);
    }

    /**
     * Gets the contents of this DataWatcher as a map.
     * @return The contents
     */
    @Deprecated
    public Map<Integer, WrappedWatchableObject> asMap() {
        Map<Integer, Object> backingMap = getMap();

        return new ConvertedMap<Integer, Object, WrappedWatchableObject>(backingMap) {
            @Override
            protected WrappedWatchableObject toOuter(Object inner) {
                return inner != null ? new WrappedWatchableObject(inner) : null;
            }

            @Override
            protected Object toInner(WrappedWatchableObject outer) {
                return outer != null ? outer.getHandle() : null;
            }
        };
    }

    /**
     * Gets a set containing the registered indexes.
     * @return The set
     */
    @Deprecated
    public Set<Integer> getIndexes() {
        return getMap().keySet();
    }

    /**
     * Gets a list of the contents of this DataWatcher.
     * @return The contents
     */
    @SuppressWarnings("unchecked")
    public List<WrappedWatchableObject> getWatchableObjects() {
        return new ArrayList<>(asMap().values());
    }

    @Override
    public Iterator<WrappedWatchableObject> iterator() {
        return getWatchableObjects().iterator();
    }

    /**
     * Gets the size of this DataWatcher's contents.
     * @return The size
     */
    public int size() {
        return getMap().size();
    }

    /**
     * Gets the item at a given index.
     * 
     * @param index Index to get
     * @return The watchable object, or null if none exists
     */
    public WrappedWatchableObject getWatchableObject(int index) {
        Object handle = getMap().get(index);

        if (handle != null) {
            return new WrappedWatchableObject(handle);
        } else {
            return null;
        }
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
        Object removed = getMap().remove(index);
        return removed != null ? new WrappedWatchableObject(removed) : null;
    }

    /**
     * Whether or not this DataWatcher has an object at a given index.
     * 
     * @param index Index to check for
     * @return True if it does, false if not
     */
    public boolean hasIndex(int index) {
        return getMap().containsKey(index);
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
    @Deprecated
    public void clear() {
        getMap().clear();
    }

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
        Validate.notNull(object, "Watcher object cannot be null!");

        if (GETTER == null) {
            FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);

            if (MinecraftReflection.watcherObjectExists()) {            
                GETTER = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
                    .parameterExactType(object.getHandleType())
                    .returnTypeExact(Object.class)
                    .build(), "get"));
            } else {
                GETTER = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
                    .parameterExactType(int.class)
                    .returnTypeExact(MinecraftReflection.getDataWatcherItemClass())
                    .build()));
            }
        }

        try {
            Object value = GETTER.invoke(handle, object.getHandle());
            return WrappedWatchableObject.getWrapped(value);
        } catch (Exception ex) {
            // Nothing exists at this index
            return null;
        }
    }

    /**
     * @param object
     * @param value
     * @param update
     */
    @Override
    public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value, boolean update) {
        setObject(object, value.getRawValue(), update);
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
        Validate.notNull(object, "Watcher object cannot be null!");

        if (SETTER == null && REGISTER == null) {
            FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
            FuzzyMethodContract contract = FuzzyMethodContract.newBuilder()
                    .banModifier(Modifier.STATIC)
                    .requireModifier(Modifier.PUBLIC)
                    .parameterExactArray(object.getHandleType(), Object.class)
                    .build();
            List<Method> methods = fuzzy.getMethodList(contract);
            for (Method method : methods) {
                if (method.getName().equals("set") || method.getName().equals("watch") || method.getName().equals("b")) {
                    SETTER = Accessors.getMethodAccessor(method);
                } else {
                    REGISTER = Accessors.getMethodAccessor(method);
                }
            }
        }

        // Unwrap the object
        value = WrappedWatchableObject.getUnwrapped(value);

        if (hasIndex(object.getIndex())) {
            SETTER.invoke(handle, object.getHandle(), value);
        } else {
            object.checkSerializer();
            REGISTER.invoke(handle, object.getHandle(), value);
        }

        if (update) {
            getWatchableObject(object.getIndex()).setDirtyState(update);
        }
    }

    // ---- Utility Methods

    /**
     * Clone the content of the current DataWatcher.
     * 
     * @return A cloned data watcher.
     */
    public IDataWatcher deepClone() {
        LegacyDataWatcher clone = new LegacyDataWatcher(getEntity());
        for (WrappedWatchableObject wrapper : this) {
            clone.setObject(wrapper.getWatcherObject(), wrapper, false);
        }

        return clone;
    }

    private static Object getHandleFromEntity(Entity entity) {
        if (ENTITY_DATA_FIELD == null) {
            ENTITY_DATA_FIELD = Accessors.getFieldAccessor(MinecraftReflection.getEntityClass(), MinecraftReflection.getDataWatcherClass(), true);
        }

        BukkitUnwrapper unwrapper = new BukkitUnwrapper();
        Object handle = ENTITY_DATA_FIELD.get(unwrapper.unwrapItem(entity));
        return handle;
    }

    private Object getEntityHandle() {
        if (ENTITY_FIELD == null) {
            ENTITY_FIELD = Accessors.getFieldAccessor(HANDLE_TYPE, MinecraftReflection.getEntityClass(), true);
        }

        Object entity = ENTITY_FIELD.get(handle);
        if (entity == null) {
            throw new NullPointerException(handle + "." + ENTITY_FIELD);
        }

        return entity;
    }

    /**
     * Retrieve the entity associated with this data watcher.
     * @return The entity, or NULL.
     */
    public Entity getEntity() {
        Object entity = getEntityHandle();
        return (Entity) MinecraftReflection.getBukkitEntity(entity);
    }

    /**
     * Set the entity associated with this data watcher.
     * @param entity - the new entity.
     */
    public void setEntity(Entity entity) {
        if (ENTITY_FIELD == null) {
            ENTITY_FIELD = Accessors.getFieldAccessor(HANDLE_TYPE, MinecraftReflection.getEntityClass(), true);
        }

        ENTITY_FIELD.set(handle, BukkitUnwrapper.getInstance().unwrapItem(entity));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;

        if (obj instanceof LegacyDataWatcher) {
            LegacyDataWatcher other = (LegacyDataWatcher) obj;
            Iterator<WrappedWatchableObject> first = iterator(), second = other.iterator();

            // Make sure they're the same size
            if (size() != other.size())
                return false;

            for (; first.hasNext() && second.hasNext();) {
                // See if the two elements are equal
                if (!first.next().equals(second.next()))
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getWatchableObjects().hashCode();
    }

    @Override
    public String toString() {
        return "LegacyDataWatcher[handle=" + handle + "]";
    }
}
