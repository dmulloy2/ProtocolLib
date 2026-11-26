package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData.BlockEntityInfo;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData.ChunkData;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData.LightData;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import org.bukkit.craftbukkit.CraftRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedLevelChunkDataTest {
    @BeforeAll
    public static void initializeBukkitAndNMS() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testBlockEntityInfo() {
        var tag = NbtFactory.ofCompound("");
        tag.put("marker", "preserved");
        var info = BlockEntityInfo.fromValues(15, 14, -64, new MinecraftKey("chest"), tag);
        assertEquals(15, info.getSectionX());
        assertEquals(14, info.getSectionZ());
        assertEquals(-64, info.getY());
        assertEquals(new MinecraftKey("chest"), info.getTypeKey());
        assertEquals("preserved", info.getAdditionalData().getString("marker"));

        info.setSectionX(8);
        assertEquals(8, info.getSectionX());
        assertEquals(14, info.getSectionZ());
        info.setSectionZ(1);
        info.setY(319);
        info.setTypeKey(new MinecraftKey("bell"));
        assertEquals(8, info.getSectionX());
        assertEquals(1, info.getSectionZ());
        assertEquals(319, info.getY());
        assertEquals(new MinecraftKey("bell"), info.getTypeKey());
        assertEquals("preserved", info.getAdditionalData().getString("marker"));
        info.setAdditionalData(null);
        assertNull(info.getAdditionalData());
        info.setAdditionalData(tag);
        assertEquals(tag, info.getAdditionalData());
        assertNull(BlockEntityInfo.fromValues(0, 0, 0, new MinecraftKey("chest")).getAdditionalData());
    }

    @Test
    public void testChunkAndLightDataConverters() {
        PacketContainer packet = createChunkPacket();
        ChunkData chunk = createChunkData();
        LightData light = createLightData();
        packet.getLevelChunkData().write(0, chunk);
        packet.getLightUpdateData().write(0, light);
        assertEquals(chunk, packet.getLevelChunkData().read(0));
        assertEquals(light, packet.getLightUpdateData().read(0));
        assertSame(chunk.getHandle(), BukkitConverters.getWrappedChunkDataConverter().getGeneric(chunk));
        assertSame(light.getHandle(), BukkitConverters.getWrappedLightDataConverter().getGeneric(light));
    }

    @Test
    public void testChunkAndLightDataCodec() {
        PacketContainer packet = createChunkPacket();
        ChunkData chunk = packet.getLevelChunkData().read(0);
        LightData light = packet.getLightUpdateData().read(0);
        var raw = (ClientboundLevelChunkWithLightPacket) packet.getHandle();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
        try {
            ClientboundLevelChunkWithLightPacket.STREAM_CODEC.encode(buffer, raw);
            var decoded = ClientboundLevelChunkWithLightPacket.STREAM_CODEC.decode(buffer);
            assertEquals(12, decoded.x());
            assertEquals(-9, decoded.z());
            ChunkData decodedChunk = new ChunkData(decoded.chunkData());
            assertArrayEquals(new byte[]{1, 2, 3}, decodedChunk.getBuffer());
            assertArrayEquals(new long[]{4, 5}, decodedChunk.getHeightmaps().get(EnumWrappers.HeightmapType.WORLD_SURFACE));
            BlockEntityInfo blockEntity = chunk.getBlockEntityInfo().get(0);
            BlockEntityInfo decodedEntity = decodedChunk.getBlockEntityInfo().get(0);
            assertEquals(blockEntity, decodedEntity);
            assertEquals(blockEntity.hashCode(), decodedEntity.hashCode());
            assertNull(decodedEntity.getAdditionalData());
            LightData decodedLight = new LightData(decoded.lightData());
            assertEquals(light.getSkyYMask(), decodedLight.getSkyYMask());
            assertEquals(light.getBlockYMask(), decodedLight.getBlockYMask());
            assertEquals(light.getEmptySkyYMask(), decodedLight.getEmptySkyYMask());
            assertEquals(light.getEmptyBlockYMask(), decodedLight.getEmptyBlockYMask());
            assertArrayEquals(light.getSkyUpdates().get(0), decodedLight.getSkyUpdates().get(0));
            assertArrayEquals(light.getBlockUpdates().get(0), decodedLight.getBlockUpdates().get(0));
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void testChunkCloneIsIndependent() {
        PacketContainer packet = createChunkPacket();
        ChunkData chunk = packet.getLevelChunkData().read(0);
        ChunkData clone = packet.deepClone().getLevelChunkData().read(0);
        assertArrayEquals(chunk.getBuffer(), clone.getBuffer());
        assertEquals(chunk.getBlockEntityInfo().get(0), clone.getBlockEntityInfo().get(0));

        // The constructor must retain a mutable list even when its input was List.of(...).
        chunk.getBlockEntityInfo().clear();
        chunk.getBuffer()[0] = 9;
        chunk.getHeightmaps().get(EnumWrappers.HeightmapType.WORLD_SURFACE)[0] = 99;
        assertTrue(chunk.getBlockEntityInfo().isEmpty());
        assertEquals(1, clone.getBlockEntityInfo().size());
        assertArrayEquals(new byte[]{1, 2, 3}, clone.getBuffer());
        assertArrayEquals(new long[]{4, 5}, clone.getHeightmaps().get(EnumWrappers.HeightmapType.WORLD_SURFACE));
    }

    @Test
    public void testChunkSettersPreservePacketCoordinates() {
        PacketContainer packet = createChunkPacket();
        ChunkData chunk = packet.getLevelChunkData().read(0);
        chunk.setBlockEntityInfo(List.of());
        chunk.setBuffer(new byte[]{9});
        chunk.setHeightmaps(Map.of());
        assertTrue(chunk.getBlockEntityInfo().isEmpty());
        assertTrue(chunk.getHeightmaps().isEmpty());
        assertArrayEquals(new byte[]{9}, chunk.getBuffer());
        var raw = (ClientboundLevelChunkWithLightPacket) packet.getHandle();
        assertEquals(12, raw.x());
        assertEquals(-9, raw.z());
    }

    @Test
    public void testLightMaskSetterPreservesOtherMasks() {
        LightData light = createLightData();
        light.setSkyYMask(BitSet.valueOf(new long[]{16}));
        assertEquals(BitSet.valueOf(new long[]{16}), ((ClientboundLightUpdatePacketData) light.getHandle()).skyYMask());
        assertEquals(BitSet.valueOf(new long[]{2}), light.getBlockYMask());
        assertEquals(BitSet.valueOf(new long[]{4}), light.getEmptySkyYMask());
        assertEquals(BitSet.valueOf(new long[]{8}), light.getEmptyBlockYMask());
    }

    private ChunkData createChunkData() {
        var blockEntity = BlockEntityInfo.fromValues(15, 8, -64, new MinecraftKey("chest"));
        return ChunkData.fromValues(Map.of(EnumWrappers.HeightmapType.WORLD_SURFACE, new long[]{4, 5}),
                new byte[]{1, 2, 3}, List.of(blockEntity));
    }

    private LightData createLightData() {
        return LightData.fromValues(BitSet.valueOf(new long[]{1}), BitSet.valueOf(new long[]{2}),
                BitSet.valueOf(new long[]{4}), BitSet.valueOf(new long[]{8}),
                List.of(new byte[2048]), List.of(new byte[2048]));
    }

    private PacketContainer createChunkPacket() {
        return PacketContainer.fromPacket(new ClientboundLevelChunkWithLightPacket(12, -9,
                (ClientboundLevelChunkPacketData) createChunkData().getHandle(),
                (ClientboundLightUpdatePacketData) createLightData().getHandle()));
    }
}
