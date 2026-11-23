package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void testVec3() {
        // Vec3(Vector3fc) cannot be created from default values, Vec3(Vec3i) can
        assertEquals(Vec3.ZERO, DefaultInstances.DEFAULT.getDefault(Vec3.class));
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
