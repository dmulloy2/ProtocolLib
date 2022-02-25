/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.updater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.comphenix.protocol.updater.Updater.UpdateType;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author dmulloy2
 */
public class UpdaterTest {

	private static final int BUKKIT_DEV_ID = 45564;
	private static Plugin plugin;

	@BeforeAll
	public static void preparePlugin() {
		Server server = mock(Server.class);
		when(server.getUpdateFolder()).thenReturn(null);

		plugin = mock(Plugin.class);
		String version = System.getProperty("projectVersion");
		if (version == null) {
			version = "4.4.0";
		}
		when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("ProtocolLib", version, null));
		when(plugin.getLogger()).thenReturn(Logger.getLogger("ProtocolLib"));
		when(plugin.getDataFolder()).thenReturn(null);
		when(plugin.getServer()).thenReturn(server);
	}

	@Test
	public void testUpdaterType() {
		assertEquals(Updater.create(plugin, BUKKIT_DEV_ID, null, UpdateType.NO_DOWNLOAD, true).getClass(),
				SpigotUpdater.class);
	}

	// @Test
	public void testSpigotUpdater() {
		SpigotUpdater updater = new SpigotUpdater(plugin, UpdateType.NO_DOWNLOAD, true);

		String remote = null;

		try {
			remote = updater.getSpigotVersion();
		} catch (Throwable ex) {
			fail("Failed to check for updates", ex);
		}

		System.out.println("Determined remote Spigot version: " + remote);
		System.out.println("Update available: " + updater.versionCheck(remote));
	}
}
