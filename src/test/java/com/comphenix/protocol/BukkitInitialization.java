package com.comphenix.protocol;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflectionTestUtil;

import com.google.common.util.concurrent.MoreExecutors;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.DataPackResources;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.repository.ResourcePackLoader;
import net.minecraft.server.packs.repository.ResourcePackRepository;
import net.minecraft.server.packs.repository.ResourcePackSourceVanilla;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftLootTable;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.util.Versioning;
import org.spigotmc.SpigotWorldConfig;

import static org.mockito.ArgumentMatchers.any;
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

            // Minecraft Data Init
            SharedConstants.a(); // .tryDetectVersion()
            DispenserRegistry.a(); // .bootStrap()

            ResourcePackRepository resourcePackRepository = ResourcePackSourceVanilla.c(); // .createVanillaTrustedRepository()
            resourcePackRepository.a(); // .reload()

            ResourceManager resourceManager = new ResourceManager(
                    EnumResourcePackType.b /* SERVER_DATA */,
                    resourcePackRepository.c() /* getAvailablePacks() */ .stream().map(ResourcePackLoader::e /* openFull() */).collect(Collectors.toList()));
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = RegistryLayer.a(); // .createRegistryAccess()
            layeredRegistryAccess = WorldLoader.b(resourceManager, layeredRegistryAccess, RegistryLayer.b /* WORLDGEN */, RegistryDataLoader.a /* WORLDGEN_REGISTRIES */); // .loadAndReplaceLayer()
			IRegistryCustom.Dimension registryCustom = layeredRegistryAccess.a().d(); // .compositeAccess().freeze()
            // IRegistryCustom.Dimension registryCustom = layeredRegistryAccess.a().c(); // .compositeAccess().freeze()

            DataPackResources dataPackResources = DataPackResources.a(
                    resourceManager,
                    registryCustom,
					FeatureFlagSet.a() /* REGISTRY.allFlags() */,
                    CommandDispatcher.ServerType.b /* DEDICATED */,
                    0,
                    MoreExecutors.directExecutor(),
                    MoreExecutors.directExecutor()
            ).join();
            dataPackResources.a(registryCustom); // .updateRegistryTags()

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

            when(mockedServer.isPrimaryThread()).thenReturn(true);
            when(mockedServer.getItemFactory()).thenReturn(CraftItemFactory.instance());
            when(mockedServer.getUnsafe()).thenReturn(CraftMagicNumbers.INSTANCE);
            when(mockedServer.getLootTable(any())).thenAnswer(invocation -> {
                NamespacedKey key = invocation.getArgument(0);
                return new CraftLootTable(key, dataPackResources.b() /* .getLootData() */ .getLootTable(CraftNamespacedKey.toMinecraft(key)));
            });
            when(mockedServer.getRegistry(any())).thenAnswer(invocation -> {
                Class<Keyed> registryType = invocation.getArgument(0);
                return CraftRegistry.createRegistry(registryType, registryCustom);
            });

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

            // Inject this fake server & our registry (must happen after server set)
            Bukkit.setServer(mockedServer);
            CraftRegistry.setMinecraftRegistry(registryCustom);

            // Init Enchantments
            Enchantments.A.getClass();
            // Enchantment.stopAcceptingRegistrations();

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
