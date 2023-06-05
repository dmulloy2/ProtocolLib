package com.comphenix.protocol.utility;

public final class IntegerMath
{
    // The largest positive int in java is 2^31 - 1, so 2^30 is the largest positive int that is a power of 2.
    public static final int MAX_SIGNED_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    private IntegerMath() {}

    /**
     * This calculates the smallest y for which 2^y > x
     *
     * @param x the number that the next power of 2 should be calculated for.
     *
     * @return If the next power of two would be larger than {@link #MAX_SIGNED_POWER_OF_TWO}, this method returns {@link Integer#MAX_VALUE}
     *
     * @see com.google.common.math.IntMath for a similar version, that is not yet implemented in the guava of Minecraft 1.8.
     */
    public static int nextPowerOfTwo(int x)
    {
        if (x > MAX_SIGNED_POWER_OF_TWO) return Integer.MAX_VALUE;
        return 1 << -Integer.numberOfLeadingZeros(x - 1);
    }
}
