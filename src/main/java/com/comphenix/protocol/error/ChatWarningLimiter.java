package com.comphenix.protocol.error;

/**
 * Rate limits the warnings and errors ProtocolLib broadcasts to players with the {@code protocol.info} permission.
 * <p>
 * Shared by every broadcast path, so a burst of errors cannot flood the chat. The console still receives everything.
 * This class is internal to ProtocolLib.
 */
public final class ChatWarningLimiter {

    private static final long WINDOW_MILLIS = 10_000;
    private static final int MAX_MESSAGES_PER_WINDOW = 5;

    private static long windowStart;
    private static int messagesInWindow;

    private ChatWarningLimiter() {
    }

    /**
     * Reserve a chat message in the current window.
     *
     * @return TRUE if the message may be broadcast, FALSE if the limit of the current window has been reached.
     */
    public static synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - windowStart > WINDOW_MILLIS) {
            windowStart = now;
            messagesInWindow = 0;
        }
        return ++messagesInWindow <= MAX_MESSAGES_PER_WINDOW;
    }
}
