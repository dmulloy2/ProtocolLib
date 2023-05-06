package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Lukas Alt
 * @since 06.05.2023
 */
class WrappedFilterMaskTest {
    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testFilterMask() {
        WrappedFilterMask filterMask = WrappedFilterMask.newInstance(new BitSet(20), WrappedFilterMask.Type.PARTIALLY_FILTERED);
        Object generic = WrappedFilterMask.getConverter().getGeneric(filterMask);
        WrappedFilterMask specific = WrappedFilterMask.getConverter().getSpecific(generic);
        assertEquals(filterMask, specific);
    }

}