package com.comphenix.protocol.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.events.InternalStructure;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SharedStructureModifierTest {

    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testForClassIsSharedAndTargetsInstances() {
        StructureModifier<Object> shared = StructureModifier.forClass(Sample.class);
        assertSame(shared, StructureModifier.forClass(Sample.class));
        assertEquals(4, shared.size());

        Sample first = new Sample(1, 2, "first");
        Sample second = new Sample(3, 4, "second");

        StructureModifier<Integer> firstInts = shared.withTarget(first).withType(int.class);
        StructureModifier<Integer> secondInts = shared.withTarget(second).withType(int.class);
        assertEquals(1, firstInts.read(0));
        assertEquals(4, secondInts.read(1));
        assertEquals("second", shared.withTarget(second).<String>withType(String.class).read(0));

        secondInts.write(0, 9);
        assertEquals(9, second.a);
        assertEquals(1, first.a);
    }

    @Test
    public void testConcurrentTypeLookupsOnSharedModifier() throws Exception {
        // shared modifiers fill their sub type caches from netty and plugin threads at the same time
        StructureModifier<Object> shared = new StructureModifier<>(Sample.class, null, false);
        Class<?>[] types = {int.class, long.class, String.class, Object.class, double.class, CharSequence.class};

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int thread = 0; thread < 8; thread++) {
                futures.add(pool.submit(() -> {
                    for (int i = 0; i < 2000; i++) {
                        Sample sample = new Sample(i, i + 1, "value");
                        StructureModifier<Object> target = shared.withTarget(sample);
                        assertEquals(i, target.withType(int.class).read(0));
                        assertEquals(5L, target.withType(long.class).read(0));
                        assertEquals("value", target.withType(String.class).read(0));
                        for (Class<?> type : types) {
                            target.withType(type).size();
                        }
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                future.get(2, TimeUnit.MINUTES);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testSharedCachesDoNotRetainForeignClassLoaders() throws Exception {
        // classes of another plugin: once that plugin is disabled, its class loader must be collectable
        WeakReference<ClassLoader> loader = useForeignClass();
        for (int i = 0; i < 50 && loader.get() != null; i++) {
            System.gc();
            Thread.sleep(20);
        }
        assertNull(loader.get(), "the class loader of a foreign class is still reachable");

        // classes that live as long as ProtocolLib stay shared
        assertSame(StructureModifier.forClass(Sample.class), StructureModifier.forClass(Sample.class));
    }

    private static WeakReference<ClassLoader> useForeignClass() throws Exception {
        ClassLoader foreignLoader = new ForeignClassLoader(SharedStructureModifierTest.class.getClassLoader());
        Class<?> foreignClass = foreignLoader.loadClass(ForeignStructure.class.getName());
        assertNotSame(ForeignStructure.class, foreignClass);
        Constructor<?> constructor = foreignClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();

        // the shared modifier, the internal structure cache and the sub type cache of a shared server class modifier
        assertEquals(2, StructureModifier.forClass(foreignClass).withTarget(instance).size());
        assertNotNull(InternalStructure.getConverter().getSpecific(instance));
        StructureModifier.forClass(Sample.class).withType(foreignClass);
        return new WeakReference<>(foreignLoader);
    }

    // defines its own copy of ForeignStructure, like the class loader of another plugin
    private static final class ForeignClassLoader extends ClassLoader {

        private ForeignClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.equals(ForeignStructure.class.getName())) {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = input.readAllBytes();
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException exception) {
                        throw new ClassNotFoundException(name, exception);
                    }
                }
                return loaded;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class Sample {

        private int a;
        private int b;
        private long c = 5L;
        private String name;

        private Sample(int a, int b, String name) {
            this.a = a;
            this.b = b;
            this.name = name;
        }
    }
}

@SuppressWarnings("unused")
class ForeignStructure {

    private int value = 1;
    private String name = "foreign";
}
