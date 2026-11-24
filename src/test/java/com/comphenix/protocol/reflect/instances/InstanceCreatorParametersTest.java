package com.comphenix.protocol.reflect.instances;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.cloning.ImmutableDetector;
import com.comphenix.protocol.wrappers.BlockPosition;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstanceCreatorParametersTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testOnlyValuesThatCannotChangeAreShared() {
        InstanceCreator creator = InstanceCreator.forClass(Holder.class);
        Holder first = (Holder) creator.get();
        Holder second = (Holder) creator.get();
        Holder third = (Holder) creator.get();

        // no default value: every instance receives null
        assertNull(first.missing);
        assertNull(third.missing);

        // the same boxed number every time
        assertEquals(0, first.number);
        assertSame(first.number, third.number);

        // values that can change are created for every instance
        assertNotSame(second.list, third.list);
        assertNotSame(second.element, third.element);
        assertNotNull(third.element);
        second.list.add("changed");
        assertTrue(third.list.isEmpty());
    }

    @Test
    public void testParameterWithoutDefaultIsNotSearchedAgain() {
        InstanceCreator creator = InstanceCreator.forClass(NeedsNoDefault.class);
        assertNull(((NeedsNoDefault) creator.get()).value);

        int attempts = NoDefault.attempts;
        for (int i = 0; i < 5; i++) {
            assertNull(((NeedsNoDefault) creator.get()).value);
        }
        assertEquals(attempts, NoDefault.attempts);
    }

    @Test
    public void testFactoryMethodParametersArePlannedToo() {
        InstanceCreator creator = InstanceCreator.forClass(FromFactory.class);
        FromFactory first = (FromFactory) creator.get();
        FromFactory second = (FromFactory) creator.get();

        assertNull(second.missing);
        assertNotSame(first.list, second.list);
        first.list.add("changed");
        assertTrue(second.list.isEmpty());
    }

    @Test
    public void testFailingConstructorIsNotTriedForEveryValue() {
        // see InstanceCreatorTest for the order in which HotSpot lists the constructors of these classes
        for (int i = 0; i < 3; i++) {
            assertNotNull(((FailingFirst) DefaultInstances.DEFAULT.create(FailingFirst.class)).value);
            assertNotNull(((FailingLast) DefaultInstances.DEFAULT.create(FailingLast.class)).value);
        }
        assertTrue(FailingFirst.failures <= 1, "failed " + FailingFirst.failures + " times");
        assertTrue(FailingLast.failures <= 1, "failed " + FailingLast.failures + " times");

        // still a new value every time
        assertNotSame(DefaultInstances.DEFAULT.create(FailingFirst.class), DefaultInstances.DEFAULT.create(FailingFirst.class));
    }

    @Test
    public void testParametersFollowTheSettingsOfTheDefaultInstances() {
        int maximumRecursion = DefaultInstances.DEFAULT.getMaximumRecursion();
        InstanceCreator creator = InstanceCreator.forClass(NeedsChain.class);
        try {
            // the end of the chain is beyond the maximum recursion, so the chain cannot be created
            DefaultInstances.DEFAULT.setMaximumRecursion(1);
            assertNull(((NeedsChain) creator.get()).chain);
            assertNull(((NeedsChain) creator.get()).chain);

            DefaultInstances.DEFAULT.setMaximumRecursion(maximumRecursion);
            assertNotNull(((NeedsChain) creator.get()).chain);
        } finally {
            DefaultInstances.DEFAULT.setMaximumRecursion(maximumRecursion);
        }
    }

    @Test
    public void testVec3Default() {
        // Vec3(Vector3fc) cannot be created from default values, Vec3(Vec3i) can
        for (int i = 0; i < 3; i++) {
            assertEquals(Vec3.ZERO, DefaultInstances.DEFAULT.create(Vec3.class));
        }
    }

    @Test
    public void testBlockChangePacket() {
        PacketContainer first = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        PacketContainer second = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        assertEquals(new BlockPosition(0, 0, 0), second.getBlockPositionModifier().read(0));

        first.getBlockPositionModifier().write(0, new BlockPosition(1, 2, 3));
        assertEquals(new BlockPosition(1, 2, 3), first.getBlockPositionModifier().read(0));
        assertEquals(new BlockPosition(0, 0, 0), second.getBlockPositionModifier().read(0));
    }

    @Test
    public void testNewPacketsDoNotShareValuesThatCanChange() {
        PacketType[] types = {
                PacketType.Play.Server.BLOCK_CHANGE,
                PacketType.Play.Server.ENTITY_TELEPORT,
                PacketType.Play.Server.POSITION,
                PacketType.Play.Server.ENTITY_VELOCITY,
                PacketType.Play.Server.TILE_ENTITY_DATA,
                PacketType.Play.Server.TAB_COMPLETE,
                PacketType.Play.Server.LOGIN,
                PacketType.Play.Client.UPDATE_SIGN,
        };
        for (PacketType type : types) {
            StructureModifier<Object> first = new PacketContainer(type).getModifier();
            StructureModifier<Object> second = new PacketContainer(type).getModifier();
            for (int i = 0; i < first.size(); i++) {
                Object value = second.read(i);
                if (value == null || cannotChange(value)) {
                    continue;
                }
                assertNotSame(first.read(i), value, type + " shares field " + first.getField(i).getName());
            }
            assertNotNull(new PacketContainer(type).deepClone());
        }
    }

    private static boolean cannotChange(Object value) {
        if (value instanceof Optional || ImmutableDetector.isImmutable(value.getClass())) {
            return true;
        }
        // immutable collections of the JDK, such as the empty list that Stream.toList() returns every time
        return value.getClass().getName().startsWith("java.util.ImmutableCollections$");
    }

    public static final class Element {
    }

    public static final class ChainEnd {
    }

    public static final class Chain {
        public Chain(ChainEnd end) {
            Objects.requireNonNull(end);
        }
    }

    public static final class NeedsChain {
        public final Chain chain;

        public NeedsChain(Chain chain) {
            this.chain = chain;
        }
    }

    public static final class NoDefault {
        static int attempts;

        public NoDefault() {
            attempts++;
            throw new IllegalStateException("no default value");
        }
    }

    public static final class NeedsNoDefault {
        public final NoDefault value;

        public NeedsNoDefault(NoDefault value) {
            this.value = value;
        }
    }

    public static final class Holder {
        public final Runnable missing;
        public final List<String> list;
        public final Integer number;
        public final Element element;

        public Holder(Runnable missing, List<String> list, int number, Element element) {
            this.missing = missing;
            this.list = list;
            this.number = number;
            this.element = element;
        }
    }

    public static final class FromFactory {
        public final Runnable missing;
        public final List<String> list;

        private FromFactory(boolean created, Runnable missing, List<String> list) {
            if (!created) {
                throw new IllegalArgumentException();
            }
            this.missing = missing;
            this.list = list;
        }

        public static FromFactory create(Runnable missing, List<String> list) {
            return new FromFactory(true, missing, list);
        }
    }

    public static final class FailingFirst {
        static int failures;
        public final Object value;

        public FailingFirst(Runnable value) {
            failures++;
            throw new IllegalStateException("no runnable");
        }

        public FailingFirst(Element value) {
            this.value = value;
        }
    }

    public static final class FailingLast {
        static int failures;
        public final Object value;

        public FailingLast(Element value) {
            this.value = value;
        }

        public FailingLast(Runnable value) {
            failures++;
            throw new IllegalStateException("no runnable");
        }
    }
}
