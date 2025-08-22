package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.utility.MinecraftReflection;

public class ResourceKey {
    private static final AutoWrapper<ResourceKey> AUTO_WRAPPER = AutoWrapper.wrap(ResourceKey.class, MinecraftReflection.getResourceKey())
        .field(0, MinecraftKey.getConverter())
        .field(1, MinecraftKey.getConverter());

    MinecraftKey registryName;
    MinecraftKey location;

    ResourceKey() {

    }

    public ResourceKey(MinecraftKey registryName, MinecraftKey location) {
        this.registryName = registryName;
        this.location = location;
    }

    public MinecraftKey getRegistryName() {
        return registryName;
    }

    public MinecraftKey getLocation() {
        return location;
    }

    public void setRegistryName(MinecraftKey registryName) {
        this.registryName = registryName;
    }

    public void setLocation(MinecraftKey location) {
        this.location = location;
    }

    public static ResourceKey fromGeneric(Object generic) {
        return AUTO_WRAPPER.wrap(generic);
    }

    public Object toGeneric() {
        return AUTO_WRAPPER.unwrap(this);
    }
}
