package com.comphenix.protocol.utility;

import com.comphenix.protocol.BukkitInitialization;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MinecraftReflectionTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @AfterAll
    public static void undoMocking() {
        // NOP
        MinecraftReflection.minecraftPackage = null;
    }

    @Test
    public void testBukkitMethod() {
        FakeEntity entity = mock(FakeEntity.class);
        FakeBlock block = mock(FakeBlock.class);

        MinecraftReflection.getBukkitEntity(entity);
        MinecraftReflection.getBukkitEntity(block);

        verify(entity, times(1)).getBukkitEntity();
        verify(block, times(1)).getBukkitEntity();
    }

    @Test
    public void testIllegalClass() {
        assertThrows(IllegalArgumentException.class, () -> MinecraftReflection.getBukkitEntity("Hello"));
    }

    @Test
    public void testNullable() {
        assertNull(MinecraftReflection.getNullableNMS("ProtocolLib"));
    }

    @Test
    public void testAttributeSnapshot() {
        assertEquals(PacketPlayOutUpdateAttributes.AttributeSnapshot.class,
                MinecraftReflection.getAttributeSnapshotClass());
    }

    @Test
    public void testChatComponent() {
        assertEquals(IChatBaseComponent.class, MinecraftReflection.getIChatBaseComponentClass());
    }

    @Test
    public void testChatSerializer() {
        assertEquals(IChatBaseComponent.ChatSerializer.class, MinecraftReflection.getChatSerializerClass());
    }

    @Test
    public void testChunkCoordIntPair() {
        assertEquals(ChunkCoordIntPair.class, MinecraftReflection.getChunkCoordIntPair());
    }

    @Test
    public void testIBlockData() {
        assertEquals(IBlockData.class, MinecraftReflection.getIBlockDataClass());
    }

    @Test
    public void testPlayerConnection() {
        assertEquals(PlayerConnection.class, MinecraftReflection.getPlayerConnectionClass());
    }

    @Test
    public void testServerPing() {
        assertEquals(ServerPing.class, MinecraftReflection.getServerPingClass());
    }

    @Test
    public void testServerPingPlayerSample() {
        assertEquals(ServerPing.ServerPingPlayerSample.class, MinecraftReflection.getServerPingPlayerSampleClass());
    }

    @Test
    public void testServerPingServerData() {
        assertEquals(ServerPing.ServerData.class, MinecraftReflection.getServerPingServerDataClass());
    }

    @Test
    public void testNbtStreamTools() {
        assertEquals(NBTCompressedStreamTools.class, MinecraftReflection.getNbtCompressedStreamToolsClass());
    }

    @Test
    public void testDataWatcherItem() {
        assertEquals(DataWatcher.Item.class, MinecraftReflection.getDataWatcherItemClass());
    }

    @Test
    public void testLoginSignature() {
        assertEquals(MinecraftEncryption.b.class, MinecraftReflection.getSaltedSignatureClass());
    }

    @Test
    public void testItemStacks() {
        ItemStack stack = new ItemStack(Material.GOLDEN_SWORD);
        Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
        assertItemsEqual(stack, MinecraftReflection.getBukkitItemStack(nmsStack));

        // The NMS handle for CraftItemStack is null with Material.AIR, make sure it is handled correctly
        assertNotNull(
                MinecraftReflection.getMinecraftItemStack(CraftItemStack.asCraftCopy(new ItemStack(Material.AIR))));
    }

    @Test
    public void testGameProfile() {
        assertEquals(GameProfile.class, MinecraftReflection.getGameProfileClass());
    }

    @Test
    public void testEnumEntityUseAction() {
        // this class is package-private in PacketPlayInUseEntity, so we can only check if no exception is thrown during retrieval
        MinecraftReflection.getEnumEntityUseActionClass();
    }

    // Mocking objects
    private interface FakeEntity {

        Entity getBukkitEntity();
    }

    private interface FakeBlock {

        Block getBukkitEntity();
    }
}
