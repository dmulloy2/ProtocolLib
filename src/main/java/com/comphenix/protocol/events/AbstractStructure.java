package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey.WrappedProfileKeyData;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;

public abstract class AbstractStructure {
    protected transient Object handle;
    protected transient StructureModifier<Object> structureModifier;

    protected AbstractStructure() {}

    protected AbstractStructure(Object handle, StructureModifier<Object> modifier) {
        Validate.notNull(handle, "handle cannot be null");
        Validate.notNull(modifier, "modifier cannot be null");

        this.handle = handle;
        this.structureModifier = modifier;
    }

    public Object getHandle() {
        return handle;
    }

    public StructureModifier<Object> getModifier() {
        return structureModifier;
    }

    /**
     * Retrieves a read/write structure for every field with the given type.
     * @param <T> Type
     * @param primitiveType - the type to find.
     * @return A modifier for this specific type.
     */
    public <T> StructureModifier<T> getSpecificModifier(Class<T> primitiveType) {
        return structureModifier.withType(primitiveType);
    }

    /**
     * Retrieves a read/write structure for every byte field.
     * @return A modifier for every byte field.
     */
    public StructureModifier<Byte> getBytes() {
        return structureModifier.withType(byte.class);
    }

    /**
     * Retrieves a read/write structure for every boolean field.
     * @return A modifier for every boolean field.
     */
    public StructureModifier<Boolean> getBooleans() {
        return structureModifier.withType(boolean.class);
    }

    /**
     * Retrieves a read/write structure for every short field.
     * @return A modifier for every short field.
     */
    public StructureModifier<Short> getShorts() {
        return structureModifier.withType(short.class);
    }

    /**
     * Retrieves a read/write structure for every integer field.
     * @return A modifier for every integer field.
     */
    public StructureModifier<Integer> getIntegers() {
        return structureModifier.withType(int.class);
    }
    /**
     * Retrieves a read/write structure for every long field.
     * @return A modifier for every long field.
     */
    public StructureModifier<Long> getLongs() {
        return structureModifier.withType(long.class);
    }

    /**
     * Retrieves a read/write structure for every float field.
     * @return A modifier for every float field.
     */
    public StructureModifier<Float> getFloat() {
        return structureModifier.withType(float.class);
    }

    /**
     * Retrieves a read/write structure for every double field.
     * @return A modifier for every double field.
     */
    public StructureModifier<Double> getDoubles() {
        return structureModifier.withType(double.class);
    }

    /**
     * Retrieves a read/write structure for every String field.
     * @return A modifier for every String field.
     */
    public StructureModifier<String> getStrings() {
        return structureModifier.withType(String.class);
    }

    /**
     * Retrieves a read/write structure for every UUID field.
     * @return A modifier for every UUID field.
     */
    public StructureModifier<UUID> getUUIDs() {
        return structureModifier.withType(UUID.class);
    }

    /**
     * Retrieves a read/write structure for every String array field.
     * @return A modifier for every String array field.
     */
    public StructureModifier<String[]> getStringArrays() {
        return structureModifier.withType(String[].class);
    }

    /**
     * Retrieves a read/write structure for every byte array field.
     * @return A modifier for every byte array field.
     */
    public StructureModifier<byte[]> getByteArrays() {
        return structureModifier.withType(byte[].class);
    }

    /**
     * Retrieve a serializer for reading and writing ItemStacks stored in a byte array.
     * @return A instance of the serializer.
     */
    public StreamSerializer getByteArraySerializer() {
        return new StreamSerializer();
    }

    /**
     * Retrieves a read/write structure for every int array field.
     * @return A modifier for every int array field.
     */
    public StructureModifier<int[]> getIntegerArrays() {
        return structureModifier.withType(int[].class);
    }

    /**
     * Retrieves a read/write structure for every short array field.
     * @return A modifier for every short array field.
     */
    public StructureModifier<short[]> getShortArrays() {
        return structureModifier.withType(short[].class);
    }

