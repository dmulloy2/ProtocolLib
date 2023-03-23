package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.BlockAccessAir;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.entity.TileEntityBell;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.lighting.LightEngine;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Etrayed
 */
@Disabled // todo: figure this registry update out
public class WrappedLevelChunkDataTest {

    @BeforeAll
    public static void initializeBukkitAndNMS() {
        BukkitInitialization.initializeAll();

        ILightAccess access = mock(ILightAccess.class);

        when(access.c(0, 0)).thenReturn(BlockAccessAir.a);
        when(access.q()).thenReturn(BlockAccessAir.a);

        LightEngine engine = new LightEngine(access, true, true);
        WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        //when(nmsWorld.s()).thenReturn();
        // TODO: somehow find a way to always call the real code for all LevelHeightAccessor implementations
        when(nmsWorld.v_()).thenReturn(256);
        when(nmsWorld.ai()).thenReturn(16); // LevelHeightAccessor is mocked and therefore always returns 0, there are further methods like this which might cause errors in the future

        when(nmsWorld.l_()).thenReturn(engine);
    }

    private final WorldServer nmsWorld;

    private final Chunk chunk;

    public WrappedLevelChunkDataTest() {
        this.nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        this.chunk = new Chunk(nmsWorld, new ChunkCoordIntPair(5, 5));

        IBlockData bellData = BuiltInRegistries.f.a(new MinecraftKey("bell")).o();

        chunk.b(0).a(0, 0, 0, bellData);
        chunk.a(new TileEntityBell(BlockPosition.b, bellData));
    }

    @Test
    public void testChunkData() {
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, nmsWorld.l_(), null, null, false);
        PacketContainer container = PacketContainer.fromPacket(packet);
        Object rawInstance = container.getSpecificModifier(MinecraftReflection.getLevelChunkPacketDataClass()).read(0);
        Object virtualInstance = BukkitConverters.getWrappedChunkDataConverter().getGeneric(container.getLevelChunkData().read(0));

        assertTrue(new ReflectionEquals(rawInstance, FuzzyReflection.fromClass(rawInstance.getClass(), true)
                .getFieldListByType(List.class).get(0).getName())
                .matches(virtualInstance));
        assertTrue(blockEntitiesEqual(rawInstance, virtualInstance));
    }

    private boolean blockEntitiesEqual(Object raw, Object virtual) {
        if (raw == null && virtual == null) {
            return true;
        }

        if (raw == null || virtual == null) {
            return false;
        }

        FieldAccessor accessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(raw.getClass(), true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(List.class).build()));
        List rawList = (List) accessor.get(raw);
        List virtualList = (List) accessor.get(virtual);

        if (rawList.size() != virtualList.size()) {
            return false;
        }

        for (int i = 0; i < rawList.size(); i++) {
            if (!EqualsBuilder.reflectionEquals(rawList.get(0), virtualList.get(0))) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void testLightData() {
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, nmsWorld.l_(), null, null, false);
        PacketContainer container = PacketContainer.fromPacket(packet);

        randomizeBitSets(container.getSpecificModifier(MinecraftReflection.getLightUpdatePacketDataClass()).read(0));

        assertTrue(new ReflectionEquals(container.getSpecificModifier(MinecraftReflection.getLightUpdatePacketDataClass()).read(0))
                .matches(BukkitConverters.getWrappedLightDataConverter().getGeneric(container.getLightUpdateData().read(0))));
    }

    private void randomizeBitSets(Object lightData) {
        for (Field field : FuzzyReflection.fromClass(MinecraftReflection.getLightUpdatePacketDataClass(), true).getFieldListByType(BitSet.class)) {
            try {
                field.setAccessible(true);

                randomizeBitSet((BitSet) field.get(lightData));
            } catch (IllegalAccessException ignored) {}
        }
    }

    private void randomizeBitSet(BitSet bitSet) {
        for (int i = 0; i < bitSet.size(); i++) {
            if (Math.random() >= 0.5D) {
                bitSet.set(i);
            }
        }
    }
}
