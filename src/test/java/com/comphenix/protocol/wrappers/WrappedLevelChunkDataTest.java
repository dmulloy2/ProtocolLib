package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.io.BaseEncoding;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etrayed
 */
public class WrappedLevelChunkDataTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    private final Random random = new Random();

    private WrappedLevelChunkData.BlockEntityInfo sampleInfo;

    public WrappedLevelChunkDataTest() {
        this.sampleInfo = new WrappedLevelChunkData.BlockEntityInfo(1, 8, 7, new MinecraftKey("minecraft", "sign"), null);
    }

    @Test
    public void testChunkData() {
        NbtCompound compound = NbtFactory.fromNMSCompound(new NBTTagCompound());

        compound.put("test", 69);

        WrappedLevelChunkData.ChunkData chunkData = new WrappedLevelChunkData.ChunkData(compound,
                BaseEncoding.base64().decode("ML3C7mBk8kMpOoj461P95A"), Collections.singletonList(sampleInfo));
        Object generic = WrappedLevelChunkData.ChunkData.getConverter().getGeneric(chunkData);
        WrappedLevelChunkData.ChunkData back = WrappedLevelChunkData.ChunkData.getConverter().getSpecific(generic);

        assertEquals(chunkData, back);
    }

    @Test
    public void testLightData() {
        WrappedLevelChunkData.LightData lightData = new WrappedLevelChunkData.LightData(randomBitSet(), randomBitSet(),
                randomBitSet(), randomBitSet(), randomByteArrayList(), randomByteArrayList(), true);
        Object generic = WrappedLevelChunkData.LightData.getConverter().getGeneric(lightData);
        WrappedLevelChunkData.LightData back = WrappedLevelChunkData.LightData.getConverter().getSpecific(generic);

        assertEquals(lightData, back);
    }

    private BitSet randomBitSet() {
        return BitSet.valueOf(random.longs().limit(30).toArray());
    }

    private List<byte[]> randomByteArrayList() {
        byte[] bytes = new byte[random.nextInt(50)];

        random.nextBytes(bytes);

        return Collections.singletonList(bytes);
    }

    @Test
    public void testBlockEntityInfo() {
        TileEntityTypes.class.getName();

        Object generic = WrappedLevelChunkData.BlockEntityInfo.getConverter().getGeneric(sampleInfo);
        WrappedLevelChunkData.BlockEntityInfo back = WrappedLevelChunkData.BlockEntityInfo.getConverter().getSpecific(generic);

        assertEquals(sampleInfo, back);
    }
}
