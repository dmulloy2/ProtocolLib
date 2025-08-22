package com.comphenix.protocol.wrappers;

import java.util.Map;

import com.comphenix.protocol.BukkitInitialization;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class WrappedDataComponentHolderTest {

    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testFromItemStack() {
        WrappedDataComponentHolder holder = WrappedDataComponentHolder.fromItemStack(new org.bukkit.inventory.ItemStack(Material.DIAMOND_SWORD));
        Map<String, WrappedDataComponent> components = holder.getAllComponents();
        assertFalse(components.isEmpty(), "Components should not be empty");
    }
}
