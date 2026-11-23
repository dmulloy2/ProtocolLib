package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultInstancesTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testTriesEveryConstructorWithTheFewestParameters() {
        List<Constructor<?>> attempted = new ArrayList<>();
        DefaultInstances instances = new DefaultInstances(PrimitiveGenerator.INSTANCE) {
            @Override
            protected <T> T createInstance(Class<T> type, Constructor<T> constructor, Class<?>[] types, Object[] params) {
                attempted.add(constructor);
                // the constructor listed first by the JVM cannot create the type, whichever one that is
                return attempted.size() == 1 ? null : super.createInstance(type, constructor, types, params);
            }
        };

        assertNotNull(instances.getDefault(TiedConstructors.class));
        assertEquals(2, attempted.size());
    }

    @Test
    public void testCycleIsNotExploredUntilTheMaximumRecursion() {
        AtomicInteger attempts = new AtomicInteger();
        DefaultInstances instances = new DefaultInstances(PrimitiveGenerator.INSTANCE) {
            @Override
            protected <T> T createInstance(Class<T> type, Constructor<T> constructor, Class<?>[] types, Object[] params) {
                attempts.incrementAndGet();
                return super.createInstance(type, constructor, types, params);
            }
        };

        // Every constructor needs one of the other two types and rejects null. Trying both constructors of each type
        // on every level up to the maximum recursion of 20 would take about a million attempts.
        assertNull(instances.getDefault(CycleA.class));
        assertTrue(attempts.get() < 100, attempts.get() + " attempts");
    }

    @Test
    public void testVec3() {
        // Vec3(Vector3fc) cannot be created from default values, Vec3(Vec3i) can
        assertEquals(Vec3.ZERO, DefaultInstances.DEFAULT.getDefault(Vec3.class));
    }

    public static final class CycleA {

        public CycleA(CycleB value) {
            Objects.requireNonNull(value);
        }

        public CycleA(CycleC value) {
            Objects.requireNonNull(value);
        }
    }

    public static final class CycleB {

        public CycleB(CycleA value) {
            Objects.requireNonNull(value);
        }

        public CycleB(CycleC value) {
            Objects.requireNonNull(value);
        }
    }

    public static final class CycleC {

        public CycleC(CycleA value) {
            Objects.requireNonNull(value);
        }

        public CycleC(CycleB value) {
            Objects.requireNonNull(value);
        }
    }

    public static final class TiedConstructors {

        public TiedConstructors(String value) {
        }

        public TiedConstructors(Integer value) {
        }

        public TiedConstructors(String first, String second) {
        }
    }
}
