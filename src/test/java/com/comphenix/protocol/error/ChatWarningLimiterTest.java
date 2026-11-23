package com.comphenix.protocol.error;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatWarningLimiterTest {

    private static final long WINDOW = TimeUnit.SECONDS.toNanos(10);

    @BeforeEach
    @AfterEach
    public void reset() {
        ChatWarningLimiter.reset();
    }

    @Test
    public void testFiveMessagesPerWindow() {
        assertWindow(1_000);
    }

    @Test
    public void testFirstWindowOpensForNegativeTimes() {
        // System.nanoTime() has an arbitrary origin and may be negative
        assertWindow(-TimeUnit.DAYS.toNanos(365));
    }

    @Test
    public void testWindowEndsWhenTheTimeWrapsAround() {
        assertWindow(Long.MAX_VALUE - WINDOW / 2);
    }

    private static void assertWindow(long start) {
        for (int i = 0; i < 5; i++) {
            assertTrue(ChatWarningLimiter.tryAcquire(start + i), "message " + i);
        }
        assertFalse(ChatWarningLimiter.tryAcquire(start + 5));
        assertFalse(ChatWarningLimiter.tryAcquire(start + WINDOW));

        // the next window
        long next = start + WINDOW + 1;
        for (int i = 0; i < 5; i++) {
            assertTrue(ChatWarningLimiter.tryAcquire(next + i), "message " + i + " of the next window");
        }
        assertFalse(ChatWarningLimiter.tryAcquire(next + 5));
    }
}
