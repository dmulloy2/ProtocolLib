package com.comphenix.protocol.wrappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

import org.bukkit.entity.Entity;

public interface IDataWatcher extends Iterable<WrappedWatchableObject> {

    Map<Integer, WrappedWatchableObject> asMap();

    Set<Integer> getIndexes();

    List<WrappedWatchableObject> getWatchableObjects();

    int size();

    WrappedWatchableObject getWatchableObject(int index);

    WrappedWatchableObject remove(int index);

    boolean hasIndex(int index);

    void clear();

    Object getObject(int index);

    Object getObject(WrappedDataWatcherObject object);

    void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value, boolean update);

    void setObject(WrappedDataWatcherObject object, Object value, boolean update);

    IDataWatcher deepClone();

    Object getHandle();

    @Deprecated
    Entity getEntity();

    @Deprecated
    void setEntity(Entity entity);
}