    /**
     * Retrieves a read/write structure for ItemStack.
     * <p>
     * This modifier will automatically marshal between the Bukkit ItemStack and the
     * internal Minecraft ItemStack.
     * @return A modifier for ItemStack fields.
     */
    public StructureModifier<ItemStack> getItemModifier() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getItemStackClass(),
                BukkitConverters.getItemStackConverter());
    }

    /**
     * Retrieves a read/write structure for arrays of ItemStacks.
     * <p>
     * This modifier will automatically marshal between the Bukkit ItemStack and the
     * internal Minecraft ItemStack.
     * @return A modifier for ItemStack array fields.
     */
    public StructureModifier<ItemStack[]> getItemArrayModifier() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getItemStackArrayClass(),
                Converters.ignoreNull(new ItemStackArrayConverter()));
    }

    /**
     * Retrieves a read/write structure for lists of ItemStacks.
     * <p>
     * This modifier will automatically marshal between the Bukkit ItemStack and the
     * internal Minecraft ItemStack.
     * @return A modifier for ItemStack list fields.
     */
    public StructureModifier<List<ItemStack>> getItemListModifier() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                List.class,
                BukkitConverters.getListConverter(BukkitConverters.getItemStackConverter())
        );
    }

    /**
     * Retrieve a read/write structure for maps of statistics.
     * <p>
     * Note that you must write back the changed map to persist it.
     * @return A modifier for maps of statistics.
     */
    public StructureModifier<Map<WrappedStatistic, Integer>> getStatisticMaps() {
        return getMaps(
                BukkitConverters.getWrappedStatisticConverter(),
                Converters.passthrough(Integer.class));
    }

    /**
     * Retrieves a read/write structure for the world type enum.
     * <p>
     * This modifier will automatically marshal between the Bukkit world type and the
     * internal Minecraft world type.
     * @return A modifier for world type fields.
     */
    public StructureModifier<WorldType> getWorldTypeModifier() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getWorldTypeClass(),
                BukkitConverters.getWorldTypeConverter());
    }

    /**
     * Retrieves a read/write structure for data watchers.
     * @return A modifier for data watchers.
     */
    public StructureModifier<WrappedDataWatcher> getDataWatcherModifier() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getDataWatcherClass(),
                BukkitConverters.getDataWatcherConverter());
    }

    /**
     * Retrieves a read/write structure for entity objects.
     * <p>
     * Note that entities are transmitted by integer ID, and the type may not be enough
     * to distinguish between entities and other values. Thus, this structure modifier
     * MAY return null or invalid entities for certain fields. Using the correct index
     * is essential.
     *
     * @param world - the world each entity is currently occupying.
     * @return A modifier entity types.
     */
    public StructureModifier<Entity> getEntityModifier(@Nonnull World world) {
        Preconditions.checkNotNull(world, "world cannot be NULL.");
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                int.class, BukkitConverters.getEntityConverter(world));
    }

    /**
     * Retrieves a read/write structure for entity objects.
     * <p>
     * Note that entities are transmitted by integer ID, and the type may not be enough
     * to distinguish between entities and other values. Thus, this structure modifier
     * MAY return null or invalid entities for certain fields. Using the correct index
     * is essential.
     *
     * @param event - the original packet event.
     * @return A modifier entity types.
     */
    public StructureModifier<Entity> getEntityModifier(@Nonnull PacketEvent event) {
        Preconditions.checkNotNull(event, "event cannot be NULL.");
        return getEntityModifier(event.getPlayer().getWorld());
    }

    /**
     * Retrieves a read/write structure for entity types
     * @return A modifier for an EntityType.
     */
    public StructureModifier<EntityType> getEntityTypeModifier() {
        return structureModifier.withType(
                MinecraftReflection.getEntityTypes(),
                BukkitConverters.getEntityTypeConverter());
    }

    /**
     * Retrieves a read/write structure for block positions.
     * @return A modifier for a BlockPosition.
     */
    public StructureModifier<BlockPosition> getBlockPositionModifier() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getBlockPositionClass(),
                BlockPosition.getConverter());
    }

    /**
     * Retrieves a read/write structure for registrable objects.
     *
     * @param registrableClass The registrable object's class.
     * @return A modifier for a registrable objects.
     * @see MinecraftReflection#getBlockEntityTypeClass()
     */
    public StructureModifier<WrappedRegistrable> getRegistrableModifier(
        @NotNull final Class<?> registrableClass
    ) {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
            registrableClass,
            BukkitConverters.getWrappedRegistrable(registrableClass));
    }

    /**
     * Retrieves a read/write structure for BlockEntityType.
     * @return A modifier for a BlockEntityType.
     */
    public StructureModifier<WrappedRegistrable> getBlockEntityTypeModifier() {
        // Convert to and from the Bukkit wrapper
        return getRegistrableModifier(MinecraftReflection.getBlockEntityTypeClass());
    }

    /**
     * Retrieves a read/write structure for wrapped ChunkCoordIntPairs.
     * @return A modifier for ChunkCoordIntPair.
     */
    public StructureModifier<ChunkCoordIntPair> getChunkCoordIntPairs() {
        // Allow access to the NBT class in packet 130
        return structureModifier.withType(
                MinecraftReflection.getChunkCoordIntPair(),
                ChunkCoordIntPair.getConverter());
    }

    /**
     * Retrieves a read/write structure for NBT classes.
     * @return A modifier for NBT classes.
     */
    public StructureModifier<NbtBase<?>> getNbtModifier() {
        // Allow access to the NBT class in packet 130
        return structureModifier.withType(
                MinecraftReflection.getNBTBaseClass(),
                BukkitConverters.getNbtConverter());
    }

    /**
     * Retrieves a read/write structure for lists of NBT classes.
     * @return A modifier for lists of NBT classes.
     */
    public StructureModifier<List<NbtBase<?>>> getListNbtModifier() {
        // Convert to and from the ProtocolLib wrapper
        return structureModifier.withType(
                Collection.class,
                BukkitConverters.getListConverter(BukkitConverters.getNbtConverter())
        );
    }

    /**
     * Retrieves a read/write structure for Vectors.
     * @return A modifier for Vectors.
     */
    public StructureModifier<Vector> getVectors() {
        // Automatically marshal between Vec3d and the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getVec3DClass(),
                BukkitConverters.getVectorConverter());
    }

    /**
     * Retrieves a read/write structure for collections of attribute snapshots.
     * <p>
     * This modifier will automatically marshal between the visible ProtocolLib WrappedAttribute and the
     * internal Minecraft AttributeSnapshot.
     * @return A modifier for AttributeSnapshot collection fields.
     */
    public StructureModifier<List<WrappedAttribute>> getAttributeCollectionModifier() {
        // Convert to and from the ProtocolLib wrapper
        return structureModifier.withType(
                Collection.class,
                BukkitConverters.getListConverter(BukkitConverters.getWrappedAttributeConverter())
        );
    }

    /**
     * Retrieves a read/write structure for collections of chunk positions.
     * <p>
     * This modifier will automatically marshal between the visible ProtocolLib BlockPosition and the
     * internal Minecraft BlockPosition.
     *
     * @return A modifier for ChunkPosition list fields.
     */
    public StructureModifier<List<BlockPosition>> getBlockPositionCollectionModifier() {
        // Convert to and from the ProtocolLib wrapper
        return structureModifier.withType(
                Collection.class,
                BukkitConverters.getListConverter(BlockPosition.getConverter()));
    }

    /**
     * Retrieves a read/write structure for collections of watchable objects before Minecraft 1.19.3.
     * <p>
     * This modifier will automatically marshal between the visible WrappedWatchableObject and the
     * internal Minecraft WatchableObject.
     * @return A modifier for watchable object list fields.
     */
    public StructureModifier<List<WrappedWatchableObject>> getWatchableCollectionModifier() {
        // Convert to and from the ProtocolLib wrapper
        return structureModifier.withType(
                Collection.class,
                BukkitConverters.getListConverter(BukkitConverters.getWatchableObjectConverter()));
    }

    /**
     * Retrieves a read/write structure for collections of data values for Minecraft 1.19.3 or later.
     * @return A modifier for data values.
     */
    public StructureModifier<List<WrappedDataValue>> getDataValueCollectionModifier() {
        // Convert to and from the ProtocolLib wrapper
        return structureModifier.withType(
                Collection.class,
                BukkitConverters.getListConverter(BukkitConverters.getDataValueConverter()));
    }

    /**
     * Retrieves a read/write structure for block fields.
     * <p>
     * This modifier will automatically marshal between Material and the
     * internal Minecraft Block.
     * @return A modifier for GameProfile fields.
     */
    public StructureModifier<Material> getBlocks() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getBlockClass(), BukkitConverters.getBlockConverter());
    }

    /**
     * Retrieves a read/write structure for game profiles in Minecraft 1.7.2.
     * <p>
     * This modifier will automatically marshal between WrappedGameProfile and the
     * internal Minecraft GameProfile.
     * @return A modifier for GameProfile fields.
     */
    public StructureModifier<WrappedGameProfile> getGameProfiles() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getGameProfileClass(), BukkitConverters.getWrappedGameProfileConverter());
    }

    /**
     * Retrieves a read/write structure for BlockData in Minecraft 1.8.
     * <p>
     * This modifier will automatically marshal between WrappedBlockData and the
     * internal Minecraft IBlockData.
     * @return A modifier for BlockData fields.
     */
    public StructureModifier<WrappedBlockData> getBlockData() {
        // Convert to and from our wrapper
        return structureModifier.withType(
                MinecraftReflection.getIBlockDataClass(),
                BukkitConverters.getWrappedBlockDataConverter()
        );
    }

    /**
     * Retrieves a read/write structure for IBlockData arrays in Minecraft 1.16.2+
     * @return A modifier for IBlockData array fields
     */
    public StructureModifier<WrappedBlockData[]> getBlockDataArrays() {
        // TODO we might want to make this a lazy converter and only convert indexes as needed
        return structureModifier.withType(
                MinecraftReflection.getArrayClass(MinecraftReflection.getIBlockDataClass()),
                Converters.array(MinecraftReflection.getIBlockDataClass(), BukkitConverters.getWrappedBlockDataConverter())
        );
    }

    /**
     * Retrieves a read/write structure for MultiBlockChangeInfo arrays in Minecraft 1.8.
     * <p>
     * This modifier will automatically marshal between MultiBlockChangeInfo and the
     * internal Minecraft MultiBlockChangeInfo.
     * @return A modifier for BlockData fields.
     */
    public StructureModifier<MultiBlockChangeInfo[]> getMultiBlockChangeInfoArrays() {
        ChunkCoordIntPair chunk = getChunkCoordIntPairs().read(0);

        // Convert to and from our wrapper
        return structureModifier.withType(
                MinecraftReflection.getMultiBlockChangeInfoArrayClass(),
                Converters.array(MinecraftReflection.getMultiBlockChangeInfoClass(), MultiBlockChangeInfo.getConverter(chunk))
        );
    }

    /**
     * Retrieves a read/write structure for chat components in Minecraft 1.7.2.
     * <p>
     * This modifier will automatically marshal between WrappedChatComponent and the
     * internal Minecraft IChatBaseComponent.
     * @return A modifier for ChatComponent fields.
     */
    public StructureModifier<WrappedChatComponent> getChatComponents() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());
    }

    /**
     * Retrieves a read/write structure for arrays of chat components.
     * <p>
     * This modifier will automatically marshal between WrappedChatComponent and the
     * internal Minecraft IChatBaseComponent (1.9.2 and below) or the internal
     * NBTCompound (1.9.4 and up).
     * <p>
     * Note that in 1.9.4 and up this modifier only works properly with sign
     * tile entities.
     * @return A modifier for ChatComponent array fields.
     */
    public StructureModifier<WrappedChatComponent[]> getChatComponentArrays() {
        // Convert to and from the Bukkit wrapper
        return structureModifier.withType(
                ComponentArrayConverter.getGenericType(),
                Converters.ignoreNull(new ComponentArrayConverter()));
    }

    /**
     * Retrieve a read/write structure for the ServerPing fields in the following packet: <br>
     * <ul>
     *   <li>{@link PacketType.Status.Server#SERVER_INFO}
     * </ul>
     * @return A modifier for ServerPing fields.
     */
    public StructureModifier<WrappedServerPing> getServerPings() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                MinecraftReflection.getServerPingClass(),
                BukkitConverters.getWrappedServerPingConverter());
    }

    /**
     * Retrieve a read/write structure for the PlayerInfoData list fields in the following packet: <br>
     * <ul>
     *   <li>{@link PacketType.Play.Server#PLAYER_INFO}
     * </ul>
     * @return A modifier for PlayerInfoData list fields.
     */
    public StructureModifier<List<PlayerInfoData>> getPlayerInfoDataLists() {
        // Convert to and from the ProtocolLib wrapper
        return structureModifier.withType(
                Collection.class,
                BukkitConverters.getListConverter(PlayerInfoData.getConverter()));
    }

    /**
     * Retrieve a read/write structure for the Protocol enum in 1.7.2.
     * @return A modifier for Protocol enum fields.
     */
    public StructureModifier<PacketType.Protocol> getProtocols() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getProtocolClass(),
                EnumWrappers.getProtocolConverter());
    }

    /**
     * Retrieve a read/write structure for the ClientCommand enum in 1.7.2.
     * @return A modifier for ClientCommand enum fields.
     */
    public StructureModifier<EnumWrappers.ClientCommand> getClientCommands() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getClientCommandClass(),
                EnumWrappers.getClientCommandConverter());
    }

    /**
     * Retrieve a read/write structure for the ChatVisibility enum in 1.7.2.
     * @return A modifier for ChatVisibility enum fields.
     */
    public StructureModifier<EnumWrappers.ChatVisibility> getChatVisibilities() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getChatVisibilityClass(),
                EnumWrappers.getChatVisibilityConverter());
    }

    /**
     * Retrieve a read/write structure for the Difficulty enum in 1.7.2.
     * @return A modifier for Difficulty enum fields.
     */
    public StructureModifier<EnumWrappers.Difficulty> getDifficulties() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getDifficultyClass(),
                EnumWrappers.getDifficultyConverter());
    }

    /**
     * Retrieve a read/write structure for the EntityUse enum in 1.7.2.
     * @return A modifier for EntityUse enum fields.
     */
    public StructureModifier<EnumWrappers.EntityUseAction> getEntityUseActions() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getEntityUseActionClass(),
                EnumWrappers.getEntityUseActionConverter());
    }

    /**
     * Retrieves a read/write structure for the EntityUseAction class in the UseEntity packet sent by the client for
     * 1.17 and above.
     * @return A modifier for EntityUseAction class fields.
     */
    public StructureModifier<WrappedEnumEntityUseAction> getEnumEntityUseActions() {
        return structureModifier.withType(
                MinecraftReflection.getEnumEntityUseActionClass(),
                WrappedEnumEntityUseAction.CONVERTER);
    }

    /**
     * Retrieve a read/write structure for the NativeGameMode enum in 1.7.2.
     * @return A modifier for NativeGameMode enum fields.
     */
    public StructureModifier<EnumWrappers.NativeGameMode> getGameModes() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getGameModeClass(),
                EnumWrappers.getGameModeConverter());
    }

    /**
     * Retrieve a read/write structure for the ResourcePackStatus enum in 1.8.
     * @return A modifier for ResourcePackStatus enum fields.
     */
    public StructureModifier<EnumWrappers.ResourcePackStatus> getResourcePackStatus() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getResourcePackStatusClass(),
                EnumWrappers.getResourcePackStatusConverter());
    }

    /**
     * Retrieve a read/write structure for the PlayerInfo enum in 1.8.
     * @return A modifier for PlayerInfoAction enum fields.
     */
    public StructureModifier<EnumWrappers.PlayerInfoAction> getPlayerInfoAction() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getPlayerInfoActionClass(),
                EnumWrappers.getPlayerInfoActionConverter());
    }

    /**
     * Retrieve a read/write structure for an EnumSet of PlayerInfos.
     * @return A modifier for an EnumSet of PlayerInfo fields.
     */
    public StructureModifier<Set<EnumWrappers.PlayerInfoAction>> getPlayerInfoActions() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumSet.class,
                Converters.collection(
                        EnumWrappers.getPlayerInfoActionConverter(),
                        generic -> EnumSet.noneOf(EnumWrappers.PlayerInfoAction.class),
                        specific -> EnumWrappers.createEmptyEnumSet(EnumWrappers.getPlayerInfoActionClass())));
    }

    /**
     * Retrieve a read/write structure for the TitleAction enum in 1.8.
     * @return A modifier for TitleAction enum fields.
     */
    public StructureModifier<EnumWrappers.TitleAction> getTitleActions() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getTitleActionClass(),
                EnumWrappers.getTitleActionConverter());
    }

    /**
     * Retrieve a read/write structure for the WorldBorderAction enum in 1.8.
     * @return A modifier for WorldBorderAction enum fields.
     */
    public StructureModifier<EnumWrappers.WorldBorderAction> getWorldBorderActions() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getWorldBorderActionClass(),
                EnumWrappers.getWorldBorderActionConverter());
    }

    /**
     * Retrieve a read/write structure for the CombatEventType enum in 1.8.
     * @return A modifier for CombatEventType enum fields.
     */
    public StructureModifier<EnumWrappers.CombatEventType> getCombatEvents() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getCombatEventTypeClass(),
                EnumWrappers.getCombatEventTypeConverter());
    }

    /**
     * Retrieve a read/write structure for the PlayerDigType enum in 1.8.
     * @return A modifier for PlayerDigType enum fields.
     */
    public StructureModifier<EnumWrappers.PlayerDigType> getPlayerDigTypes() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getPlayerDigTypeClass(),
                EnumWrappers.getPlayerDiggingActionConverter());
    }

    /**
     * Retrieve a read/write structure for the PlayerAction enum in 1.8.
     * @return A modifier for PlayerAction enum fields.
     */
    public StructureModifier<EnumWrappers.PlayerAction> getPlayerActions() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getPlayerActionClass(),
                EnumWrappers.getEntityActionConverter());
    }

    /**
     * Retrieve a read/write structure for the ScoreboardAction enum in 1.8.
     * @return A modifier for ScoreboardAction enum fields.
     */
    public StructureModifier<EnumWrappers.ScoreboardAction> getScoreboardActions() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getScoreboardActionClass(),
                EnumWrappers.getUpdateScoreActionConverter());
    }

    /**
     * Retrieve a read/write structure for the Particle enum in 1.8-1.12.
     * <b>NOTE:</b> This will produce undesirable results in 1.13
     * @return A modifier for Particle enum fields.
     */
    public StructureModifier<EnumWrappers.Particle> getParticles() {
        // Convert to and from the wrapper
        return structureModifier.withType(
                EnumWrappers.getParticleClass(),
                EnumWrappers.getParticleConverter());
    }

    /**
     * Retrieve a read/write structure for ParticleParams in 1.13
     * @return A modifier for ParticleParam fields.
     */
    public StructureModifier<WrappedParticle> getNewParticles() {
        return structureModifier.withType(
                MinecraftReflection.getParticleParam(),
                BukkitConverters.getParticleConverter()
        );
    }

    /**
     * Retrieve a read/write structure for the MobEffectList class in 1.9.
     * @return A modifier for MobEffectList fields.
     */
    public StructureModifier<PotionEffectType> getEffectTypes() {
        if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
            return getHolders(MinecraftReflection.getMobEffectListClass(), BukkitConverters.getEffectTypeConverter());
        }

        // Convert to and from Bukkit
        return structureModifier.withType(
                MinecraftReflection.getMobEffectListClass(),
                BukkitConverters.getEffectTypeConverter());
    }

    /**
     * Retrieve a read/write structure for the SoundCategory enum in 1.9.
     * @return A modifier for SoundCategory enum fields.
     */
    public StructureModifier<EnumWrappers.SoundCategory> getSoundCategories() {
        // Convert to and from the enums
        return structureModifier.withType(
                EnumWrappers.getSoundCategoryClass(),
                EnumWrappers.getSoundCategoryConverter());
    }

    /**
     * Retrieve a read/write structure for a Holder&lt;T&gt; in 1.19.3.
     * @param genericType NMS type of T
     * @param converter Converter from genericType to T
     * @return A modifier for Holder fields
     * @param <T> Bukkit type
     */
    public <T> StructureModifier<T> getHolders(Class<?> genericType, EquivalentConverter<T> converter) {
        Preconditions.checkNotNull(genericType, "genericType cannot be null");
        Preconditions.checkNotNull(converter, "converter cannot be null");

        Class<?> holderClass = MinecraftReflection.getHolderClass();

        WrappedRegistry registry = WrappedRegistry.getRegistry(genericType);
        if (registry == null) {
            throw new IllegalArgumentException("No registry found for " + genericType);
        }

        return structureModifier.withParamType(
                holderClass,
                Converters.ignoreNull(Converters.holder(converter, registry)),
                genericType
        );
    }

    /**
     * Retrieve a read/write structure for the SoundEffect enum in 1.9.
     * @return A modifier for SoundEffect enum fields.
     */
    public StructureModifier<Sound> getSoundEffects() {
        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
            return getHolders(MinecraftReflection.getSoundEffectClass(), BukkitConverters.getSoundConverter());
        }

        // Convert to and from Bukkit
        return structureModifier.withType(
                MinecraftReflection.getSoundEffectClass(),
                BukkitConverters.getSoundConverter());
    }

    /**
     * Retrieve a read/write structure for the ItemSlot enum in 1.9.
     * @return A modifier for ItemSlot enum fields.
     */
    public StructureModifier<EnumWrappers.ItemSlot> getItemSlots() {
        return structureModifier.withType(
                EnumWrappers.getItemSlotClass(),
                EnumWrappers.getItemSlotConverter());
    }

    /**
     * Retrieve a read/write structure for the Hand enum in 1.9.
     * @return A modifier for Hand enum fields.
     */
    public StructureModifier<EnumWrappers.Hand> getHands() {
        return structureModifier.withType(
                EnumWrappers.getHandClass(),
                EnumWrappers.getHandConverter());
    }

    /**
     * Retrieve a read/write structure for the Direction enum in 1.10.
     * @return A modifier for Direction enum fields.
     */
    public StructureModifier<EnumWrappers.Direction> getDirections() {
        return structureModifier.withType(
                EnumWrappers.getDirectionClass(),
                EnumWrappers.getDirectionConverter());
    }

    /**
     * Retrieve a read/write structure for the ChatType enum in 1.12.
     * @return A modifier for ChatType enum fields.
     */
    public StructureModifier<EnumWrappers.ChatType> getChatTypes() {
        return structureModifier.withType(
                EnumWrappers.getChatTypeClass(),
                EnumWrappers.getChatTypeConverter());
    }

    /**
     * Retrieve a read/write structure for the MinecraftKey class.
     * @return A modifier for MinecraftKey fields.
     */
    public StructureModifier<MinecraftKey> getMinecraftKeys() {
        return structureModifier.withType(
                MinecraftReflection.getMinecraftKeyClass(),
                MinecraftKey.getConverter());
    }

    /**
     * Retrieve a read/write structure for custom packet payloads (available since Minecraft 1.20.2).
     * @return A modifier for CustomPacketPayloads fields.
     */
    public StructureModifier<CustomPacketPayloadWrapper> getCustomPacketPayloads() {
        return structureModifier.withType(
                CustomPacketPayloadWrapper.getCustomPacketPayloadClass(),
                CustomPacketPayloadWrapper.getConverter());
    }

    /**
     * Retrieve a read/write structure for dimension IDs in 1.13.1+
     * @return A modifier for dimension IDs
     */
    @Deprecated
    public StructureModifier<Integer> getDimensions() {
        if (MinecraftVersion.NETHER_UPDATE.atOrAbove() && !MinecraftVersion.NETHER_UPDATE_2.atOrAbove()) {
            return structureModifier.withParamType(
                    MinecraftReflection.getResourceKey(),
                    BukkitConverters.getDimensionIDConverter(),
                    MinecraftReflection.getDimensionManager()
            );
        } else {
            return structureModifier.withType(
                    MinecraftReflection.getDimensionManager(),
                    BukkitConverters.getDimensionIDConverter()
            );
        }
    }

    public StructureModifier<World> getDimensionTypes() {
        return structureModifier.withType(
                MinecraftReflection.getDimensionManager(),
                BukkitConverters.getDimensionConverter()
        );
    }

    /**
     * Retrieve a read/write structure for the MerchantRecipeList class.
     * @return A modifier for MerchantRecipeList fields.
     */
    public StructureModifier<List<MerchantRecipe>> getMerchantRecipeLists() {
        return structureModifier.withType(
                MinecraftReflection.getMerchantRecipeList(),
                BukkitConverters.getMerchantRecipeListConverter()
        );
    }

    /**
     * Retrieve a read/write structure for ItemSlot/ItemStack pair lists in 1.16+
     * @return The Structure Modifier
     */
    public StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> getSlotStackPairLists() {
        return getLists(BukkitConverters.getPairConverter(
                EnumWrappers.getItemSlotConverter(),
                BukkitConverters.getItemStackConverter()
        ));
    }

    /**
     * Retrieve a read/write structure for MovingObjectPositionBlock in 1.16+
     * @return The Structure Modifier
     */
    public StructureModifier<MovingObjectPositionBlock> getMovingBlockPositions() {
        return structureModifier.withType(
                MovingObjectPositionBlock.getNmsClass(),
                MovingObjectPositionBlock.getConverter()
        );
    }

    /**
     * Retrieve a read/write structure for World ResourceKeys in 1.16+
     * @return The Structure Modifier
     */
    public StructureModifier<World> getWorldKeys() {
        return structureModifier.withParamType(
                MinecraftReflection.getResourceKey(),
                BukkitConverters.getWorldKeyConverter(),
                MinecraftReflection.getNmsWorldClass()
        );
    }


    /**
     * Retrieve a read/write structure for SectionPositions in 1.16.2+
     * @return The Structure Modifier
     */
    public StructureModifier<BlockPosition> getSectionPositions() {
        return structureModifier.withType(
                MinecraftReflection.getSectionPosition(),
                BukkitConverters.getSectionPositionConverter()
        );
    }

    /**
     * Retrieve a read/write structure for Game State IDs in 1.16+
     * @return The Structure Modifier
     */
    public StructureModifier<Integer> getGameStateIDs() {
        return structureModifier.withType(
                MinecraftReflection.getGameStateClass(),
                BukkitConverters.getGameStateConverter()
        );
    }

    public StructureModifier<List<Integer>> getIntLists() {
        return structureModifier.withType(
                List.class,
                BukkitConverters.getListConverter(
                        MinecraftReflection.getIntArrayListClass(),
                        Converters.passthrough(int.class)
                )
        );
    }

    public StructureModifier<List<UUID>> getUUIDLists() {
        return structureModifier.withType(
                List.class,
                BukkitConverters.getListConverter(Converters.passthrough(UUID.class)));
    }

    /**
     * Retrieve a read/write structure for Instants in (mostly for use in 1.19+)
     * @return The Structure Modifier
     */
    public StructureModifier<Instant> getInstants() {
        return structureModifier.withType(Instant.class);
    }

    /**
     * Retrieve a read/write structure for profile public keys in 1.19
     * @return The Structure Modifier
     */
    public StructureModifier<WrappedProfilePublicKey> getProfilePublicKeys() {
        return structureModifier.withType(
            MinecraftReflection.getProfilePublicKeyClass(),
            BukkitConverters.getWrappedProfilePublicKeyConverter());
    }

    /**
     * Retrieve a read/write structure for profile public key data in 1.19
     * @return The Structure Modifier
     */
    public StructureModifier<WrappedProfileKeyData> getProfilePublicKeyData() {
        return structureModifier.withType(
                MinecraftReflection.getProfilePublicKeyDataClass(),
                BukkitConverters.getWrappedPublicKeyDataConverter());
    }

    /**
     * Retrieves read/write structure for remote chat session data in 1.19.3
     * @return The Structure Modifier
     */
    public StructureModifier<WrappedRemoteChatSessionData> getRemoteChatSessionData() {
        return structureModifier.withType(
                MinecraftReflection.getRemoteChatSessionDataClass(),
                BukkitConverters.getWrappedRemoteChatSessionDataConverter()
        );
    }

    /**
     * Retrieve a read/write structure for LevelChunkPacketData in 1.18+
     *
     * @return The Structure Modifier
     */
    public StructureModifier<WrappedLevelChunkData.ChunkData> getLevelChunkData() {
        return structureModifier.withType(MinecraftReflection.getLevelChunkPacketDataClass(), BukkitConverters.getWrappedChunkDataConverter());
    }

    /**
     * Retrieve a read/write structure for LightUpdatePacketData in 1.18+
     *
     * @return The Structure Modifier
     */
    public StructureModifier<WrappedLevelChunkData.LightData> getLightUpdateData() {
        return structureModifier.withType(MinecraftReflection.getLightUpdatePacketDataClass(), BukkitConverters.getWrappedLightDataConverter());
    }

    /**
     * @return read/write structure for login encryption packets
     */
    public StructureModifier<Either<byte[], WrappedSaltedSignature>> getLoginSignatures() {
        return getEithers(Converters.passthrough(byte[].class), BukkitConverters.getWrappedSignatureConverter());
    }

    /**
     * @return read/writer structure direct access to salted signature data like chat messages
     */
    public StructureModifier<WrappedSaltedSignature> getSignatures() {
        return structureModifier.withType(
                MinecraftReflection.getSaltedSignatureClass(),
                BukkitConverters.getWrappedSignatureConverter()
        );
    }

    /**
     * @return read/writer structure direct access to unsalted signature data for example in chat message (since 1.19.3)
     */
    public StructureModifier<WrappedMessageSignature> getMessageSignatures() {
        return structureModifier.withType(
                MinecraftReflection.getMessageSignatureClass(),
                BukkitConverters.getWrappedMessageSignatureConverter()
        );
    }

    /**
     * @param leftConverter converter for left values
     * @param rightConverter converter for right values
     * @return ProtocolLib's read/write structure for Mojang either structures
     * @param <L> left data type after converting from NMS
     * @param <R> right data type after converting from NMS
     */
    public <L, R> StructureModifier<Either<L, R>> getEithers(EquivalentConverter<L> leftConverter,
                                                             EquivalentConverter<R> rightConverter) {
        return structureModifier.withType(
                com.mojang.datafixers.util.Either.class,
                BukkitConverters.getEitherConverter(
                        leftConverter, rightConverter
                )
        );
    }

    /**
     * Retrieve a read/write structure for the Map class.
     * @param keyConverter Converter for map keys
     * @param valConverter Converter for map values
     * @param <K> Key param
     * @param <V> Value param
     * @return A modifier for Map fields.
     *
     * @see BukkitConverters
     * @see EquivalentConverter
     */
    public <K, V> StructureModifier<Map<K, V>> getMaps(EquivalentConverter<K> keyConverter,
                                                       EquivalentConverter<V> valConverter) {
        return structureModifier.withType(
                Map.class,
                BukkitConverters.getMapConverter(keyConverter, valConverter));
    }

    /**
     * Retrieve a read/write structure for the Set class.
     * @param converter Converter for elements
     * @param <E> Element param
     * @return A modifier for Set fields
     *
     * @see BukkitConverters
     * @see EquivalentConverter
     */
    public <E> StructureModifier<Set<E>> getSets(EquivalentConverter<E> converter) {
        return structureModifier.withType(
                Set.class,
                BukkitConverters.getSetConverter(converter));
    }

    /**
     * Retrieve a read/write structure for the List class.
     * @param converter Converter for elements
     * @param <E> Element param
     * @return A modifier for List fields
     */
    public <E> StructureModifier<List<E>> getLists(EquivalentConverter<E> converter) {
        return structureModifier.withType(
                List.class,
                BukkitConverters.getListConverter(converter));
    }

    /**
     * Retrieve a read/write structure for an enum. This allows for the use of
     * user-created enums that may not exist in ProtocolLib. The specific (user
     * created) enum constants must match up perfectly with their generic (NMS)
     * counterparts.
     *
     * @param enumClass The specific Enum class
     * @param nmsClass The generic Enum class
     * @return The modifier
     */
    public <T extends Enum<T>> StructureModifier<T> getEnumModifier(Class<T> enumClass, Class<?> nmsClass) {
        return structureModifier.withType(
                nmsClass,
                new EnumWrappers.EnumConverter<>(nmsClass, enumClass));
    }

    /**
     * Retrieve a read/write structure for an enum. This method is for convenience,
     * see {@link #getEnumModifier(Class, Class)} for more information.
     *
     * @param enumClass The specific Enum class
     * @param index Index of the generic Enum
     * @return The modifier
     * @see #getEnumModifier(Class, Class)
     */
    public <T extends Enum<T>> StructureModifier<T> getEnumModifier(Class<T> enumClass, int index) {
        return getEnumModifier(
                enumClass,
                structureModifier.getField(index).getType());
    }

    /**
     * Retrieve a read/write structure for an optional, passing
     * the value of the optional field through the given converter
     * if present.
     *
     * @param converter Converter for internal element of optional, if present
     * @param <T> The inner type of the optional
     * @return The modifier
     */
    public <T> StructureModifier<Optional<T>> getOptionals(EquivalentConverter<T> converter) {
        return structureModifier.withType(Optional.class, Converters.optional(converter));
    }

    public StructureModifier<Iterable<PacketContainer>> getPacketBundles() {
        return structureModifier.withType(Iterable.class, Converters.iterable(
            BukkitConverters.getPacketContainerConverter(), ArrayList::new, ArrayList::new
        ));
    }

    /**
     * Represents an equivalent converter for ItemStack arrays.
     * @author Kristian
     */
    private static class ItemStackArrayConverter implements EquivalentConverter<ItemStack[]> {
        final EquivalentConverter<ItemStack> stackConverter = BukkitConverters.getItemStackConverter();

        @Override
        public Object getGeneric(ItemStack[] specific) {
            Class<?> nmsStack = MinecraftReflection.getItemStackClass();
            Object[] result = (Object[]) Array.newInstance(nmsStack, specific.length);

            // Unwrap every item
            for (int i = 0; i < result.length; i++) {
                result[i] = stackConverter.getGeneric(specific[i]);
            }
            return result;
        }

        @Override
        public ItemStack[] getSpecific(Object generic) {
            Object[] input = (Object[]) generic;
            ItemStack[] result = new ItemStack[input.length];

            // Add the wrapper
            for (int i = 0; i < result.length; i++) {
                result[i] = stackConverter.getSpecific(input[i]);
            }
            return result;
        }

        @Override
        public Class<ItemStack[]> getSpecificType() {
            return ItemStack[].class;
        }
    }

    /**
     * Represents an equivalent converter for ChatComponent arrays.
     * @author dmulloy2
     */
    private static class LegacyComponentConverter implements EquivalentConverter<WrappedChatComponent[]> {
        final EquivalentConverter<WrappedChatComponent> componentConverter = BukkitConverters.getWrappedChatComponentConverter();

        @Override
        public Object getGeneric(WrappedChatComponent[] specific) {
            Class<?> nmsComponent = MinecraftReflection.getIChatBaseComponentClass();
            Object[] result = (Object[]) Array.newInstance(nmsComponent, specific.length);

            // Unwrap every item
            for (int i = 0; i < result.length; i++) {
                result[i] = componentConverter.getGeneric(specific[i]);
            }
            return result;
        }

        @Override
        public WrappedChatComponent[] getSpecific(Object generic) {
            Object[] input = (Object[]) generic;
            WrappedChatComponent[] result = new WrappedChatComponent[input.length];

            // Add the wrapper
            for (int i = 0; i < result.length; i++) {
                result[i] = componentConverter.getSpecific(input[i]);
            }
            return result;
        }

        @Override
        public Class<WrappedChatComponent[]> getSpecificType() {
            return WrappedChatComponent[].class;
        }
    }

    /**
     * Converts from NBT to WrappedChatComponent arrays
     * @author dmulloy2
     */
    private static class NBTComponentConverter implements EquivalentConverter<WrappedChatComponent[]> {
        private final EquivalentConverter<NbtBase<?>> nbtConverter = BukkitConverters.getNbtConverter();
        private static final int LINES = 4;

        @Override
        public WrappedChatComponent[] getSpecific(Object generic) {
            final NbtBase<?> nbtBase = nbtConverter.getSpecific(generic);
            final NbtCompound compound = (NbtCompound) nbtBase;

            final WrappedChatComponent[] components = new WrappedChatComponent[LINES];
            for (int i = 0; i < LINES; i++) {
                if (compound.containsKey("Text" + (i + 1))) {
                    components[i] = WrappedChatComponent.fromJson(compound.getString("Text" + (i + 1)));
                } else {
                    components[i] = WrappedChatComponent.fromText("");
                }
            }

            return components;
        }

        @Override
        public Object getGeneric(WrappedChatComponent[] specific) {
            NbtCompound compound = NbtFactory.ofCompound("");

            for (int i = 0; i < LINES; i++) {
                WrappedChatComponent component;
                if (i < specific.length && specific[i] != null) {
                    component = specific[i];
                } else {
                    component = WrappedChatComponent.fromText("");
                }

                compound.put("Text" + (i + 1), component.getJson());
            }

            return nbtConverter.getGeneric(compound);
        }

        @Override
        public Class<WrappedChatComponent[]> getSpecificType() {
            return WrappedChatComponent[].class;
        }
    }

    /**
     * A delegated converter that supports NBT to Component Array and regular Component Array
     * @author dmulloy2
     */
    private static class ComponentArrayConverter implements EquivalentConverter<WrappedChatComponent[]> {
        private static final EquivalentConverter<WrappedChatComponent[]> DELEGATE;
        static {
            if (MinecraftReflection.signUpdateExists()) {
                DELEGATE = new LegacyComponentConverter();
            } else {
                DELEGATE = new NBTComponentConverter();
            }
        }

        @Override
        public WrappedChatComponent[] getSpecific(Object generic) {
            return DELEGATE.getSpecific(generic);
        }

        @Override
        public Object getGeneric(WrappedChatComponent[] specific) {
            return DELEGATE.getGeneric(specific);
        }

        @Override
        public Class<WrappedChatComponent[]> getSpecificType() {
            return DELEGATE.getSpecificType();
        }

        public static Class<?> getGenericType() {
            if (DELEGATE instanceof NBTComponentConverter) {
                return MinecraftReflection.getNBTCompoundClass();
            } else {
                return MinecraftReflection.getIChatBaseComponentArrayClass();
            }
        }
    }
}
