package com.comphenix.protocol.error;

import java.util.concurrent.TimeUnit;

/**
 * Rate limits the warnings and errors ProtocolLib broadcasts to players with the {@code protocol.info} permission.
 * <p>
 * Shared by every broadcast path, so a burst of errors cannot flood the chat. The console still receives everything.
 * This class is internal to ProtocolLib.
 */
public final class ChatWarningLimiter {

    private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(10);
    private static final int MAX_MESSAGES_PER_WINDOW = 5;

    private static boolean windowOpen;
    private static long windowStart;
    private static int messagesInWindow;

    private ChatWarningLimiter() {
    }

    /**
     * Reserve a chat message in the current window.
     *
     * @return TRUE if the message may be broadcast, FALSE if the limit of the current window has been reached.
     */
    public static boolean tryAcquire() {
        // a monotonic clock, so that a correction of the system clock cannot shorten or extend the current window
        return tryAcquire(System.nanoTime());
    }

    /**
     * Reserve a chat message in the window that contains the given time.
     *
     * @param now - the current value of {@link System#nanoTime()}, whose origin is arbitrary and may be negative.
     * @return TRUE if the message may be broadcast, FALSE if the limit of the current window has been reached.
     */
    static synchronized boolean tryAcquire(long now) {
        // the difference is correct even if the value wraps around between the two times
        if (!windowOpen || now - windowStart > WINDOW_NANOS) {
            windowOpen = true;
            windowStart = now;
            messagesInWindow = 0;
        }
        return ++messagesInWindow <= MAX_MESSAGES_PER_WINDOW;
    }

    /**
     * Forget the current window, so that the next message opens a new one.
     */
    static synchronized void reset() {
        windowOpen = false;
        messagesInWindow = 0;
    }
}
