package com.comphenix.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import net.minecraft.server.v1_8_R3.DispenserRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_8_R3.util.Versioning;

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

			DispenserRegistry.c(); // Basically registers everything

			// Mock the server object
			Server mockedServer = mock(Server.class);

			when(mockedServer.getLogger()).thenReturn(Logger.getLogger("Minecraft"));
			when(mockedServer.getName()).thenReturn("Mock Server");
			when(mockedServer.getVersion()).thenReturn(CraftServer.class.getPackage().getImplementationVersion());
			when(mockedServer.getBukkitVersion()).thenReturn(Versioning.getBukkitVersion());

			when(mockedServer.getItemFactory()).thenReturn(CraftItemFactory.instance());
			when(mockedServer.isPrimaryThread()).thenReturn(true);

			// Inject this fake server
			Bukkit.setServer(mockedServer);

			initializePackage();
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
