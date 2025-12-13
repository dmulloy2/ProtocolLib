package com.comphenix.protocol.utility;

import com.comphenix.protocol.BukkitInitialization;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertEquals(ClientboundUpdateAttributesPacket.AttributeSnapshot.class,
                MinecraftReflection.getAttributeSnapshotClass());
    }

    @Test
    public void testChatComponent() {
        assertEquals(Component.class, MinecraftReflection.getIChatBaseComponentClass());
    }

    @Test
    public void testChatSerializer() {
        assertEquals(ComponentSerialization.class, MinecraftReflection.getChatSerializerClass());
    }

    @Test
    public void testChunkCoordIntPair() {
        assertEquals(ChunkPos.class, MinecraftReflection.getChunkCoordIntPair());
    }

    @Test
    public void testIBlockData() {
        assertEquals(BlockState.class, MinecraftReflection.getIBlockDataClass());
    }

    @Test
    public void testPlayerConnection() {
        assertEquals(ServerGamePacketListenerImpl.class, MinecraftReflection.getPlayerConnectionClass());
    }

    @Test
    public void testServerPing() {
        assertEquals(ServerStatus.class, MinecraftReflection.getServerPingClass());
    }

    @Test
    public void testServerPingPlayerSample() {
        assertEquals(ServerStatus.Players.class, MinecraftReflection.getServerPingPlayerSampleClass());
    }

    @Test
    public void testServerPingServerData() {
        assertEquals(ServerStatus.Version.class, MinecraftReflection.getServerPingServerDataClass());
    }

    @Test
    public void testNbtStreamTools() {
        assertEquals(NbtIo.class, MinecraftReflection.getNbtCompressedStreamToolsClass());
    }

    @Test
    public void testDataWatcherItem() {
        assertEquals(SynchedEntityData.DataItem.class, MinecraftReflection.getDataWatcherItemClass());
    }

    @Test
    public void testLoginSignature() {
        assertEquals(Crypt.SaltSignaturePair.class, MinecraftReflection.getSaltedSignatureClass());
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
