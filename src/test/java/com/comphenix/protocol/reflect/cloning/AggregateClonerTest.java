package com.comphenix.protocol.reflect.cloning;

import java.util.Arrays;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AggregateClonerTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testArrays() {
        List<Integer> input = Arrays.asList(1, 2, 3);
        assertEquals(input, AggregateCloner.DEFAULT.clone(input));
    }

    // @Test
    // Usages of NonNullList were removed in 1.17.1
    /* public void testNonNullList() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);

        NonNullList<ItemStack> list = NonNullList.a(16, ItemStack.b);
        packet.getModifier().write(1, list);

        PacketContainer cloned = packet.deepClone();

        @SuppressWarnings("unchecked")
        NonNullList<ItemStack> list1 = (NonNullList<ItemStack>) cloned.getModifier().read(1);

        assertEquals(list.size(), list1.size());
        Assertions.assertArrayEquals(list.toArray(), list1.toArray());
    } */
}
