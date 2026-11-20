package com.comphenix.protocol.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
