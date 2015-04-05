package com.comphenix.protocol;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemFactory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.utility.Constants;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

/**
 * Used to ensure that ProtocolLib and Bukkit is prepared to be tested.
 *
 * @author Kristian
 */
public class BukkitInitialization {
	private static boolean initialized;

	/**
	 * Initialize Bukkit and ProtocolLib such that we can perfrom unit testing.
	 * @throws IllegalAccessException If we are unable to initialize Bukkit.
	 */
	public static void initializeItemMeta() throws IllegalAccessException {
		if (!initialized) {
			// Denote that we're done
			initialized = true;

			initializePackage();

			/* "Accessed X before bootstrap!
			try {
				Block.S(); // Block.register()
			} catch (Throwable ex) {
				System.err.println("Failed to register blocks: " + ex);
			}

			try {
				Item.t(); // Item.register()
			} catch (Throwable ex) {
				System.err.println("Failed to register items: " + ex);
			}

			try {
				StatisticList.a(); // StatisticList.register()
			} catch (Throwable ex) {
				System.err.println("Failed to register statistics: " + ex);
			} */

			// Mock the server object
			Server mockedServer = mock(Server.class);
			ItemFactory mockedFactory = mock(CraftItemFactory.class);
			ItemMeta mockedMeta = mock(ItemMeta.class);

			when(mockedServer.getItemFactory()).thenReturn(mockedFactory);
			when(mockedServer.isPrimaryThread()).thenReturn(true);
			when(mockedFactory.getItemMeta(any(Material.class))).thenReturn(mockedMeta);

			// Inject this fake server
			FieldUtils.writeStaticField(Bukkit.class, "server", mockedServer, true);

			// And the fake item factory
			FieldUtils.writeStaticField(CraftItemFactory.class, "instance", mockedFactory, true);
		}
	}

	/**
	 * Ensure that package names are correctly set up.
	 */
	public static void initializePackage() {
		// Initialize reflection
		MinecraftReflection.setMinecraftPackage(Constants.NMS, Constants.OBC);
		MinecraftVersion.setCurrentVersion(MinecraftVersion.BOUNTIFUL_UPDATE);
	}
}
