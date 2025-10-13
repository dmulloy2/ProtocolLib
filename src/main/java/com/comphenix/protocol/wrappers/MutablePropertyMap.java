package com.comphenix.protocol.wrappers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

/**
 * Mutable version of PropertyMap
 * Credit to PaperMC: https://github.com/PaperMC/Paper/blob/main/paper-server/src/main/java/io/papermc/paper/profile/MutablePropertyMap.java
 */
public class MutablePropertyMap extends PropertyMap {
    private final Multimap<String, Property> properties = HashMultimap.create();

    public MutablePropertyMap() {
        super(ImmutableMultimap.of());
    }

    public MutablePropertyMap(final Multimap<String, Property> properties) {
        this();
        this.putAll(properties);
    }

    @Override
    protected Multimap<String, Property> delegate() {
        return this.properties;
    }
}
