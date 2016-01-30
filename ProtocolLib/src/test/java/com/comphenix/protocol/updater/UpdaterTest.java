/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.updater;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Test;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.updater.Updater.UpdateType;

/**
 * @author dmulloy2
 */

public class UpdaterTest {
	private static final int BUKKIT_DEV_ID = 45564;

	@Test
	public void testSpigotUpdater() {
		SpigotUpdater updater = new SpigotUpdater(null, UpdateType.NO_DOWNLOAD, true);

		String remote = null;

		try {
			remote = updater.getSpigotVersion();
		} catch (Throwable ex) {
			ex.printStackTrace();
			fail("Failed to check for updates");
		}

		System.out.println("Determined remote Spigot version: " + remote);
	}

	@Test
	public void testBukkitUpdater() {
		Server server = mock(Server.class);
		when(server.getUpdateFolder()).thenReturn(null);

		Plugin plugin = mock(Plugin.class);
		when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("ProtocolLib", ProtocolLibrary.class.getPackage().getImplementationVersion(), null));
		when(plugin.getLogger()).thenReturn(Logger.getLogger("ProtocolLib"));
		when(plugin.getDataFolder()).thenReturn(null);
		when(plugin.getServer()).thenReturn(server);

		BukkitUpdater updater = new BukkitUpdater(plugin, BUKKIT_DEV_ID, null, UpdateType.NO_DOWNLOAD, true);
		if (! updater.read()) {
			fail("Failed to check for updates");
		}

		String remote = updater.getLatestName();
		System.out.println("Determined remote Bukkit Dev version: " + remote);
	}
}