/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.events;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.compat.netty.Netty;
import com.comphenix.protocol.compat.netty.WrappedByteBuf;
import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.cloning.AggregateCloner;
import com.comphenix.protocol.reflect.cloning.AggregateCloner.BuilderParameters;
import com.comphenix.protocol.reflect.cloning.BukkitCloner;
import com.comphenix.protocol.reflect.cloning.Cloner;
import com.comphenix.protocol.reflect.cloning.CollectionCloner;
import com.comphenix.protocol.reflect.cloning.FieldCloner;
import com.comphenix.protocol.reflect.cloning.ImmutableDetector;
import com.comphenix.protocol.reflect.cloning.SerializableCloner;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatVisibility;
import com.comphenix.protocol.wrappers.EnumWrappers.ClientCommand;
import com.comphenix.protocol.wrappers.EnumWrappers.CombatEventType;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.EnumWrappers.ResourcePackStatus;
import com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.EnumWrappers.WorldBorderAction;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.comphenix.protocol.wrappers.WrappedStatistic;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents a Minecraft packet indirectly.
 * 
 * @author Kristian
 */
public class PacketContainer implements Serializable {
	private static final long serialVersionUID = 3;
	
	protected PacketType type;
	protected transient Object handle;

	// Current structure modifier
	protected transient StructureModifier<Object> structureModifier;

	// Support for serialization
	private static ConcurrentMap<Class<?>, Method> writeMethods = Maps.newConcurrentMap();
	private static ConcurrentMap<Class<?>, Method> readMethods = Maps.newConcurrentMap();
	
	// Used to clone packets
	private static final AggregateCloner DEEP_CLONER = AggregateCloner.newBuilder().
			instanceProvider(DefaultInstances.DEFAULT).
			andThen(BukkitCloner.class).
			andThen(ImmutableDetector.class).
			andThen(CollectionCloner.class).
			andThen(getSpecializedDeepClonerFactory()).
			build();
	
	private static final AggregateCloner SHALLOW_CLONER = AggregateCloner.newBuilder().
			instanceProvider(DefaultInstances.DEFAULT).
			andThen(new Function<BuilderParameters, Cloner>() {
						@Override
						public Cloner apply(@Nullable BuilderParameters param) {
							if (param == null)
								throw new IllegalArgumentException("Cannot be NULL.");
							
							return new FieldCloner(param.getAggregateCloner(), param.getInstanceProvider()) {{
								// Use a default writer with no concept of cloning
								writer = new ObjectWriter();
							}};
						}
					}).
			build();
	
	// Packets that cannot be cloned by our default deep cloner
	private static final Set<PacketType> CLONING_UNSUPPORTED = Sets.newHashSet(
		PacketType.Play.Server.UPDATE_ATTRIBUTES, PacketType.Status.Server.OUT_SERVER_INFO);
	
	/**
	 * Creates a packet container for a new packet.
	 * <p>
	 * Deprecated: Use {@link #PacketContainer(PacketType)} instead.
	 * @param id - ID of the packet to create.
	 */
	@Deprecated
	public PacketContainer(int id) {
		this(PacketType.findLegacy(id), StructureCache.newPacket(PacketType.findLegacy(id)));
	}
	
	/**
	 * Creates a packet container for an existing packet.
	 * @param id - ID of the given packet.
	 * @param handle - contained packet.
	 * @deprecated Use {@link #PacketContainer(PacketType, Object)} instead
	 */
	@Deprecated
	public PacketContainer(int id, Object handle) {
		this(PacketType.findLegacy(id), handle);
	}
	
	/**
	 * Creates a packet container for an existing packet.
	 * @param id - ID of the given packet.
	 * @param handle - contained packet.
	 * @param structure - structure modifier.
	 * @deprecated Use {@link #PacketContainer(PacketType, Object, StructureModifier)} instead
	 */
	@Deprecated
	public PacketContainer(int id, Object handle, StructureModifier<Object> structure) {
		this(PacketType.findLegacy(id), handle, structure);
	}
	
	/**
	 * Creates a packet container for a new packet.
	 * @param type - the type of the packet to create.
	 */
	public PacketContainer(PacketType type) {
		this(type, StructureCache.newPacket(type));
	}
	
