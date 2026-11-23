package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructureModifierDerivedCacheTest {

    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testTypedModifierCreatesNoCacheUntilItIsNarrowed() throws Exception {
        StructureModifier<Object> root = new StructureModifier<>(Sample.class, null, false);
        StructureModifier<Object> objects = root.withType(Object.class);

        Object derived = derivedModifiers(objects);
        assertNull(field(derived, "byType"));
        assertNull(field(derived, "byParamType"));
        assertTrue(objects.defaultFields.isEmpty());
        assertNull(objects.subtypeCache);

        // narrowing a copy creates the cache, and every copy of the typed modifier shares it
        Sample first = new Sample("first", "a");
        Sample second = new Sample("second", "b");
        StructureModifier<String> firstNames = objects.withTarget(first).withType(String.class);
        Map<?, ?> byType = (Map<?, ?>) field(derived, "byType");
        assertNotNull(byType);
        assertEquals(1, byType.size());
        Object cached = byType.get(String.class);

        StructureModifier<String> secondNames = objects.withTarget(second).withType(String.class);
        assertSame(cached, byType.get(String.class));
        assertEquals(1, byType.size());
        assertEquals("first", firstNames.read(0));
        assertEquals("second", secondNames.read(0));

        secondNames.write(0, "changed");
        assertEquals("changed", second.name);
        assertEquals("first", first.name);
    }

    @Test
    public void testParameterizedLookupsShareTheirCache() throws Exception {
        StructureModifier<Object> root = new StructureModifier<>(Sample.class, null, false);
        Sample first = new Sample("first", "a");
        Sample second = new Sample("second", "b");

        StructureModifier<List<String>> firstTags = root.withTarget(first).withParamType(List.class, null, String.class);
        StructureModifier<List<String>> secondTags = root.withTarget(second).withParamType(List.class, null, String.class);
        assertEquals(Arrays.asList("a"), firstTags.read(0));
        assertEquals(Arrays.asList("b"), secondTags.read(0));

        Map<?, ?> byParamType = (Map<?, ?>) field(derivedModifiers(root), "byParamType");
        assertEquals(1, byParamType.size());
    }

    @Test
    public void testConstructedModifierKeepsItsSubtypeCache() {
        // subclasses may still read the cache of modifiers created by the public constructors
        StructureModifier<Object> root = new StructureModifier<>(Sample.class, null, false);
        assertNotNull(root.subtypeCache);

        StructureModifier<String> names = root.withType(String.class);
        assertEquals(1, root.subtypeCache.size());
        assertEquals(names.getFields(), ((StructureModifier<?>) root.subtypeCache.get(String.class)).getFields());
    }

    @Test
    public void testModifierThatWasNeverInitializedCanBeNarrowed() {
        StructureModifier<Object> uninitialized = new StructureModifier<Object>() { };
        assertEquals(0, uninitialized.withType(int.class).size());
    }

    private static Object derivedModifiers(StructureModifier<?> modifier) throws ReflectiveOperationException {
        Field field = StructureModifier.class.getDeclaredField("derivedModifiers");
        field.setAccessible(true);
        return field.get(modifier);
    }

    private static Object field(Object owner, String name) throws ReflectiveOperationException {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    @SuppressWarnings("unused")
    private static final class Sample {

        private int id = 1;
        private String name;
        private List<String> tags;

        private Sample(String name, String tag) {
            this.name = name;
            this.tags = Arrays.asList(tag);
        }
    }
}
