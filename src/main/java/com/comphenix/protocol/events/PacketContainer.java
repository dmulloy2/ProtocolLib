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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.cloning.AggregateCloner;
import com.comphenix.protocol.reflect.cloning.AggregateCloner.BuilderParameters;
import com.comphenix.protocol.reflect.cloning.BukkitCloner;
import com.comphenix.protocol.reflect.cloning.Cloner;
import com.comphenix.protocol.reflect.cloning.CollectionCloner;
import com.comphenix.protocol.reflect.cloning.FieldCloner;
import com.comphenix.protocol.reflect.cloning.GuavaOptionalCloner;
import com.comphenix.protocol.reflect.cloning.ImmutableDetector;
import com.comphenix.protocol.reflect.cloning.JavaOptionalCloner;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.MinecraftGenerator;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.Converters;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import javax.annotation.Nullable;

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
	private static final Map<PacketType, Function<Object, Object>> PACKET_DESERIALIZER_METHODS = new ConcurrentHashMap<>();

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

		setDefaults();
	}

	private void setDefaults() {
		if (MinecraftVersion.NETHER_UPDATE.atOrAbove() && type == PacketType.Play.Server.CHAT) {
			if (!getUUIDs().optionRead(0).isPresent()) {
				getUUIDs().writeSafely(0, MinecraftGenerator.SYS_UUID);
			}
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
		Object handle = this.getHandle();
		PacketType packetType = this.getType();
		if (handle == null || packetType == null) {
			// nothing to clone, just carry on (this should normally not happen)
			return this;
		}

		// try fast cloning first
		if (!FAST_CLONE_UNSUPPORTED.contains(packetType)) {
			try {
				Object cloned = DEEP_CLONER.clone(handle);
				return new PacketContainer(packetType, cloned);
			} catch (Exception ex) {
				FAST_CLONE_UNSUPPORTED.add(packetType);
			}
		}

		// Fall back on the slower alternative method of reading and writing back the packet
		Object serialized = this.serializeToBuffer();
		Object deserialized = deserializeFromBuffer(packetType, serialized);

		// ensure that we don't leak memory
		ReferenceCountUtil.safeRelease(serialized);
		return new PacketContainer(packetType, deserialized);
	}

	// To save space, we'll skip copying the inflated buffers in packet 51 and 56
	private static com.google.common.base.Function<BuilderParameters, Cloner> getSpecializedDeepClonerFactory() {
		// Look at what you've made me do Java, look at it!!
		return new com.google.common.base.Function<BuilderParameters, Cloner>() {
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

		// serialize the packet
		ByteBuf buffer = (ByteBuf) this.serializeToBuffer();
		if (buffer != null) {
			output.writeBoolean(true);
			output.writeInt(buffer.readableBytes());
			buffer.readBytes(output, buffer.readableBytes());

			// ensure that we don't leak memory
			ReferenceCountUtil.safeRelease(buffer);
		} else {
			output.writeBoolean(false);
		}
	}

	private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
		// Default deserialization
		input.defaultReadObject();

		// Deserialize the packet from the stream (if present)
		this.structureModifier = StructureCache.getStructure(this.type);
		if (input.readBoolean()) {
			int dataLength = input.readInt();

			ByteBuf byteBuf = (ByteBuf) MinecraftReflection.createPacketDataSerializer(dataLength);
			while (true) {
				// ObjectInputStream only reads a specific amount of bytes before moving the cursor forwards and
				// allows reading the next byte chunk. So we need to read until the data is gone from the stream and
				// fully transferred into the buffer.
				int transferredBytes = byteBuf.writeBytes(input, dataLength);

				// check if we reached the end of the stream, or if the stream has no more data available
				dataLength -= transferredBytes;
				if (dataLength <= 0 || transferredBytes <= 0) {
					break;
				}
			}

			// deserialize & ensure that we don't leak memory
			Object packet = deserializeFromBuffer(this.type, byteBuf);
			ReferenceCountUtil.safeRelease(byteBuf);

			this.handle = packet;
			this.structureModifier = this.structureModifier.withTarget(packet);
		}
	}

	/**
	 * Construct a new packet data serializer.
	 * @return The packet data serializer.
	 * @deprecated use {@link MinecraftReflection#createPacketDataSerializer(int)} instead
	 */
	@Deprecated
	public static ByteBuf createPacketBuffer() {
		return (ByteBuf) MinecraftReflection.createPacketDataSerializer(0);
	}

	// ---- Cloning

	public static Object deserializeFromBuffer(PacketType packetType, Object buffer) {
		if (buffer == null) {
			return null;
		}

		Function<Object, Object> deserializer = PACKET_DESERIALIZER_METHODS.computeIfAbsent(packetType, type -> {
			if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
				// best guess - a constructor which takes a buffer as the only argument
				ConstructorAccessor bufferConstructor = Accessors.getConstructorAccessorOrNull(
						type.getPacketClass(),
						MinecraftReflection.getPacketDataSerializerClass());
				if (bufferConstructor != null) {
					return bufferConstructor::invoke;
				}

				// they might have a static method to create them instead
				List<Method> methods = FuzzyReflection.fromClass(type.getPacketClass(), true)
						.getMethodList(FuzzyMethodContract.newBuilder()
								.requireModifier(Modifier.STATIC)
								.returnTypeExact(type.getPacketClass())
								.parameterExactArray(MinecraftReflection.getPacketDataSerializerClass())
								.build());
				if (!methods.isEmpty()) {
					MethodAccessor accessor = Accessors.getMethodAccessor(methods.get(0));
					return buf -> accessor.invoke(null, buf);
				}
			}

			// try to construct a packet instance using a no-args constructor and invoke the read method
			MethodAccessor readMethod = MinecraftMethods.getPacketReadByteBufMethod();
			Objects.requireNonNull(readMethod,
					"Unable to find the Packet#read(ByteBuf) method, cannot deserialize " + type);

			Object checkInstance = DefaultInstances.DEFAULT.create(type.getPacketClass());
			Objects.requireNonNull(checkInstance, "Unable to construct empty packet, cannot deserialize " + type);

			// okay, Packet#read exists
			return buf -> {
				Object packet = DefaultInstances.DEFAULT.create(type.getPacketClass());
				readMethod.invoke(packet, buf);
				return packet;
			};
		});
		return deserializer.apply(buffer);
	}

	public Object serializeToBuffer() {
		Object handle = this.getHandle();
		if (handle == null) {
			return null;
		}

		Object targetBuffer = MinecraftReflection.createPacketDataSerializer(0);
		MinecraftMethods.getPacketWriteByteBufMethod().invoke(handle, targetBuffer);
		return targetBuffer;
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
