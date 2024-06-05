package com.comphenix.protocol.utility;

import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StreamSerializerTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testStrings() throws IOException {
        StreamSerializer serializer = new StreamSerializer();
        String initial = "Hello - this is a test.";

        // Buffer
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        serializer.serializeString(new DataOutputStream(buffer), initial);

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        String deserialized = serializer.deserializeString(input, 50);

        assertEquals(initial, deserialized);
    }

    // For future reference, items are saved in the ChunkRegionLoader and TileEntityChest

    @Test
    public void testCompound() throws IOException {
        StreamSerializer serializer = new StreamSerializer();
        NbtCompound initial = NbtFactory.ofCompound("tag");
        initial.put("name", "Ole");
        initial.put("age", 20);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        serializer.serializeCompound(new DataOutputStream(buffer), initial);

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        NbtCompound deserialized = serializer.deserializeCompound(input);

        assertEquals(initial, deserialized);
    }

    @Test
    @Disabled // TODO -- replaced with registry friendly bytebuf
    public void testItems() throws IOException {
        StreamSerializer serializer = new StreamSerializer();
        ItemStack initial = new ItemStack(Material.STRING);

        String serialized = serializer.serializeItemStack(initial);
        ItemStack deserialized = serializer.deserializeItemStack(serialized);

        assertItemsEqual(initial, deserialized);
    }

    @Test
    @Disabled // TODO -- replaced with registry friendly bytebuf
    public void testItemMeta() throws IOException {
        StreamSerializer serializer = new StreamSerializer();
        ItemStack initial = new ItemStack(Material.BLUE_WOOL, 2);

        ItemMeta meta = initial.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Blue Wool");
        initial.setItemMeta(meta);

        String serialized = serializer.serializeItemStack(initial);
        ItemStack deserialized = serializer.deserializeItemStack(serialized);

        assertItemsEqual(initial, deserialized);
    }
}
