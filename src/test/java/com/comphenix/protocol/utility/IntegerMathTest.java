package com.comphenix.protocol.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegerMathTest
{
    @Test
    void testNextPowerOfTwo() {
        // Test with 0, expected 1 as 2^0 is 1 which is > 0
        assertEquals(1, IntegerMath.nextPowerOfTwo(0));

        // Test with a power of two, expected the same number as the input is already a power of two
        assertEquals(2, IntegerMath.nextPowerOfTwo(2));
        assertEquals(16, IntegerMath.nextPowerOfTwo(16));
        assertEquals(IntegerMath.MAX_SIGNED_POWER_OF_TWO, IntegerMath.nextPowerOfTwo(IntegerMath.MAX_SIGNED_POWER_OF_TWO));

        // Test with a number that is not a power of two, expected the next higher power of two
        assertEquals(8, IntegerMath.nextPowerOfTwo(7));
        assertEquals(1024, IntegerMath.nextPowerOfTwo(1000));

        assertEquals(Integer.MAX_VALUE, IntegerMath.nextPowerOfTwo(Integer.MAX_VALUE - 1));
        assertEquals(Integer.MAX_VALUE, IntegerMath.nextPowerOfTwo(Integer.MAX_VALUE));
    }
}
