package com.comphenix.protocol.wrappers;

import java.lang.reflect.Modifier;
import java.util.HashMap;
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
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InMemoryDataWatcher implements IDataWatcher {
    private Object entityHandle;
    private Map<Integer, WrappedWatchableObject> entries = new HashMap<>();

    public InMemoryDataWatcher() {

    }

    public InMemoryDataWatcher(Object handle) {
        this.entityHandle = entityFromWatcherHandle(handle);
        this.populateFromHandle(handle);
    }

    public InMemoryDataWatcher(Entity entity) {
        this.entityHandle = BukkitUnwrapper.getInstance().unwrapItem(entity);
        this.populateFromEntity(entity);
    }

    public InMemoryDataWatcher(List<WrappedWatchableObject> objects) {
        for (WrappedWatchableObject obj : objects) {
            entries.put(obj.getIndex(), obj);
        }
    }

    private static FieldAccessor ENTITY_WATCHER_FIELD;
    private static FieldAccessor WATCHER_ENTITY_FIELD;
    private static FieldAccessor ARRAY_FIELD;
    private static ConstructorAccessor CONSTRUCTOR;

    private static boolean ARRAY_BACKED = MinecraftVersion.v1_20_5.atOrAbove();

    private static Class<?> SYNCED_DATA_HOLDER_CLASS = ARRAY_BACKED
        ? MinecraftReflection.getMinecraftClass("network.syncher.SyncedDataHolder")
        : MinecraftReflection.getEntityClass();

    public static WrappedDataWatcher getEntityWatcher(Entity entity) {
        return new WrappedDataWatcher(entity);
    }

    private static Object entityFromWatcherHandle(Object handle) {
        if (WATCHER_ENTITY_FIELD == null) {
            WATCHER_ENTITY_FIELD = Accessors.getFieldAccessor(MinecraftReflection.getDataWatcherClass(), SYNCED_DATA_HOLDER_CLASS, true);
        }

        return WATCHER_ENTITY_FIELD.get(handle);
    }

    public void populateFromEntity(Entity entity) {
        populateFromEntity(BukkitUnwrapper.getInstance().unwrapItem(entity));
    }

    public void applyToEntity(Entity entity) {
        applyToEntity(BukkitUnwrapper.getInstance().unwrapItem(entity));
    }

    private void applyToEntity(Object entityHandle) {
        if (ENTITY_WATCHER_FIELD == null) {
            ENTITY_WATCHER_FIELD = Accessors.getFieldAccessor(MinecraftReflection.getEntityClass(), MinecraftReflection.getDataWatcherClass(), true);
        }

        Object handle = getHandle(entityHandle);
        ENTITY_WATCHER_FIELD.set(entityHandle, handle);
    }

    private void populateFromHandle(Object handle) {
        if (ARRAY_FIELD == null) {
            try {
                FuzzyReflection fuzzy = FuzzyReflection.fromClass(handle.getClass(), true);
                ARRAY_FIELD = Accessors.getFieldAccessor(fuzzy.getField(FuzzyFieldContract
                    .newBuilder()
                    .banModifier(Modifier.STATIC)
                    .typeDerivedOf(Object[].class)
                    .build()));
            } catch (IllegalArgumentException ex) {
                throw new FieldAccessException("Failed to find watchable object array", ex);
            }
        }

        Object[] backing = (Object[]) ARRAY_FIELD.get(handle);
        for (Object itemHandle : backing) {
            if (itemHandle == null) {
                continue;
            }

            WrappedWatchableObject object = new WrappedWatchableObject(itemHandle);
            entries.put(object.getIndex(), object);
        }
    }

    private void populateFromEntity(Object entityHandle) {
        if (ENTITY_WATCHER_FIELD == null) {
            ENTITY_WATCHER_FIELD = Accessors.getFieldAccessor(MinecraftReflection.getEntityClass(), MinecraftReflection.getDataWatcherClass(), true);
        }

        Object handle = ENTITY_WATCHER_FIELD.get(entityHandle);
        populateFromHandle(handle);
    }

    public Object getHandle() {
        return getHandle(entityHandle);
    }

    public Object getHandle(Object entityHandle) {
        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(MinecraftReflection.getDataWatcherClass(),
                SYNCED_DATA_HOLDER_CLASS, MinecraftReflection.getArrayClass(MinecraftReflection.getDataWatcherItemClass()));
        }

        if (CONSTRUCTOR == null) {
            throw new IllegalStateException("Cannot find constructor for DataWatcher.");
        }

        Object[] items = new Object[entries.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = entries.get(i).getHandle();
        }

        return CONSTRUCTOR.invoke(null, entityHandle, items);
    }

    /**
     * @return
     */
    @Override
    public IDataWatcher deepClone() {
        InMemoryDataWatcher clone = new InMemoryDataWatcher();
        clone.entries = new HashMap<>(this.entries.size());
        clone.entityHandle = this.entityHandle;

        for (WrappedWatchableObject object : this) {
            clone.setObject(object.getWatcherObject(), object.getValue(), false);
        }

        return clone;
    }

    /**
     * @return
     */
    @Override
    @Nullable
    @Deprecated
    public Entity getEntity() {
        return entityHandle != null ? (Entity) MinecraftReflection.getBukkitEntity(entityHandle) : null;
    }

    /**
     * @param entity
     */
    @Override
    @Deprecated
    public void setEntity(Entity entity) {
        this.entityHandle = BukkitUnwrapper.getInstance().unwrapItem(entity);
    }

    /**
     * @return
     */
    @Override
    public Map<Integer, WrappedWatchableObject> asMap() {
        return ImmutableMap.copyOf(entries);
    }

    /**
     * @return
     */
    @Override
    public Set<Integer> getIndexes() {
        return entries.keySet();
    }

    /**
     * @return
     */
    @Override
    public List<WrappedWatchableObject> getWatchableObjects() {
        return Lists.newArrayList(entries.values());
    }

    /**
     * @return
     */
    @Override
    public int size() {
        return entries.size();
    }

    /**
     * @param index
     * @return
     */
    @Override
    public WrappedWatchableObject getWatchableObject(int index) {
        return entries.get(index);
    }

    /**
     * @param index
     * @return
     */
    @Override
    public WrappedWatchableObject remove(int index) {
        return entries.remove(index);
    }

    /**
     * @param index
     * @return
     */
    @Override
    public boolean hasIndex(int index) {
        return entries.containsKey(index);
    }

    /**
     *
     */
    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Object getObject(int index) {
        WrappedWatchableObject obj = getWatchableObject(index);
        return obj != null ? obj.getValue() : null;
    }

    /**
     * @param object
     * @return
     */
    @Override
    public Object getObject(WrappedDataWatcherObject object) {
        return getObject(object.getIndex());
    }

    /**
     * @param object
     * @param value
     * @param update
     */
    @Override
    public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value, boolean update) {
        entries.put(object.getIndex(), value);

        if (update) {
            value.setDirtyState(true);
        }
    }

    /**
     * @param object
     * @param value
     * @param update
     */
    @Override
    public void setObject(WrappedDataWatcherObject object, Object value, boolean update) {
        setObject(object, new WrappedWatchableObject(object, value), update);
    }

    /**
     * @return
     */
    @NotNull
    @Override
    public Iterator<WrappedWatchableObject> iterator() {
        return entries.values().iterator();
    }
}
