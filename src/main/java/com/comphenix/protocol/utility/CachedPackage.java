/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2012 Kristian S.
 * Stangeland
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.utility;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a dynamic package and an arbitrary number of cached classes.
 *
 * @author Kristian
 */
final class CachedPackage {

    private final String packageName;
    private final ClassSource source;
    private final Map<String, Optional<Class<?>>> cache;

    /**
     * Construct a new cached package.
     *
     * @param packageName - the name of the current package.
     * @param source      - the class source.
     */
    CachedPackage(String packageName, ClassSource source) {
        this.source = source;
        this.packageName = packageName;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Correctly combine a package name and the child class we're looking for.
     *
     * @param packageName - name of the package, or an empty string for the default package.
     * @param className   - the class name.
     * @return We full class path.
     */
    public static String combine(String packageName, String className) {
        if (packageName == null || packageName.isEmpty()) {
            return className;
        } else {
            return packageName + "." + className;
        }
    }

    /**
     * Associate a given class with a class name.
     *
     * @param className - class name.
     * @param clazz     - type of class.
     */
    public void setPackageClass(String className, Class<?> clazz) {
        Optional<Class<?>> previous = cache.get(className);
        if (previous != null && previous.isPresent()) {
            throw new IllegalStateException("Tried to redefine class " + className);
        }

        cache.put(className, Optional.ofNullable(clazz));
    }

    public void removePackageClass(String className) {
        cache.remove(className);
    }

    private Optional<Class<?>> resolveClass(String className) {
        return source.loadClass(combine(packageName, className));
    }

    /**
     * Retrieve the class object of a specific class in the current package.
     *
     * @param className - the specific class.
     * @return Class object.
     * @throws RuntimeException If we are unable to find the given class.
     */
    public Optional<Class<?>> getPackageClass(String className, String... aliases) {
        // Fast path: a plain get on cache hit. computeIfAbsent(className, x -> {...}) allocated a
        // fresh capturing lambda (capturing className + aliases) on EVERY call — even hits — which,
        // on the per-packet isBundlePacket() path, was a large source of allocation. The negative
        // result (class absent on this version) is cached too, so this stays a single get afterwards.
        Optional<Class<?>> cached = cache.get(className);
        if (cached != null) {
            return cached;
        }

        Optional<Class<?>> clazz = resolveClass(className);
        if (!clazz.isPresent()) {
            for (String alias : aliases) {
                Optional<Class<?>> aliasClazz = resolveClass(alias);
                if (aliasClazz.isPresent()) {
                    clazz = aliasClazz;
                    break;
                }
            }
        }

        cache.put(className, clazz);
        return clazz;
    }
}
