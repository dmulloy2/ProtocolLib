package com.comphenix.protocol.utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Represents an input stream that delegates to a byte buffer.
 * @author Kristian
 */
public class ByteBufferInputStream extends InputStream {
    private ByteBuffer buf;

    public ByteBufferInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len)
            throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}
