package com.comphenix.protocol;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflectionTestUtil;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R7.CraftLootTable;
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_21_R7.tag.CraftBlockTag;
import org.bukkit.craftbukkit.v1_21_R7.tag.CraftEntityTag;
import org.bukkit.craftbukkit.v1_21_R7.tag.CraftFluidTag;
import org.bukkit.craftbukkit.v1_21_R7.tag.CraftItemTag;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_21_R7.util.Versioning;
import org.jetbrains.annotations.NotNull;
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
            SharedConstants.tryDetectVersion(); // .tryDetectVersion()
            Bootstrap.bootStrap(); // .bootStrap()

            PackRepository repo = ServerPacksSource.createVanillaTrustedRepository();
            repo.reload();

            ResourceManager resourceManager = new MultiPackResourceManager(
                PackType.SERVER_DATA,
                repo.getAvailablePacks().stream().map(Pack::open).collect(Collectors.toList())
            );

            LayeredRegistryAccess<RegistryLayer> layeredAccess1 = RegistryLayer.createRegistryAccess();
            List<net.minecraft.core.Registry.PendingTags<?>> tags = TagLoader.loadTagsForExistingRegistries(resourceManager, layeredAccess1.getLayer(RegistryLayer.STATIC));
            RegistryAccess.Frozen access1 = layeredAccess1.getAccessForLoading(RegistryLayer.WORLDGEN);
            List<HolderLookup.RegistryLookup<?>> list1 = TagLoader.buildUpdatedLookups(access1, tags);
            RegistryAccess.Frozen access2 = RegistryDataLoader.load(resourceManager, list1, RegistryDataLoader.WORLDGEN_REGISTRIES);
            LayeredRegistryAccess<RegistryLayer> layeredAccess2 = layeredAccess1.replaceFrom(RegistryLayer.WORLDGEN, access2);
            RegistryAccess.Frozen registryCustom = layeredAccess2.compositeAccess().freeze();

            ReloadableServerResources dataPackResources = ReloadableServerResources.loadResources(
                resourceManager,
                layeredAccess2,
                tags,
                FeatureFlags.REGISTRY.allFlags() /* REGISTRY.allFlags() */,
                Commands.CommandSelection.DEDICATED /* DEDICATED */,
                LevelBasedPermissionSet.ALL,
                MoreExecutors.directExecutor(),
                MoreExecutors.directExecutor()
            ).join();
            dataPackResources.updateStaticRegistryTags();

            /* try {
                IRegistry.class.getName();
            } catch (Throwable ex) {
                ex.printStackTrace();
            } */

            String releaseTarget = MinecraftReflectionTestUtil.RELEASE_TARGET;
            String serverVersion = CraftServer.class.getPackage().getImplementationVersion();

            // Mock the server object
            CraftServer mockedServer = mock(CraftServer.class);
            DedicatedServer mockedGameServer = mock(DedicatedServer.class);

            when(mockedGameServer.registryAccess()).thenReturn(registryCustom);

            when(mockedServer.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Minecraft"));
            when(mockedServer.getName()).thenReturn("Mock Server");
            when(mockedServer.getVersion()).thenReturn(serverVersion + " (MC: " + releaseTarget + ")");
            when(mockedServer.getBukkitVersion()).thenReturn(Versioning.getBukkitVersion());
            when(mockedServer.getServer()).thenReturn(mockedGameServer);

            when(mockedServer.isPrimaryThread()).thenReturn(true);
            when(mockedServer.getItemFactory()).thenReturn(CraftItemFactory.instance());
            when(mockedServer.getUnsafe()).thenReturn(CraftMagicNumbers.INSTANCE);
            when(mockedServer.getLootTable(any())).thenAnswer(invocation -> {
                NamespacedKey key = invocation.getArgument(0);
                return new CraftLootTable(key, dataPackResources.fullRegistries().getLootTable(CraftLootTable.bukkitKeyToMinecraft(key)));
            });
            when(mockedServer.getRegistry(any())).thenAnswer(invocation -> {
                Class<Keyed> registryType = invocation.getArgument(0);
                Object registry = CraftRegistry.createRegistry(registryType, registryCustom);

                if (registry == null) {
                    System.err.println("WARN: Missing registry for " + registryType);
                    return new DummyRegistry<>();
                }

                return registry;
            });

            when(mockedServer.getTag(any(), any(), any())).then(mock -> {
                String registry = mock.getArgument(0);
                Class<?> clazz = mock.getArgument(2);
                Identifier key = CraftNamespacedKey.toMinecraft(mock.getArgument(1));

                switch (registry) {
                    case org.bukkit.Tag.REGISTRY_BLOCKS -> {
                        Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have block type");
                        TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, key);
                        if (BuiltInRegistries.BLOCK.get(blockTagKey).isPresent()) {
                            return new CraftBlockTag(BuiltInRegistries.BLOCK, blockTagKey);
                        }
                    }
                    case org.bukkit.Tag.REGISTRY_ITEMS -> {
                        Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have item type");
                        TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, key);
                        if (BuiltInRegistries.ITEM.get(itemTagKey).isPresent()) {
                            return new CraftItemTag(BuiltInRegistries.ITEM, itemTagKey);
                        }
                    }
                    case org.bukkit.Tag.REGISTRY_FLUIDS -> {
                        Preconditions.checkArgument(clazz == org.bukkit.Fluid.class, "Fluid namespace must have fluid type");
                        TagKey<Fluid> fluidTagKey = TagKey.create(Registries.FLUID, key);
                        if (BuiltInRegistries.FLUID.get(fluidTagKey).isPresent()) {
                            return new CraftFluidTag(BuiltInRegistries.FLUID, fluidTagKey);
                        }
                    }
                    case org.bukkit.Tag.REGISTRY_ENTITY_TYPES -> {
                        Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace must have entity type");
                        TagKey<EntityType<?>> entityTagKey = TagKey.create(Registries.ENTITY_TYPE, key);
                        if (BuiltInRegistries.ENTITY_TYPE.get(entityTagKey).isPresent()) {
                            return new CraftEntityTag(BuiltInRegistries.ENTITY_TYPE, entityTagKey);
                        }
                    }
                    default -> throw new IllegalArgumentException();
                }

                return null;
            });

            ServerLevel nmsWorld = mock(ServerLevel.class);
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
            Enchantments.AQUA_AFFINITY.getClass();

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

    class DummyRegistry<T extends Keyed> implements Registry<T> {

        @Override
        public Iterator<T> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public T get(NamespacedKey key) {
            return null;
        }

        @Override
        public T getOrThrow(@NotNull NamespacedKey namespacedKey) {
            return null;
        }

        @Override
        public Stream<T> stream() {
            List<T> empty = Collections.emptyList();
            return empty.stream();
        }
    }
}
