package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BlockPositionConverterTest {

    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testConverterRoundTrip() {
        EquivalentConverter<BlockPosition> converter = BlockPosition.getConverter();
        assertSame(converter, BlockPosition.getConverter());

        BlockPosition position = new BlockPosition(1, -64, 3);
        Object generic = converter.getGeneric(position);
        assertEquals(new BlockPos(1, -64, 3), generic);
        assertEquals(position, converter.getSpecific(generic));
    }

    @Test
    public void testMutableSubclassIsConverted() {
        // the modifier is cached per class, the mutable subclass must not reuse the one of BlockPos by accident
        EquivalentConverter<BlockPosition> converter = BlockPosition.getConverter();
        assertEquals(new BlockPosition(4, 5, 6), converter.getSpecific(new BlockPos(4, 5, 6)));
        assertEquals(new BlockPosition(7, 8, 9), converter.getSpecific(new BlockPos.MutableBlockPos(7, 8, 9)));
        assertEquals(new BlockPosition(4, 5, 6), converter.getSpecific(new BlockPos(4, 5, 6)));
        assertNull(converter.getSpecific("not a block position"));
    }
}