	/**
	 * Creates a packet container for an existing packet.
	 * @param type - Type of the given packet.
	 * @param handle - contained packet.
	 */
	public PacketContainer(PacketType type, Object handle) {
		this(type, handle, StructureCache.getStructure(type).withTarget(handle));
	}
	
	/**
	 * Creates a packet container for an existing packet.
	 * @param type - Type of the given packet.
	 * @param handle - contained packet.
	 * @param structure - structure modifier.
	 */
	public PacketContainer(PacketType type, Object handle, StructureModifier<Object> structure) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be null.");
		if (type == null)
			throw new IllegalArgumentException("type cannot be null.");
		
		this.type = type;
		this.handle = handle;
		this.structureModifier = structure;
	}
	
	/**
	 * Construct a new packet container from a given handle.
	 * @param packet - the NMS packet.
	 * @return The packet container.
	 */
	public static PacketContainer fromPacket(Object packet) {
		PacketType type = PacketType.fromClass(packet.getClass());
		return new PacketContainer(type, packet);
	}
	
	/**
	 * For serialization.
	 */
	protected PacketContainer() {
	}
	
	/**
	 * Retrieves the underlying Minecraft packet.
	 * @return Underlying Minecraft packet.
	 */
	public Object getHandle() {
		return handle;
	}
	
	/**
	 * Retrieves the generic structure modifier for this packet.
	 * @return Structure modifier.
	 */
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
	 * Retrieves a read/write structure for ItemStack.
	 * <p>
	 * This modifier will automatically marshall between the Bukkit ItemStack and the
	 * internal Minecraft ItemStack.
	 * @return A modifier for ItemStack fields.
	 */
	public StructureModifier<ItemStack> getItemModifier() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<ItemStack>withType(
				MinecraftReflection.getItemStackClass(), BukkitConverters.getItemStackConverter());
	}
	
	/**
	 * Retrieves a read/write structure for arrays of ItemStacks.
	 * <p>
	 * This modifier will automatically marshall between the Bukkit ItemStack and the
	 * internal Minecraft ItemStack.
	 * @return A modifier for ItemStack array fields.
	 */
	public StructureModifier<ItemStack[]> getItemArrayModifier() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<ItemStack[]>withType(
				MinecraftReflection.getItemStackArrayClass(),
				BukkitConverters.getIgnoreNull(new ItemStackArrayConverter()));
	}
	
	/**
	 * Retrieve a read/write structure for maps of statistics.
	 * <p>
	 * Note that you must write back the changed map to persist it.
	 * @return A modifier for maps of statistics.
	 */
	public StructureModifier<Map<WrappedStatistic, Integer>> getStatisticMaps() {
		return structureModifier.withType(Map.class,
			BukkitConverters.<WrappedStatistic, Integer>getMapConverter(
				MinecraftReflection.getStatisticClass(),
				BukkitConverters.getWrappedStatisticConverter()
			)
		);
	}
	
	/**
	 * Retrieves a read/write structure for the world type enum.
	 * <p>
	 * This modifier will automatically marshall between the Bukkit world type and the
	 * internal Minecraft world type.
	 * @return A modifier for world type fields.
	 */
	public StructureModifier<WorldType> getWorldTypeModifier() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<WorldType>withType(
				MinecraftReflection.getWorldTypeClass(),
				BukkitConverters.getWorldTypeConverter());
	}
	
	/**
	 * Retrieves a read/write structure for data watchers.
	 * @return A modifier for data watchers.
	 */
	public StructureModifier<WrappedDataWatcher> getDataWatcherModifier() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<WrappedDataWatcher>withType(
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
		return structureModifier.<Entity>withType(
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
	 * Retrieves a read/write structure for chunk positions.
	 * @return A modifier for a ChunkPosition.
	 */
	public StructureModifier<ChunkPosition> getPositionModifier() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.withType(
				MinecraftReflection.getChunkPositionClass(),
				ChunkPosition.getConverter());
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
	 * This modifier will automatically marshall between the visible ProtocolLib WrappedAttribute and the
	 * internal Minecraft AttributeSnapshot.
	 * @return A modifier for AttributeSnapshot collection fields.
	 */
	public StructureModifier<List<WrappedAttribute>> getAttributeCollectionModifier() {
		// Convert to and from the ProtocolLib wrapper
		return structureModifier.withType(
			Collection.class,
			BukkitConverters.getListConverter(
					MinecraftReflection.getAttributeSnapshotClass(),
					BukkitConverters.getWrappedAttributeConverter())
		);
	}
	
	/**
	 * Retrieves a read/write structure for collections of chunk positions.
	 * <p>
	 * This modifier will automatically marshall between the visible ProtocolLib ChunkPosition and the
	 * internal Minecraft ChunkPosition.
	 * 
	 * @return A modifier for ChunkPosition list fields.
	 */
	public StructureModifier<List<ChunkPosition>> getPositionCollectionModifier() {
		// Convert to and from the ProtocolLib wrapper
		return structureModifier.withType(
			Collection.class,
			BukkitConverters.getListConverter(
					MinecraftReflection.getChunkPositionClass(),
					ChunkPosition.getConverter())
		);
	}

	/**
	 * Retrieves a read/write structure for collections of chunk positions.
	 * <p>
	 * This modifier will automatically marshall between the visible ProtocolLib BlockPosition and the
	 * internal Minecraft BlockPosition.
	 *
	 * @return A modifier for ChunkPosition list fields.
	 */
	public StructureModifier<List<BlockPosition>> getBlockPositionCollectionModifier() {
		// Convert to and from the ProtocolLib wrapper
		return structureModifier.withType(
			Collection.class,
			BukkitConverters.getListConverter(
					MinecraftReflection.getBlockPositionClass(),
					BlockPosition.getConverter())
		);
	}

	/**
	 * Retrieves a read/write structure for collections of watchable objects.
	 * <p>
	 * This modifier will automatically marshall between the visible WrappedWatchableObject and the
	 * internal Minecraft WatchableObject.
	 * @return A modifier for watchable object list fields.
	 */
	public StructureModifier<List<WrappedWatchableObject>> getWatchableCollectionModifier() {
		// Convert to and from the ProtocolLib wrapper
		return structureModifier.withType(
			Collection.class,
			BukkitConverters.getListConverter(
					MinecraftReflection.getWatchableObjectClass(),
					BukkitConverters.getWatchableObjectConverter())
		);
	}
	
	
	/**
	 * Retrieves a read/write structure for block fields.
	 * <p>
	 * This modifier will automatically marshall between Material and the
	 * internal Minecraft Block.
	 * @return A modifier for GameProfile fields.
	 */
	public StructureModifier<Material> getBlocks() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<Material>withType(
				MinecraftReflection.getBlockClass(), BukkitConverters.getBlockConverter());
	}
	
	/**
	 * Retrieves a read/write structure for game profiles in Minecraft 1.7.2.
	 * <p>
	 * This modifier will automatically marshall between WrappedGameProfile and the
	 * internal Minecraft GameProfile.
	 * @return A modifier for GameProfile fields.
	 */
	public StructureModifier<WrappedGameProfile> getGameProfiles() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<WrappedGameProfile>withType(
				MinecraftReflection.getGameProfileClass(), BukkitConverters.getWrappedGameProfileConverter());
	}
	
	/**
	 * Retrieves a read/write structure for BlockData in Minecraft 1.8.
	 * <p>
	 * This modifier will automatically marshall between WrappedBlockData and the
	 * internal Minecraft IBlockData.
	 * @return A modifier for BlockData fields.
	 */
	public StructureModifier<WrappedBlockData> getBlockData() {
		// Convert to and from our wrapper
		return structureModifier.<WrappedBlockData>withType(
				MinecraftReflection.getIBlockDataClass(), BukkitConverters.getWrappedBlockDataConverter());
	}

	/**
	 * Retrieves a read/write structure for MultiBlockChangeInfo arrays in Minecraft 1.8.
	 * <p>
	 * This modifier will automatically marshall between MultiBlockChangeInfo and the
	 * internal Minecraft MultiBlockChangeInfo.
	 * @return A modifier for BlockData fields.
	 */
	public StructureModifier<MultiBlockChangeInfo[]> getMultiBlockChangeInfoArrays() {
		ChunkCoordIntPair chunk = getChunkCoordIntPairs().read(0);

		// Convert to and from our wrapper
		return structureModifier.<MultiBlockChangeInfo[]>withType(
				MinecraftReflection.getMultiBlockChangeInfoArrayClass(), MultiBlockChangeInfo.getArrayConverter(chunk));
	}
	
	/**
	 * Retrieves a read/write structure for chat components in Minecraft 1.7.2.
	 * <p>
	 * This modifier will automatically marshall between WrappedChatComponent and the
	 * internal Minecraft IChatBaseComponent.
	 * @return A modifier for ChatComponent fields.
	 */
	public StructureModifier<WrappedChatComponent> getChatComponents() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<WrappedChatComponent>withType(
				MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());
	}

	/**
	 * Retrieves a read/write structure for arrays of chat components.
	 * <p>
	 * This modifier will automatically marshall between WrappedChatComponent and the
	 * internal Minecraft IChatBaseComponent.
	 * @return A modifier for ChatComponent array fields.
	 */
	public StructureModifier<WrappedChatComponent[]> getChatComponentArrays() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<WrappedChatComponent[]>withType(
				MinecraftReflection.getIChatBaseComponentArrayClass(),
				BukkitConverters.getIgnoreNull(new WrappedChatComponentArrayConverter()));
	}
	
	/**
	 * Retrieve a read/write structure for the ServerPing fields in the following packet: <br>
	 * <ul>
	 *   <li>{@link PacketType.Status.Server#OUT_SERVER_INFO}
	 * </ul>
	 * @return A modifier for ServerPing fields.
	 */
	public StructureModifier<WrappedServerPing> getServerPings() {
		// Convert to and from the wrapper
		return structureModifier.<WrappedServerPing>withType(
				MinecraftReflection.getServerPingClass(), BukkitConverters.getWrappedServerPingConverter());
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
			BukkitConverters.getListConverter(
					MinecraftReflection.getPlayerInfoDataClass(),
					PlayerInfoData.getConverter())
		);
	}
	
	/**
	 * Retrieve a read/write structure for the Protocol enum in 1.7.2.
	 * @return A modifier for Protocol enum fields.
	 */
	public StructureModifier<Protocol> getProtocols() {
		// Convert to and from the wrapper
		return structureModifier.<Protocol>withType(
				EnumWrappers.getProtocolClass(), EnumWrappers.getProtocolConverter());
	}
	
	/**
	 * Retrieve a read/write structure for the ClientCommand enum in 1.7.2.
	 * @return A modifier for ClientCommand enum fields.
	 */
	public StructureModifier<ClientCommand> getClientCommands() {
		// Convert to and from the wrapper
		return structureModifier.<ClientCommand>withType(
				EnumWrappers.getClientCommandClass(), EnumWrappers.getClientCommandConverter());
	}

	/**
	 * Retrieve a read/write structure for the ChatVisibility enum in 1.7.2.
	 * @return A modifier for ChatVisibility enum fields.
	 */
	public StructureModifier<ChatVisibility> getChatVisibilities() {
		// Convert to and from the wrapper
		return structureModifier.<ChatVisibility>withType(
				EnumWrappers.getChatVisibilityClass(), EnumWrappers.getChatVisibilityConverter());
	}
	
	/**
	 * Retrieve a read/write structure for the Difficulty enum in 1.7.2.
	 * @return A modifier for Difficulty enum fields.
	 */
	public StructureModifier<Difficulty> getDifficulties() {
		// Convert to and from the wrapper
		return structureModifier.<Difficulty>withType(
				EnumWrappers.getDifficultyClass(), EnumWrappers.getDifficultyConverter());
	}
	
	/**
	 * Retrieve a read/write structure for the EntityUse enum in 1.7.2.
	 * @return A modifier for EntityUse enum fields.
	 */
	public StructureModifier<EntityUseAction> getEntityUseActions() {
		// Convert to and from the wrapper
		return structureModifier.<EntityUseAction>withType(
				EnumWrappers.getEntityUseActionClass(), EnumWrappers.getEntityUseActionConverter());
	}

	/**
	 * Retrieve a read/write structure for the NativeGameMode enum in 1.7.2.
	 * @return A modifier for NativeGameMode enum fields.
	 */
	public StructureModifier<NativeGameMode> getGameModes() {
		// Convert to and from the wrapper
		return structureModifier.<NativeGameMode>withType(
				EnumWrappers.getGameModeClass(), EnumWrappers.getGameModeConverter());
	}

	/**
	 * Retrieve a read/write structure for the ResourcePackStatus enum in 1.8.
	 * @return A modifier for ResourcePackStatus enum fields.
	 */
	public StructureModifier<ResourcePackStatus> getResourcePackStatus() {
		// Convert to and from the wrapper
		return structureModifier.<ResourcePackStatus>withType(
				EnumWrappers.getResourcePackStatusClass(), EnumWrappers.getResourcePackStatusConverter());
	}

	/**
	 * Retrieve a read/write structure for the PlayerInfo enum in 1.8.
	 * @return A modifier for PlayerInfoAction enum fields.
	 */
	public StructureModifier<PlayerInfoAction> getPlayerInfoAction() {
		// Convert to and from the wrapper
		return structureModifier.<PlayerInfoAction>withType(
				EnumWrappers.getPlayerInfoActionClass(), EnumWrappers.getPlayerInfoActionConverter());
	}

    /**
     * Retrieve a read/write structure for the TitleAction enum in 1.8.
     * @return A modifier for TitleAction enum fields.
     */
    public StructureModifier<TitleAction> getTitleActions() {
        // Convert to and from the wrapper
        return structureModifier.<TitleAction>withType(
                EnumWrappers.getTitleActionClass(), EnumWrappers.getTitleActionConverter());
    }
    
    /**
     * Retrieve a read/write structure for the WorldBorderAction enum in 1.8.
     * @return A modifier for WorldBorderAction enum fields.
     */
    public StructureModifier<WorldBorderAction> getWorldBorderActions() {
        // Convert to and from the wrapper
        return structureModifier.<WorldBorderAction>withType(
                EnumWrappers.getWorldBorderActionClass(), EnumWrappers.getWorldBorderActionConverter());
    }
    
    /**
     * Retrieve a read/write structure for the CombatEventType enum in 1.8.
     * @return A modifier for CombatEventType enum fields.
     */
    public StructureModifier<CombatEventType> getCombatEvents() {
        // Convert to and from the wrapper
        return structureModifier.<CombatEventType>withType(
                EnumWrappers.getCombatEventTypeClass(), EnumWrappers.getCombatEventTypeConverter());
    }
    
    /**
     * Retrieve a read/write structure for the PlayerDigType enum in 1.8.
     * @return A modifier for PlayerDigType enum fields.
     */
    public StructureModifier<PlayerDigType> getPlayerDigTypes() {
        // Convert to and from the wrapper
        return structureModifier.<PlayerDigType>withType(
                EnumWrappers.getPlayerDigTypeClass(), EnumWrappers.getPlayerDiggingActionConverter());
    }
    
    /**
     * Retrieve a read/write structure for the PlayerAction enum in 1.8.
     * @return A modifier for PlayerAction enum fields.
     */
    public StructureModifier<PlayerAction> getPlayerActions() {
        // Convert to and from the wrapper
        return structureModifier.<PlayerAction>withType(
                EnumWrappers.getPlayerActionClass(), EnumWrappers.getEntityActionConverter());
    }
    
    /**
     * Retrieve a read/write structure for the ScoreboardAction enum in 1.8.
     * @return A modifier for ScoreboardAction enum fields.
     */
    public StructureModifier<ScoreboardAction> getScoreboardActions() {
        // Convert to and from the wrapper
        return structureModifier.<ScoreboardAction>withType(
                EnumWrappers.getScoreboardActionClass(), EnumWrappers.getUpdateScoreActionConverter());
    }

    /**
     * Retrieve a read/write structure for the Particle enum in 1.8.
     * @return A modifier for Particle enum fields.
     */
    public StructureModifier<Particle> getParticles() {
    	// Convert to and from the wrapper
    	return structureModifier.<Particle>withType(
    			EnumWrappers.getParticleClass(), EnumWrappers.getParticleConverter());
    }

	/**
	 * Retrieves the ID of this packet.
	 * <p>
	 * Deprecated: Use {@link #getType()} instead.
	 * @return Packet ID.
	 */
	@Deprecated
	public int getID() {
		return type.getLegacyId();
	}
	
	/**
	 * Retrieve the packet type of this packet.
	 * @return The packet type.
	 */
	public PacketType getType() {
		return type;
	}
	
	/**
	 * Create a shallow copy of the current packet.
	 * <p>
	 * This merely writes the content of each field to the new class directly,
	 * without performing any expensive copies.
	 * 
	 * @return A shallow copy of the current packet.
	 */
	public PacketContainer shallowClone() {
		Object clonedPacket = SHALLOW_CLONER.clone(getHandle());
		return new PacketContainer(getType(), clonedPacket);
	}
	
	/**
	 * Create a deep copy of the current packet.
	 * <p>
	 * This will perform a full copy of the entire object tree, only skipping
	 * known immutable objects and primitive types.
	 * <p>
	 * Note that the inflated buffers in packet 51 and 56 will be copied directly to save memory.
	 * 
	 * @return A deep copy of the current packet.
	 */
	public PacketContainer deepClone() {
		Object clonedPacket = null;
		
		// Fall back on the alternative (but slower) method of reading and writing back the packet
		if (CLONING_UNSUPPORTED.contains(type)) {
			clonedPacket = SerializableCloner.clone(this).getHandle();
		} else {
			clonedPacket = DEEP_CLONER.clone(getHandle());
		}
		return new PacketContainer(getType(), clonedPacket);
	}
		
	// To save space, we'll skip copying the inflated buffers in packet 51 and 56
	private static Function<BuilderParameters, Cloner> getSpecializedDeepClonerFactory() {
		// Look at what you've made me do Java, look at it!!
		return new Function<BuilderParameters, Cloner>() {
			@Override
			public Cloner apply(@Nullable BuilderParameters param) {
				return new FieldCloner(param.getAggregateCloner(), param.getInstanceProvider()) {{
					this.writer = new ObjectWriter() {
						@Override
						protected void transformField(StructureModifier<Object> modifierSource,
													  StructureModifier<Object> modifierDest, int fieldIndex) {
							// No need to clone inflated buffers
							if (modifierSource.getField(fieldIndex).getName().startsWith("inflatedBuffer"))
								modifierDest.write(fieldIndex, modifierSource.read(fieldIndex));
							else
								defaultTransform(modifierSource, modifierDest, getDefaultCloner(), fieldIndex);
						};
					};
				}};
			}
		};
	}
	
	private void writeObject(ObjectOutputStream output) throws IOException {
	    // Default serialization
		output.defaultWriteObject();

		// We'll take care of NULL packets as well
		output.writeBoolean(handle != null);

		try {
			if (MinecraftReflection.isUsingNetty()) {
				WrappedByteBuf buffer = createPacketBuffer();
				MinecraftMethods.getPacketWriteByteBufMethod().invoke(handle, buffer.getHandle());

				output.writeInt(buffer.readableBytes());
				buffer.readBytes(output, buffer.readableBytes());
			} else {
				// Call the write-method
				output.writeInt(-1);
				getMethodLazily(writeMethods, handle.getClass(), "write", DataOutput.class).
					invoke(handle, new DataOutputStream(output));
			}
		
		} catch (IllegalArgumentException e) {
			throw new IOException("Minecraft packet doesn't support DataOutputStream", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Insufficient security privileges.", e);
		} catch (InvocationTargetException e) {
			throw new IOException("Could not serialize Minecraft packet.", e);
		}
	}

	private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
	    // Default deserialization
		input.defaultReadObject();
		
		// Get structure modifier
		structureModifier = StructureCache.getStructure(type);

	    // Don't read NULL packets
	    if (input.readBoolean()) {
	    	
	    	// Create a default instance of the packet
	    	handle = StructureCache.newPacket(type);
	    	
			// Call the read method
			try {
				if (MinecraftReflection.isUsingNetty()) {
					WrappedByteBuf buffer = createPacketBuffer();
					buffer.writeBytes(input, input.readInt());
					
					MinecraftMethods.getPacketReadByteBufMethod().invoke(handle, buffer.getHandle());
				} else {
					if (input.readInt() != -1)
						throw new IllegalArgumentException("Cannot load a packet from 1.7.2 in 1.6.4.");
					
					getMethodLazily(readMethods, handle.getClass(), "read", DataInput.class).
						invoke(handle, new DataInputStream(input));
				}
			} catch (IllegalArgumentException e) {
				throw new IOException("Minecraft packet doesn't support DataInputStream", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Insufficient security privileges.", e);
			} catch (InvocationTargetException e) {
				throw new IOException("Could not deserialize Minecraft packet.", e);
			}
			
			// And we're done
			structureModifier = structureModifier.withTarget(handle);
	    }
	}
	
	/**
	 * Construct a new packet data serializer.
	 * @return The packet data serializer.
	 */
	private WrappedByteBuf createPacketBuffer() {
		return Netty.createPacketBuffer();
	}

	// ---- Metadata
	// This map will only be initialized if it is actually used
	private Map<String, Object> metadata;

	/**
	 * Gets the metadata value for a given key.
	 * 
	 * @param key Metadata key
	 * @return Metadata value, or null if nonexistent.
	 */
	public Object getMetadata(String key) {
		if (metadata != null) {
			return metadata.get(key);
		}

		return null;
	}

	/**
	 * Adds metadata to this packet.
	 * <p>
	 * Note: Since metadata is lazily initialized, this may result in the creation of the metadata map.
	 * 
	 * @param key Metadata key
	 * @param value Metadata value
	 */
	public void addMetadata(String key, Object value) {
		if (metadata == null) {
			metadata = new HashMap<String, Object>();
		}

		metadata.put(key, value);
	}

	/**
	 * Removes metadata from this packet.
	 * <p>
	 * Note: If this operation leaves the metadata map empty, the map will be set to null.
	 * 
	 * @param key Metadata key
	 * @return The previous value, or null if nonexistant.
	 */
	public Object removeMetadata(String key) {
		if (metadata != null) {
			Object value = metadata.remove(key);
			if (metadata.isEmpty()) {
				metadata = null;
			}

			return value;
		}

		return null;
	}

	/**
	 * Whether or not this packet has metadata for a given key.
	 * 
	 * @param key Metadata key
	 * @return True if this packet has metadata for the key, false if not.
	 */
	public boolean hasMetadata(String key) {
		return metadata != null && metadata.containsKey(key);
	}

	/**
	 * Retrieve the cached method concurrently.
	 * @param lookup - a lazy lookup cache.
	 * @param handleClass - class type of the current packet.
	 * @param methodName - name of method to retrieve.
	 * @param parameterClass - the one parameter type in the method.
	 * @return Reflected method.
	 */
	private Method getMethodLazily(ConcurrentMap<Class<?>, Method> lookup,
								   Class<?> handleClass, String methodName, Class<?> parameterClass) {
		Method method = lookup.get(handleClass);
		
		// Atomic operation
		if (method == null) {
			Method initialized = FuzzyReflection.fromClass(handleClass).getMethod(
							FuzzyMethodContract.newBuilder().
							parameterCount(1).
							parameterDerivedOf(parameterClass).
							returnTypeVoid().
							build());
			method = lookup.putIfAbsent(handleClass, initialized);
			
			// Use our version if we succeeded
			if (method == null) {
				method = initialized;
			}
		}
		
		return method;
	}
	
	/**
	 * Represents an equivalent converter for ItemStack arrays.
	 * @author Kristian
	 */
	private static class ItemStackArrayConverter implements EquivalentConverter<ItemStack[]> {
		final EquivalentConverter<ItemStack> stackConverter = BukkitConverters.getItemStackConverter();
		
		@Override
		public Object getGeneric(Class<?>genericType, ItemStack[] specific) {
			Class<?> nmsStack = MinecraftReflection.getItemStackClass();
			Object[] result = (Object[]) Array.newInstance(nmsStack, specific.length);
			
			// Unwrap every item
			for (int i = 0; i < result.length; i++) {
				result[i] = stackConverter.getGeneric(nmsStack, specific[i]);
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
	private static class WrappedChatComponentArrayConverter implements EquivalentConverter<WrappedChatComponent[]> {
		final EquivalentConverter<WrappedChatComponent> componentConverter = BukkitConverters.getWrappedChatComponentConverter();
		
		@Override
		public Object getGeneric(Class<?>genericType, WrappedChatComponent[] specific) {
			Class<?> nmsComponent = MinecraftReflection.getIChatBaseComponentClass();
			Object[] result = (Object[]) Array.newInstance(nmsComponent, specific.length);
			
			// Unwrap every item
			for (int i = 0; i < result.length; i++) {
				result[i] = componentConverter.getGeneric(nmsComponent, specific[i]);
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

	@Override
	public String toString() {
		return "PacketContainer[type=" + type + ", structureModifier=" + structureModifier + "]";
	}
}