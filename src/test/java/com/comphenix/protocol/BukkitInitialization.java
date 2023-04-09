package com.comphenix.protocol;

import java.util.Collections;
import java.util.List;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflectionTestUtil;
import net.minecraft.SharedConstants;
import net.minecraft.core.IRegistry;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.level.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_19_R3.util.Versioning;
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
	private boolean initialized;
	private boolean packaged;

	private BukkitInitialization() {
		System.out.println("Created new BukkitInitialization on " + Thread.currentThread().getName());
	}

	/**
	 * Statically initializes the mock server for unit testing
	 */
	public static void initializeAll() {
		instance.initialize();
	}

	private static final Object initLock = new Object();

	/**
	 * Initialize Bukkit and ProtocolLib such that we can perform unit testing
	 */
	private void initialize() {
		if (initialized) {
			return;
		}

		synchronized (initLock) {
			if (initialized) {
				return;
			}

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

			String releaseTarget = MinecraftReflectionTestUtil.RELEASE_TARGET;
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
				FieldAccessor spigotConfig = Accessors.getFieldAccessor(nmsWorld.getClass().getField("spigotConfig"));
				spigotConfig.set(nmsWorld, mockWorldConfig);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}

			CraftWorld world = mock(CraftWorld.class);
			when(world.getHandle()).thenReturn(nmsWorld);

			List<World> worlds = Collections.singletonList(world);
			when(mockedServer.getWorlds()).thenReturn(worlds);

			// Inject this fake server
			Bukkit.setServer(mockedServer);

			initialized = true;
		}
	}

	/**
	 * Ensure that package names are correctly set up.
	 */
	private void setPackage() {
		if (!this.packaged) {
			this.packaged = true;

			try {
				LogManager.getLogger();
			} catch (Throwable ex) {
				// Happens only on my Jenkins, but if it errors here it works when it matters
				ex.printStackTrace();
			}

			MinecraftReflectionTestUtil.init();
		}
	}
}
