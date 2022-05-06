package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etrayed
 */
public class BlockEntityInfoTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();

        TileEntityTypes.class.getName();
    }

    @Test
    public void test() {
        BlockEntityInfo info = new BlockEntityInfo(1, 8, 7, new MinecraftKey("minecraft", "sign"), null);

        Object generic = BlockEntityInfo.getConverter().getGeneric(info);
        BlockEntityInfo back = BlockEntityInfo.getConverter().getSpecific(generic);

        assertEquals(info, back);
    }
}
