package com.comphenix.integration.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Charsets;

public class SimpleMinecraftClient {
    private static final int CONNECT_TIMEOUT = 2500;
    private static final int READ_TIMEOUT = 15000;
    
    // The version after which we must send a plugin message with the host name
    private static final String PLUGIN_MESSAGE_VERSION = "1.6.0";
    
    // Current Minecraft version
    private final MinecraftVersion version;
    private final int protocolVersion;
    
    public SimpleMinecraftClient(MinecraftVersion version, int protocolVersion) {
		this.version = version;
		this.protocolVersion = protocolVersion;
	}
    
    /**
     * Query the local server for ping information.
     * @return The server information.
     * @throws IOException 
     */
	public String queryLocalPing() throws IOException {
		return queryServerPing(new InetSocketAddress("localhost", 25565));
	}
    
	/**
	 * Query the given server for its list ping information.
	 * @param address - the server hostname and port.
	 * @return The server information.
	 * @throws IOException
	 */
	public String queryServerPing(InetSocketAddress address) throws IOException {
        Socket socket = null;
        OutputStream output = null;
        InputStream input = null;
        InputStreamReader reader = null;
        
        // UTF-16!
        Charset charset = Charsets.UTF_16BE;
        
        try {
            socket = new Socket();
            socket.connect(address, CONNECT_TIMEOUT);

            // Shouldn't take that long
            socket.setSoTimeout(READ_TIMEOUT);
            
            // Retrieve sockets
            output = socket.getOutputStream();
            input = socket.getInputStream();
            reader = new InputStreamReader(input, charset);
            
            // Get the server to send a MOTD
            output.write(new byte[] { (byte) 0xFE, (byte) 0x01 });

            // For 1.6
            if (version.compareTo(new MinecraftVersion(PLUGIN_MESSAGE_VERSION)) >= 0) {
                DataOutputStream data = new DataOutputStream(output);
            	String host = address.getHostName();

            	data.writeByte(0xFA);
            	writeString(data, "MC|PingHost");
            	data.writeShort(3 + 2 * host.length() + 4);

            	data.writeByte(protocolVersion);
            	writeString(data, host);
                data.writeInt(address.getPort());
            	data.flush();
            }
            
            int packetId = input.read();
            int length = reader.read();
            
            if (packetId != 255) 
                throw new IOException("Invalid packet ID: " + packetId);
            if (length <= 0)
                throw new IOException("Invalid string length.");
                
            char[] chars = new char[length];
             
            // Read all the characters
            if (reader.read(chars, 0, length) != length) {
                throw new IOException("Premature end of stream.");
            }

            return new String(chars);
            
        } finally {
            if (reader != null)
                reader.close();
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            if (socket != null)
                socket.close();
        }
	}
	
	private void writeString(DataOutputStream output, String text) throws IOException {
	    if (text.length() > 32767)
	        throw new IOException("String too big: " + text.length());
		output.writeShort(text.length());
		output.writeChars(text);
	}
}