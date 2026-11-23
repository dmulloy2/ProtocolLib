package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    public void testKnownConstructorIsNotUsedWhereTheMaximumRecursionCutsTheSearch() {
        DefaultInstances instances = new DefaultInstances(PrimitiveGenerator.INSTANCE);
        instances.setNonNull(true);
        instances.setMaximumRecursion(2);

        for (int i = 0; i < 2; i++) {
            // inside Outer, the leaf is beyond the maximum recursion, so Choice can only use its other constructor
            assertFalse(instances.getDefault(Outer.class).choice.fromLeaf);
            // on its own, Choice is created from a leaf
            assertTrue(instances.getDefault(Choice.class).fromLeaf);
        }
    }

    @Test
    public void testKnownConstructorIsNotUsedFurtherDownACycle() {
        // A new Link needs a Node, and the Node created from a Link would need a Link again: that cycle ends with
        // null, which Node(Link) rejects. On its own, a Node can be created from a Link that holds null. Earlier
        // calls must not change which constructor creates the value of a type.
        DefaultInstances nodeFirst = new DefaultInstances(PrimitiveGenerator.INSTANCE);
        DefaultInstances linkFirst = new DefaultInstances(PrimitiveGenerator.INSTANCE);
        boolean nodeLinked = nodeFirst.getDefault(Node.class).linked;
        boolean linkNodeLinked = linkFirst.getDefault(Link.class).node.linked;

        for (int i = 0; i < 2; i++) {
            assertEquals(nodeLinked, linkFirst.getDefault(Node.class).linked);
            assertEquals(linkNodeLinked, nodeFirst.getDefault(Link.class).node.linked);
        }
    }

    @Test
    public void testConstructorThatACustomProviderRuledOutIsTriedAgain() {
        // A custom provider can return a value for a type that it returned null for before. The search must then
        // choose the constructor that this value allows.
        AtomicReference<Runnable> task = new AtomicReference<>();
        InstanceProvider tasks = type -> type == Runnable.class ? task.get() : null;
        DefaultInstances instances = DefaultInstances.fromArray(PrimitiveGenerator.INSTANCE, tasks);
        instances.setNonNull(true);

        assertFalse(instances.getDefault(Task.class).fromRunnable);
        task.set(() -> {
        });
        assertTrue(instances.getDefault(Task.class).fromRunnable);
    }

    @Test
    public void testConstructorThatASubclassRefusedIsTriedAgain() {
        // An override of createInstance can create an instance with a constructor that it refused before. The search
        // must then use the constructor listed first by the JVM again, whichever one that is.
        List<Constructor<?>> attempted = new ArrayList<>();
        DefaultInstances instances = new DefaultInstances(PrimitiveGenerator.INSTANCE) {
            @Override
            protected <T> T createInstance(Class<T> type, Constructor<T> constructor, Class<?>[] types, Object[] params) {
                attempted.add(constructor);
                // only the very first attempt fails
                return attempted.size() == 1 ? null : super.createInstance(type, constructor, types, params);
            }
        };

        assertNotNull(instances.getDefault(TiedConstructors.class));
        int firstSearch = attempted.size();
        assertNotNull(instances.getDefault(TiedConstructors.class));
        assertEquals(attempted.get(0), attempted.get(firstSearch));
    }

    public static final class Task {

        public final boolean fromRunnable;

        public Task(Runnable runnable) {
            this.fromRunnable = true;
        }

        public Task(int first, int second) {
            this.fromRunnable = false;
        }
    }

    public static final class Leaf {
    }

    public static final class Choice {

        public final boolean fromLeaf;

        public Choice(Leaf leaf) {
            this.fromLeaf = true;
        }

        public Choice(int first, int second) {
            this.fromLeaf = false;
        }
    }

    public static final class Outer {

        public final Choice choice;

        public Outer(Choice choice) {
            this.choice = choice;
        }
    }

    public static final class Node {

        public final boolean linked;

        public Node(Link link) {
            Objects.requireNonNull(link);
            this.linked = true;
        }

        public Node(String name) {
            this.linked = false;
        }
    }

    public static final class Link {

        public final Node node;

        public Link(Node node) {
            this.node = node;
        }
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
