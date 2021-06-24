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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.*;
import com.comphenix.protocol.reflect.cloning.*;
import com.comphenix.protocol.reflect.cloning.AggregateCloner.BuilderParameters;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.*;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

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

/**
 * Represents a Minecraft packet indirectly.
 * 
 * @author Kristian
 */
@SuppressWarnings("unused")
public class PacketContainer extends AbstractStructure implements Serializable {
	private static final long serialVersionUID = 3;

	private PacketType type;

	// Support for serialization
	private static ConcurrentMap<Class<?>, Method> writeMethods = Maps.newConcurrentMap();
	private static ConcurrentMap<Class<?>, Method> readMethods = Maps.newConcurrentMap();

	// Used to clone packets
	private static final AggregateCloner DEEP_CLONER = AggregateCloner
			.newBuilder()
			.instanceProvider(StructureCache::newPacket)
			.andThen(BukkitCloner.class)
			.andThen(ImmutableDetector.class)
			.andThen(JavaOptionalCloner.class)
			.andThen(GuavaOptionalCloner.class)
			.andThen(CollectionCloner.class)
			.andThen(getSpecializedDeepClonerFactory())
			.build();
	
	private static final AggregateCloner SHALLOW_CLONER = AggregateCloner
			.newBuilder()
			.instanceProvider(StructureCache::newPacket)
			.andThen(param -> {
				if (param == null)
					throw new IllegalArgumentException("Cannot be NULL.");

				return new FieldCloner(param.getAggregateCloner(), param.getInstanceProvider()) {{
					// Use a default writer with no concept of cloning
					writer = new ObjectWriter();
				}};
			})
			.build();
	
	// Packets that cannot be cloned by our default deep cloner
	private static final Set<PacketType> FAST_CLONE_UNSUPPORTED = Sets.newHashSet(
		PacketType.Play.Server.BOSS,
		PacketType.Play.Server.ADVANCEMENTS,
		PacketType.Play.Client.USE_ENTITY,
		PacketType.Status.Server.SERVER_INFO
	);

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
		super(handle, structure);
		
		this.type = type;

		if (type == PacketType.Play.Server.CHAT) {
			getUUIDs().writeSafely(0, new UUID(0L, 0L));
		}
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

	public StructureModifier<InternalStructure> getStructures() {
		return structureModifier.withType(Object.class, InternalStructure.CONVERTER);
	}

	public StructureModifier<Optional<InternalStructure>> getOptionalStructures() {
		return structureModifier.withType(Optional.class, Converters.optional(InternalStructure.CONVERTER));
	}

	/**
	 * @deprecated Packet IDs are unreliable
	 */
	@Deprecated
	public int getId() {
    	return type.getCurrentId();
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

		if (!FAST_CLONE_UNSUPPORTED.contains(type)) {
			try {
				clonedPacket = DEEP_CLONER.clone(getHandle());
			} catch (Exception ex) {
				FAST_CLONE_UNSUPPORTED.add(type);
			}
		}

		// Fall back on the slower alternative method of reading and writing back the packet
		if (clonedPacket == null) {
			clonedPacket = SerializableCloner.clone(this).getHandle();
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
			ByteBuf buffer = createPacketBuffer();
			MinecraftMethods.getPacketWriteByteBufMethod().invoke(handle, buffer);

			output.writeInt(buffer.readableBytes());
			buffer.readBytes(output, buffer.readableBytes());
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
			ByteBuf buffer = createPacketBuffer();
			buffer.writeBytes(input, input.readInt());
	    	
	    	// Create a default instance of the packet
			if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
				Object serializer = MinecraftReflection.getPacketDataSerializer(buffer);

				try {
					handle = type.getPacketClass()
							.getConstructor(MinecraftReflection.getPacketDataSerializerClass())
							.newInstance(serializer);
				} catch (ReflectiveOperationException ex) {
					// they might have a static method to create them instead
					Method method = FuzzyReflection.fromClass(type.getPacketClass(), true)
							.getMethod(FuzzyMethodContract
									.newBuilder()
									.requireModifier(Modifier.STATIC)
									.returnTypeExact(type.getPacketClass())
									.parameterExactArray(MinecraftReflection.getPacketDataSerializerClass())
									.build());
					try {
						handle = method.invoke(null, serializer);
					} catch (ReflectiveOperationException ignored) {
						throw new RuntimeException("Failed to construct packet for " + type, ex);
					}
				}
			} else {
				handle = StructureCache.newPacket(type);

				// Call the read method
				try {
					MinecraftMethods.getPacketReadByteBufMethod().invoke(handle, buffer);
				} catch (IllegalArgumentException e) {
					throw new IOException("Minecraft packet doesn't support DataInputStream", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Insufficient security privileges.", e);
				} catch (InvocationTargetException e) {
					throw new IOException("Could not deserialize Minecraft packet.", e);
				}
			}
			
			// And we're done
			structureModifier = structureModifier.withTarget(handle);
	    }
	}
	
	/**
	 * Construct a new packet data serializer.
	 * @return The packet data serializer.
	 */
	public static ByteBuf createPacketBuffer() {
		ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
		Class<?> packetSerializer = MinecraftReflection.getPacketDataSerializerClass();

		try {
			return (ByteBuf) packetSerializer.getConstructor(ByteBuf.class).newInstance(buffer);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct packet serializer.", e);
		}
	}

	// ---- Metadata

	/**
	 * Gets the metadata value for a given key if it exists. Packet metadata expires after a minute, which is far longer
	 * than a packet will ever be held in processing.
	 *
	 * @param key Metadata key
	 * @param <T> Metadata type
	 * @return The metadata value, or an empty optional
	 */
	public <T> Optional<T> getMeta(String key) {
		return PacketMetadata.get(handle, key);
	}

	/**
	 * Sets the metadata value at a given key. Packet metadata expires after a minute, which is far longer than a packet
	 * will ever be held in processing.
	 *
	 * @param key Metadata key
	 * @param value Metadata value
	 * @param <T> Metadata type
	 */
	public <T> void setMeta(String key, T value) {
		PacketMetadata.set(handle, key, value);
	}

	/**
	 * Removes the metadata for a given key if it exists.
	 * @param key Key to remove meta for
	 */
	public void removeMeta(String key) {
		PacketMetadata.remove(handle, key);
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

	@Override
	public String toString() {
		return "PacketContainer[type=" + type + ", structureModifier=" + structureModifier + "]";
	}
}
