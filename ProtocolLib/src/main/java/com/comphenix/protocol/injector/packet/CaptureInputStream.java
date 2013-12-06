package com.comphenix.protocol.injector.packet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents an input stream that stores every read block of bytes in another output stream.
 * 
 * @author Kristian
 */
class CaptureInputStream extends FilterInputStream {
	protected OutputStream out;
	
	public CaptureInputStream(InputStream in, OutputStream out) {
		super(in);
		this.out = out;
	}
	
	@Override
	public int read() throws IOException {
		int value = super.read();
		
		// Write the byte
		if (value >= 0)
			out.write(value);
		return value;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		out.close();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int count = super.read(b);
		
		if (count > 0 ) {
			out.write(b, 0, count);
		}
		return count;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = super.read(b, off, len);
		
		if (count > 0 ) {
			out.write(b, off, count);
		}
		return count;
	}
	
	/**
	 * Retrieve the output stream that receives all the read information.
	 * @return The output stream everything is copied to.
	 */
	public OutputStream getOutputStream() {
		return out;
	}
}
