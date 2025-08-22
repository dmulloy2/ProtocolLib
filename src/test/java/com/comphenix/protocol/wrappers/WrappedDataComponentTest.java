package com.comphenix.protocol.wrappers;

import java.util.Map;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedDataComponentTest {

    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testShallowClone() {
        WrappedDataComponentHolder holder = new WrappedDataComponentHolder(new ItemStack(Items.DIAMOND_AXE));
        Map<String, WrappedDataComponent> components = holder.getAllComponents();
        assertFalse(components.isEmpty(), "Components should not be empty");

        for (WrappedDataComponent component : components.values()) {
            WrappedDataComponent clone = component.shallowClone();
            assertNotSame(component, clone, "Clone should not be the same instance");
            assertEquals(component, clone, "Clone should be equal to the original component");
            assertTrue(component.getValue().equals(clone.getValue()), "Clone value should match original");
        }
    }

    @Test
    public void testDeepCloneWithPrimitive() {
        WrappedDataComponent component = WrappedDataComponent.create("minecraft:damage", 10);
        WrappedDataComponent clone = component.deepClone();
        assertNotSame(component, clone, "Clone should not be the same instance");
        assertEquals(component, clone, "Clone should be equal to the original component");
        assertEquals(10, (int)clone.getValue(), "Clone value should match original");
    }

    @Test
    public void testDeepCloneWithEnum() {
        WrappedDataComponent component = WrappedDataComponent.create("minecraft:item_model", Items.DIAMOND_SHOVEL);
        WrappedDataComponent clone = component.deepClone();
        assertNotSame(component, clone, "Clone should not be the same instance");
        assertEquals(component, clone, "Clone should be equal to the original component");
        assertEquals(Items.DIAMOND_SHOVEL, clone.getValue(), "Clone value should match original");
    }
}
