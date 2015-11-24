package com.comphenix.integration.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.utility.StreamSerializer;
import com.google.common.io.ByteStreams;

public class SimpleMinecraftClient {
    private static final int CONNECT_TIMEOUT = 2500;
    private static final int READ_TIMEOUT = 15000;

    // Current Minecraft version
    private final int protocolVersion;

    // Typical Minecraft serializer
    private static StreamSerializer serializer = StreamSerializer.getDefault();
    
    public SimpleMinecraftClient(int protocolVersion) {
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
        
        try {
            socket = new Socket();
            socket.connect(address, CONNECT_TIMEOUT);

            // Shouldn't take that long
            socket.setSoTimeout(READ_TIMEOUT);
            
            // Retrieve sockets
            output = socket.getOutputStream();
            input = socket.getInputStream();

            // The output writer
            DataOutputStream data = new DataOutputStream(output);

            // Request a server information packet
            writePacket(data, new HandshakePacket(protocolVersion, address.getHostName(), address.getPort(), 1));
            writePacket(data, new RequestPacket());
            data.flush();
            
            // Read a single packet, and close the connection
            SimplePacket packet = readPacket(new DataInputStream(input), Protocol.STATUS);
          
            socket.close();
            return ((ResponsePacket) packet).getPingJson();
        } finally {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            if (socket != null)
                socket.close();
        }
	}
	
	private void writePacket(DataOutputStream output, SimplePacket packet) throws IOException {
		ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
		DataOutputStream packetOutput = new DataOutputStream(packetBuffer);
		
		// Prefix the packet with a length field
		packet.write(packetOutput);
		writeByteArray(output, packetBuffer.toByteArray());
	}

	private SimplePacket readPacket(DataInputStream input, Protocol protocol) throws IOException {
		while (true) {
			byte[] buffer = readByteArray(input);
			
			// Skip empty packets
			if (buffer.length == 0)
				continue;
			
			DataInputStream data = getDataInput(buffer);
			PacketType type = PacketType.findCurrent(protocol, Sender.SERVER, serializer.deserializeVarInt(data));
			
			if (type == PacketType.Status.Server.OUT_SERVER_INFO) {
				ResponsePacket response = new ResponsePacket();
				response.read(type, data);
				return response;
			} else {
				throw new IllegalArgumentException("Unsuppported and unexpected type: " + type);
			}
		}
	}

	/**
	 * Wrap an input stream around a byte array.
	 * @param bytes - the array.
	 * @return The wrapped input stream.
	 */
	private DataInputStream getDataInput(byte[] bytes) {
		return new DataInputStream(new ByteArrayInputStream(bytes));
	}
	
	/**
	 * Write a byte array to the output stream, prefixed by a length.
	 * @param output - the stream.
	 * @param data - the data to write.
	 */
	private static void writeByteArray(DataOutputStream output, byte[] data) throws IOException {
		StreamSerializer.getDefault().serializeVarInt(output, data.length);
		
		if (data.length > 0) {
			output.write(data);
		}
	}

	/**
	 * Read a byte array from an input stream, prefixed by length.
	 * @param input - the input stream.
	 * @return The read byte array.
	 */
	private static byte[] readByteArray(DataInputStream input) throws IOException {
		int length = serializer.deserializeVarInt(input);
		byte[] data = new byte[length];

		ByteStreams.readFully(input, data);
		return data;
	}

	private static class RequestPacket extends SimplePacket {
		public RequestPacket() {
			super(PacketType.Status.Client.IN_START);
		}
	}
	
	private static class ResponsePacket extends SimplePacket {
		private String ping;
		
		public ResponsePacket() {
			super(PacketType.Status.Server.OUT_SERVER_INFO);
		}
		
		@Override
		public void read(PacketType type, DataInputStream input) throws IOException {
			super.read(type, input);
			ping = serializer.deserializeString(input, 32000);
		}
		
		public String getPingJson() {
			return ping;
		}
	}
	
	private static class HandshakePacket extends SimplePacket {	
		private int protocol;
		private String host;
		private int port;
		private int nextState;
		
		public HandshakePacket(int protocol, String host, int port, int nextState) {
			super(PacketType.Handshake.Client.SET_PROTOCOL);
			this.protocol = protocol;
			this.host = host;
			this.port = port;
			this.nextState = nextState;
		}

		@Override
		public void write(DataOutputStream output) throws IOException {
			super.write(output);
			serializer.serializeVarInt(output, protocol);
			serializer.serializeString(output, host);
			output.writeShort(port);
			serializer.serializeVarInt(output, nextState);
		}
	}
	
	private static class SimplePacket {
		protected final PacketType type;
		protected final StreamSerializer serializer = StreamSerializer.getDefault();
		
		public SimplePacket(PacketType type) {
			this.type = type;
		}
		
		public void write(DataOutputStream output) throws IOException {
			serializer.serializeVarInt(output, type.getCurrentId());
		}
		
		@SuppressWarnings("unused")
		public void read(PacketType type, DataInputStream input) throws IOException {
			// Note - we don't read the packet id
			if (this.type != type) {
				throw new IllegalArgumentException("Unexpected type: " + type);
			}
		}
	}
}