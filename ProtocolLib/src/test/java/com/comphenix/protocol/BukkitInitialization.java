package com.comphenix.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.minecraft.server.v1_8_R2.DispenserRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.utility.Constants;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.ItemFactoryDelegate;

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
		// None of this works in 1.8
		if (!initialized) {
			// Denote that we're done
			initialized = true;

			initializePackage();

			DispenserRegistry.c(); // Basically registers everything

			// Mock the server object
			Server mockedServer = mock(Server.class);
			ItemMeta mockedMeta = mock(ItemMeta.class);
			ItemFactory mockedFactory = new ItemFactoryDelegate(mockedMeta);

			when(mockedServer.getItemFactory()).thenReturn(mockedFactory);
			when(mockedServer.isPrimaryThread()).thenReturn(true);
			// when(mockedFactory.getItemMeta(any(Material.class))).thenReturn(mockedMeta);

			// Inject this fake server
			FieldUtils.writeStaticField(Bukkit.class, "server", mockedServer, true);

			// TODO Figure this out
			/* try {
				FieldUtils.writeStaticFinalField(CraftItemFactory.class, "instance", mockedFactory, true);
			} catch (Exception ex) {
				System.err.println("Failed to inject fake item factory: ");
				ex.printStackTrace();
			} */
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
