package com.comphenix.protocol.collections;

import com.comphenix.protocol.utility.IntegerMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegerMapTest {
    @Test
    public void testNextPower() {
        assertEquals(128, IntegerMath.nextPowerOfTwo(127));
        assertEquals(128, IntegerMath.nextPowerOfTwo(128));
        assertEquals(256, IntegerMath.nextPowerOfTwo(129));
    }
    @Test
    public void testCapacityIncrement() {
        IntegerMap<Boolean> map = new IntegerMap<>();
        for (int i = 0; i < 512; i++) {
            map.put(i, false);
        }
        assertEquals(map.size(), 512);
    }
}