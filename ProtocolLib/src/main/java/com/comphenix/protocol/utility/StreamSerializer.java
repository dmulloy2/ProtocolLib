package com.comphenix.protocol.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.comphenix.protocol.compat.netty.Netty;
import com.comphenix.protocol.compat.netty.WrappedByteBuf;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.google.common.base.Preconditions;

/**
 * Utility methods for reading and writing Minecraft objects to streams.
 * 
 * @author Kristian
 */
public class StreamSerializer {
	private static final StreamSerializer DEFAULT = new StreamSerializer();
	
	// Cached methods
	private static MethodAccessor READ_ITEM_METHOD;
	private static MethodAccessor WRITE_ITEM_METHOD;

	private static MethodAccessor READ_NBT_METHOD;
	private static MethodAccessor WRITE_NBT_METHOD;
	
	private static MethodAccessor READ_STRING_METHOD;
	private static MethodAccessor WRITE_STRING_METHOD;
	
	/**
	 * Retrieve a default stream serializer.
	 * @return A serializer.
	 */
	public static StreamSerializer getDefault() {
		return DEFAULT;
	}

	/**
	 * Write a variable integer to an output stream.
	 * @param destination - the destination.
	 * @param value - the value to write.
	 * @throws IOException The destination stream threw an exception.
	 */
	public void serializeVarInt(@Nonnull DataOutputStream destination, int value) throws IOException {
		Preconditions.checkNotNull(destination, "source cannot be NULL");
		
		while ((value & 0xFFFFFF80) != 0) {
			destination.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}
		destination.writeByte(value);
	}

	/**
	 * Read a variable integer from an input stream.
	 * @param source - the source.
	 * @return The integer.
	 * @throws IOException The source stream threw an exception.
	 */
	public int deserializeVarInt(@Nonnull DataInputStream source) throws IOException {
		Preconditions.checkNotNull(source, "source cannot be NULL");
		
		int result = 0;
		int length = 0;
		byte currentByte;
		do {
			currentByte = source.readByte();
			result |= (currentByte & 0x7F) << length++ * 7;
			if (length > 5)
				throw new RuntimeException("VarInt too big");
		} while ((currentByte & 0x80) == 0x80);

		return result;
	}

