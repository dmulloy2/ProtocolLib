package com.comphenix.integration.protocol;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.reflect.ExactReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleCraftBukkitITCase {

	/**
	 * The maximum amount of time to wait before a server has started.
	 * <p>
	 * We have to give it the ample time of 60 seconds, as a server may have to generate the spawn area in three worlds.
	 */
	private static final int TIMEOUT_MS = 60000;
	// The fake plugin
	private static volatile Plugin FAKE_PLUGIN = null;

	/**
	 * Setup the CraftBukkit server for all the tests.
	 *
	 * @throws IOException          Unable to setup server.
	 * @throws InterruptedException Thread interrupted.
	 */
	//@BeforeAll
	public static void setupCraftBukkit() throws Exception {
		setupPlugins();

		try {
			org.bukkit.craftbukkit.Main.main(new String[0]);
		} finally {
			System.out.println("Current class loader: " + Thread.currentThread().getContextClassLoader());
			System.out.println("Loader of SimpleLogger: " + SimpleLogger.class.getClassLoader());
			System.out.println("Loader of Logger: " + Logger.class.getClassLoader());
		}

		// We need to wait until the server object is ready
		while (Bukkit.getServer() == null) {
			Thread.sleep(1);
		}

		// Make it clear this plugin doesn't exist
		FAKE_PLUGIN = createPlugin("FakeTestPluginIntegration");

		// No need to look for updates
		ProtocolLibrary.disableUpdates();

		// Wait until the server and all the plugins have loaded
		Bukkit.getScheduler().callSyncMethod(FAKE_PLUGIN, () -> {
			initializePlugin(FAKE_PLUGIN);
			return null;
		}).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

		// Plugins are now ready
		ProtocolLibrary.getConfig().setDebug(true);
	}

	/**
	 * Close the CraftBukkit server when they're done.
	 */
	//@AfterAll
	public static void shutdownCraftBukkit() {
		Bukkit.shutdown();
	}

	/**
	 * Copy ProtocolLib into the plugins folder.
	 *
	 * @throws IOException If anything went wrong.
	 */
	private static void setupPlugins() throws IOException {
		File pluginDirectory = new File("plugins/");
		File srcDirectory = new File("../");
		File bestFile = null;
		int bestLength = Integer.MAX_VALUE;

		for (File file : srcDirectory.listFiles()) {
			String name = file.getName();

			if (file.isFile() && name.startsWith("ProtocolLib") && name.length() < bestLength) {
				bestLength = name.length();
				bestFile = file;
			}
		}

		if (bestFile == null) {
			throw new IllegalStateException("Cannot find ProtocolLib in " + srcDirectory);
		}

		// Copy the ProtocolLib plugin to the server
		if (pluginDirectory.exists()) {
			deleteFolder(pluginDirectory);
		}

		pluginDirectory.mkdirs();

		File destination = new File(pluginDirectory, bestFile.getName()).getAbsoluteFile();
		Files.copy(bestFile, destination);
	}

	private static void deleteFolder(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteFolder(file);
					} else {
						file.delete();
					}
				}
			}
		}
	}

	/**
	 * Load a specific fake plugin.
	 *
	 * @param plugin - the plugin to load.
	 */
	@SuppressWarnings("unchecked")
	private static void initializePlugin(Plugin plugin) {
		PluginManager manager = Bukkit.getPluginManager();
		ExactReflection reflect = ExactReflection.fromObject(manager, true);

		try {
			List<Object> plugins = (List<Object>) Accessors.getFieldAccessor(reflect.getField("plugins")).get(manager);
			Map<String, Plugin> lookupNames = (Map<String, Plugin>) Accessors
					.getFieldAccessor(reflect.getField("lookupNames"))
					.get(manager);

			// Associate this plugin
			plugins.add(plugin);
			lookupNames.put(plugin.getName(), plugin);
		} catch (Exception e) {
			throw new RuntimeException("Unable to access the fields of " + manager, e);
		}
	}

	/**
	 * Create a mockable plugin for all the tests.
	 *
	 * @param fakePluginName - the fake plugin name.
	 * @return The plugin.
	 */
	private static Plugin createPlugin(String fakePluginName) {
		Plugin plugin = mock(Plugin.class);
		PluginDescriptionFile description = mock(PluginDescriptionFile.class);

		when(description.getDepend()).thenReturn(Lists.newArrayList("ProtocolLib"));
		when(description.getSoftDepend()).thenReturn(Collections.emptyList());
		when(description.getLoadBefore()).thenReturn(Collections.emptyList());
		when(description.getLoad()).thenReturn(PluginLoadOrder.POSTWORLD);

		when(plugin.getName()).thenReturn(fakePluginName);
		when(plugin.getServer()).thenReturn(Bukkit.getServer());
		when(plugin.isEnabled()).thenReturn(true);
		when(plugin.getDescription()).thenReturn(description);
		return plugin;
	}

	//@Test
	public void testPingPacket() throws Throwable {
		TestPingPacket.newTest().startTest(FAKE_PLUGIN);
	}
}
