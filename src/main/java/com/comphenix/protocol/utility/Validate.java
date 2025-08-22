package com.comphenix.protocol.utility;

public final class Validate {
    private Validate() {}

    public static void notNull(Object obj, String err) {
        if (obj == null) {
            throw new IllegalArgumentException(err);
        }
    }

    public static void isTrue(boolean bool, String err) {
        if (!bool) {
            throw new IllegalArgumentException(err);
        }
    }
}
