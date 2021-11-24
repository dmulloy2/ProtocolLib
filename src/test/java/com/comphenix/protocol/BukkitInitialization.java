package com.comphenix.protocol;

import java.util.Collections;
import java.util.List;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.utility.Constants;

import net.minecraft.SharedConstants;
import net.minecraft.core.IRegistry;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.level.WorldServer;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_18_R1.util.Versioning;
import org.spigotmc.SpigotWorldConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Used to ensure that ProtocolLib and Bukkit is prepared to be tested.
 *
 * @author Kristian
 */
public class BukkitInitialization {
	private static final BukkitInitialization instance = new BukkitInitialization();

	private BukkitInitialization() {
		System.out.println("Created new BukkitInitialization on " + Thread.currentThread().getName());
	}

	private boolean initialized;
	private boolean packaged;

	/**
	 * Statically initializes the mock server for unit testing
	 */
	public static synchronized void initializeAll() {
		instance.initialize();
	}

	/**
	 * @deprecated - Replaced with initializeAll()
	 */
	@Deprecated
	public static synchronized void initializePackage() {
		initializeAll();
	}

	/**
	 * @deprecated - Replaced with initializeAll()
	 */
	@Deprecated
	public static synchronized void initializeItemMeta() {
		initializeAll();
	}

	/**
	 * Initialize Bukkit and ProtocolLib such that we can perfrom unit testing
	 */
	private void initialize() {
		if (!initialized) {
			// Denote that we're done
			initialized = true;

			try {
				LogManager.getLogger();
			} catch (Throwable ex) {
				// Happens only on my Jenkins, but if it errors here it works when it matters
				ex.printStackTrace();
			}

			instance.setPackage();

			SharedConstants.a();
			DispenserRegistry.a();

			try {
				IRegistry.class.getName();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}

			String releaseTarget = SharedConstants.b().getReleaseTarget();
			String serverVersion = CraftServer.class.getPackage().getImplementationVersion();

			// Mock the server object
			Server mockedServer = mock(Server.class);

			when(mockedServer.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Minecraft"));
			when(mockedServer.getName()).thenReturn("Mock Server");
			when(mockedServer.getVersion()).thenReturn(serverVersion + " (MC: " + releaseTarget + ")");
			when(mockedServer.getBukkitVersion()).thenReturn(Versioning.getBukkitVersion());

			when(mockedServer.getItemFactory()).thenReturn(CraftItemFactory.instance());
			when(mockedServer.isPrimaryThread()).thenReturn(true);

			WorldServer nmsWorld = mock(WorldServer.class);

			SpigotWorldConfig mockWorldConfig = mock(SpigotWorldConfig.class);

			try {
				FieldUtils.writeField(nmsWorld.getClass().getField("spigotConfig"), nmsWorld, mockWorldConfig, true);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}

			CraftWorld world = mock(CraftWorld.class);
			when(world.getHandle()).thenReturn(nmsWorld);

			List<World> worlds = Collections.singletonList(world);
			when(mockedServer.getWorlds()).thenReturn(worlds);

			// Inject this fake server
			Bukkit.setServer(mockedServer);
		}
	}

	/**
	 * Ensure that package names are correctly set up.
	 */
	private void setPackage() {
		if (!packaged) {
			packaged = true;

			try {
				LogManager.getLogger();
			} catch (Throwable ex) {
				// Happens only on my Jenkins, but if it errors here it works when it matters
				ex.printStackTrace();
			}

			Constants.init();
		}
	}
}