	/**
	 * Write or serialize a NBT compound to the given output stream.
	 * <p>
	 * Note: An NBT compound can be written to a stream even if it's NULL.
	 * 
	 * @param output - the target output stream.
	 * @param compound - the NBT compound to be serialized, or NULL to represent nothing.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public void serializeCompound(@Nonnull DataOutputStream output, NbtCompound compound) throws IOException {
		if (output == null)
			throw new IllegalArgumentException("Output stream cannot be NULL.");
		
		// Get the NMS version of the compound
		Object handle = compound != null ? NbtFactory.fromBase(compound).getHandle() : null;
		
		if (MinecraftReflection.isUsingNetty()) {
			if (WRITE_NBT_METHOD == null) {
				WRITE_NBT_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("writeNbtCompound", /* a */
								MinecraftReflection.getNBTCompoundClass())
				);
			}

			WrappedByteBuf buf = Netty.packetWriter(output);
			buf.writeByte(NbtType.TAG_COMPOUND.getRawID());

			WRITE_NBT_METHOD.invoke(buf.getHandle(), handle);
		} else {
			if (WRITE_NBT_METHOD == null) {
				WRITE_NBT_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(2).
						parameterDerivedOf(MinecraftReflection.getNBTBaseClass(), 0).
						parameterDerivedOf(DataOutput.class, 1).
						returnTypeVoid().
						build())
				);
			}

			WRITE_NBT_METHOD.invoke(null, handle, output);
		}
	}

	/**
	 * Read or deserialize an NBT compound from a input stream.
	 * @param input - the target input stream.
	 * @return The resulting compound, or NULL.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public NbtCompound deserializeCompound(@Nonnull DataInputStream input) throws IOException {
		if (input == null)
			throw new IllegalArgumentException("Input stream cannot be NULL.");
		Object nmsCompound = null;
		
		// Invoke the correct method
		if (MinecraftReflection.isUsingNetty()) {
			if (READ_NBT_METHOD == null) {
				READ_NBT_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("readNbtCompound", /* h */
								MinecraftReflection.getNBTCompoundClass(), new Class<?>[0])
				);
			}

			nmsCompound = READ_NBT_METHOD.invoke(Netty.packetReader(input).getHandle());
		} else {
			if (READ_NBT_METHOD == null) {
				READ_NBT_METHOD = Accessors.getMethodAccessor(
				  FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(1).
					parameterDerivedOf(DataInput.class).
					returnDerivedOf(MinecraftReflection.getNBTBaseClass()).
					build())
				);
			}
			
			try {
				nmsCompound = READ_NBT_METHOD.invoke(null, input);
			} catch (Exception e) {
				throw new IOException("Cannot read item stack.", e);
			}
		}

		// Convert back to an NBT Compound
		if (nmsCompound != null)
			return NbtFactory.fromNMSCompound(nmsCompound);
		else
			return null;
	}

	/**
	 * Serialize a string using the standard Minecraft UTF-16 encoding.
	 * <p>
	 * Note that strings cannot exceed 32767 characters, regardless if maximum lenght.
	 * @param output - the output stream.
	 * @param text - the string to serialize.
	 * @throws IOException If the data in the string cannot be written.
	 */
	public void serializeString(@Nonnull DataOutputStream output, String text) throws IOException {
		if (output == null)
			throw new IllegalArgumentException("output stream cannot be NULL.");
		if (text == null)
			throw new IllegalArgumentException("text cannot be NULL.");
		
		if (MinecraftReflection.isUsingNetty()) {
			if (WRITE_STRING_METHOD == null) {
				WRITE_STRING_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("writeString", /* a */
								String.class)
				);
			}

			WRITE_STRING_METHOD.invoke(Netty.packetWriter(output).getHandle(), text);
		} else {
			if (WRITE_STRING_METHOD == null) {
				WRITE_STRING_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(2).
						parameterExactType(String.class, 0).
						parameterDerivedOf(DataOutput.class, 1).
						returnTypeVoid().
						build())
				);
			}

			WRITE_STRING_METHOD.invoke(null, text, output);
		}
	}
	
	/**
	 * Deserialize a string using the standard Minecraft UTF-16 encoding.
	 * <p>
	 * Note that strings cannot exceed 32767 characters, regardless if maximum length.
	 * @param input - the input stream.
	 * @param maximumLength - the maximum length of the string.
	 * @return The deserialized string.
	 * @throws IOException If deserializing fails
	 */
	public String deserializeString(@Nonnull DataInputStream input, int maximumLength) throws IOException {
		if (input == null)
			throw new IllegalArgumentException("Input stream cannot be NULL.");
		if (maximumLength > 32767)
			throw new IllegalArgumentException("Maximum length cannot exceed 32767 characters.");
		if (maximumLength < 0)
			throw new IllegalArgumentException("Maximum length cannot be negative.");
		
		if (MinecraftReflection.isUsingNetty()) {
			if (READ_STRING_METHOD == null) {
				READ_STRING_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("readString", /* c */
								String.class, new Class<?>[] { int.class })
				);
			}

			return (String) READ_STRING_METHOD.invoke(Netty.packetReader(input).getHandle(), maximumLength);
		} else {
			if (READ_STRING_METHOD == null) {
				READ_STRING_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(2).
						parameterDerivedOf(DataInput.class, 0).
						parameterExactType(int.class, 1).
						returnTypeExact(String.class).
						build())
					);
			}

			return (String) READ_STRING_METHOD.invoke(null, input, maximumLength);
		}
	}

	/**
	 * Serialize an item stack as a base-64 encoded string.
	 * <p>
	 * Note: An ItemStack can be written to the serialized text even if it's NULL.
	 * 
	 * @param stack - the item stack to serialize, or NULL to represent air/nothing.
	 * @return A base-64 representation of the given item stack.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public String serializeItemStack(ItemStack stack) throws IOException {
		Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);
		byte[] bytes = null;

		if (MinecraftReflection.isUsingNetty()) {
			WrappedByteBuf buf = Netty.buffer();
			Object serializer = MinecraftReflection.getPacketDataSerializer(buf.getHandle());

			if (WRITE_ITEM_METHOD == null) {
				WRITE_ITEM_METHOD = Accessors.getMethodAccessor(
						FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("writeStack", // a()
								MinecraftReflection.getItemStackClass()));
			}

			WRITE_ITEM_METHOD.invoke(serializer, nmsItem);

			bytes = buf.array();
		} else {
			if (WRITE_ITEM_METHOD == null) {
				WRITE_ITEM_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(2).
						parameterDerivedOf(MinecraftReflection.getItemStackClass(), 0).
						parameterDerivedOf(DataOutput.class, 1).
						build())
					);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutput = new DataOutputStream(outputStream);

			WRITE_ITEM_METHOD.invoke(null, nmsItem, dataOutput);

			bytes = outputStream.toByteArray();
		}

		return Base64Coder.encodeLines(bytes);
	}

	/**
	 * Deserialize an item stack from a base-64 encoded string.
	 * @param input - base-64 encoded string.
	 * @return A deserialized item stack, or NULL if the serialized ItemStack was also NULL.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public ItemStack deserializeItemStack(String input) throws IOException {
		Validate.notNull(input, "input cannot be null!");

		Object nmsItem = null;
		byte[] bytes = Base64Coder.decodeLines(input);

		if (MinecraftReflection.isUsingNetty()) {
			WrappedByteBuf buf = Netty.copiedBuffer(bytes);
			Object serializer = MinecraftReflection.getPacketDataSerializer(buf.getHandle());

			if (READ_ITEM_METHOD == null) {
				READ_ITEM_METHOD = Accessors.getMethodAccessor(FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("readItemStack", // i(ItemStack)
								MinecraftReflection.getItemStackClass(), new Class<?>[0]));
			}

			nmsItem = READ_ITEM_METHOD.invoke(serializer);
		} else {
			if (READ_ITEM_METHOD == null) {
				READ_ITEM_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(1).
						parameterDerivedOf(DataInput.class).
						returnDerivedOf(MinecraftReflection.getItemStackClass()).
						build())
					);
			}

			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			DataInputStream inputStream = new DataInputStream(byteStream);

			nmsItem = READ_ITEM_METHOD.invoke(null, inputStream);
		}

		return nmsItem != null ? MinecraftReflection.getBukkitItemStack(nmsItem) : null;
	}

	/**
	 * Write or serialize an item stack to the given output stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayOutputStream ByteArrayOutputStream}
	 * and {@link java.io.DataOutputStream DataOutputStream}.
	 * <p>
	 * Note: An ItemStack can be written to a stream even if it's NULL.
	 * 
	 * @param output - the target output stream.
	 * @param stack - the item stack that will be written, or NULL to represent air/nothing.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public void serializeItemStack(DataOutputStream output, ItemStack stack) throws IOException {
		Validate.notNull("output cannot be null!");
		
		// Get the NMS version of the ItemStack
		Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);
		
		if (MinecraftReflection.isUsingNetty()) {
			if (WRITE_ITEM_METHOD == null) {
				WRITE_ITEM_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("writeStack", /* a */
								MinecraftReflection.getItemStackClass())
				);
			}

			WrappedByteBuf buf = Netty.buffer();
			Object serializer = MinecraftReflection.getPacketDataSerializer(buf.getHandle());

			WRITE_ITEM_METHOD.invoke(serializer, nmsItem);

			output.write(buf.array());
		} else {
			if (WRITE_ITEM_METHOD == null)
				WRITE_ITEM_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(2).
						parameterDerivedOf(MinecraftReflection.getItemStackClass(), 0).
						parameterDerivedOf(DataOutput.class, 1).
						build())
			);

			WRITE_ITEM_METHOD.invoke(null, nmsItem, output);
		}
	}

	/**
	 * Read or deserialize an item stack from an underlying input stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayInputStream ByteArrayInputStream}
	 * and {@link java.io.DataInputStream DataInputStream}.
	 * 
	 * @param input - the target input stream.
	 * @return The resulting item stack, or NULL if the serialized item stack was NULL.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 * @deprecated This is a pretty hacky solution for backwards compatibility. See {@link #deserializeItemStack(DataInputStream)}
	 */
	@Deprecated
	public ItemStack deserializeItemStack(DataInputStream input) throws IOException {
		Validate.notNull(input, "input cannot be null!");
		Object nmsItem = null;
		
		if (MinecraftReflection.isUsingNetty()) {
			if (READ_ITEM_METHOD == null) {
				READ_ITEM_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true).
						getMethodByParameters("readItemStack", /* i */
								MinecraftReflection.getItemStackClass(), new Class<?>[0])
				);
			}

			byte[] bytes = new byte[8192];
			input.read(bytes);

			WrappedByteBuf buf = Netty.copiedBuffer(bytes);
			Object serializer = MinecraftReflection.getPacketDataSerializer(buf.getHandle());

			nmsItem = READ_ITEM_METHOD.invoke(serializer);
		} else {
			if (READ_ITEM_METHOD == null) {
				READ_ITEM_METHOD = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
						FuzzyMethodContract.newBuilder().
						parameterCount(1).
						parameterDerivedOf(DataInput.class).
						returnDerivedOf(MinecraftReflection.getItemStackClass()).
						build())
					);
			}

			nmsItem = READ_ITEM_METHOD.invoke(null, input);
		}
	
		// Convert back to a Bukkit item stack
		if (nmsItem != null)
			return MinecraftReflection.getBukkitItemStack(nmsItem);
		else
			return null;
	}
}