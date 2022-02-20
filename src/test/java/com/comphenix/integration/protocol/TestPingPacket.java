package com.comphenix.integration.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.Plugin;

public class TestPingPacket {

	// Current versions
	private static final int PROTOCOL_VERSION = 4;

	// Timeout
	private static final int TIMEOUT_PING_MS = 10000;

	private volatile String source;

	private TestPingPacket() {
		// Prevent external constructors
	}

	/**
	 * Create a new test ping packet test.
	 *
	 * @return The new test.
	 */
	public static TestPingPacket newTest() {
		return new TestPingPacket();
	}

	/**
	 * Invoked when the test should be started.
	 *
	 * @param plugin - the current plugin.
	 * @throws Throwable Anything went wrong.
	 */
	public void startTest(Plugin plugin) throws Throwable {
		try {
			String transmitted = this.testInterception(plugin).
					get(TIMEOUT_PING_MS, TimeUnit.MILLISECONDS);

			// Make sure it's the same
			System.out.println("Server string: " + transmitted);
			assertEquals(this.source, transmitted);
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	private Future<String> testInterception(Plugin test) {
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(test, PacketType.Status.Server.SERVER_INFO) {
					@Override
					public void onPacketSending(PacketEvent event) {
						TestPingPacket.this.source = event.getPacket().getServerPings().read(0).toJson();
					}
				});

		// Invoke the client on a separate thread
		return Executors.newSingleThreadExecutor().submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				SimpleMinecraftClient client = new SimpleMinecraftClient(PROTOCOL_VERSION);
				String information = client.queryLocalPing();

				// Wait for the listener to catch up
				for (int i = 0; i < 1000 && (TestPingPacket.this.source == null); i++) {
					Thread.sleep(1);
				}

				return information;
			}
		});
	}
}
