package com.comphenix.protocol.utility;

import com.comphenix.protocol.injector.netty.NettyByteBufAdapter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;

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
	 *
	 * @return A serializer.
	 */
	public static StreamSerializer getDefault() {
		return DEFAULT;
	}

	/**
	 * Write a variable integer to an output stream.
	 *
	 * @param destination - the destination.
	 * @param value       - the value to write.
	 * @throws IOException The destination stream threw an exception.
	 */
	public void serializeVarInt(DataOutputStream destination, int value) throws IOException {
		while (true) {
			if ((value & ~0x7F) == 0) {
				destination.writeByte(value);
				break;
			} else {
				destination.writeByte((value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	/**
	 * Read a variable integer from an input stream.
	 *
	 * @param source - the source.
	 * @return The integer.
	 * @throws IOException The source stream threw an exception.
	 */
	public int deserializeVarInt(DataInputStream source) throws IOException {
		int result = 0;
		for (byte j = 0; j < 5; j++) {
			int nextByte = source.readByte();
			result |= (nextByte & 0x7F) << j * 7;
			if ((nextByte & 0x80) != 128) {
				return result;
			}
		}
		throw new RuntimeException("VarInt is too big");
	}

	/**
	 * Write or serialize a NBT compound to the given output stream.
	 * <p>
	 * Note: An NBT compound can be written to a stream even if it's NULL.
	 *
	 * @param output   - the target output stream.
	 * @param compound - the NBT compound to be serialized, or NULL to represent nothing.
	 */
	public void serializeCompound(DataOutputStream output, NbtCompound compound) {
		if (WRITE_NBT_METHOD == null) {
			WRITE_NBT_METHOD = Accessors.getMethodAccessor(FuzzyReflection
					.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true)
					.getMethodByParameters("writeNbtCompound", MinecraftReflection.getNBTCompoundClass()));
		}

		ByteBuf buf = NettyByteBufAdapter.packetWriter(output);
		buf.writeByte(NbtType.TAG_COMPOUND.getRawID());

		// Get the NMS version of the compound
		Object handle = compound != null ? NbtFactory.fromBase(compound).getHandle() : null;
		WRITE_NBT_METHOD.invoke(buf, handle);
	}

	/**
	 * Read or deserialize an NBT compound from a input stream.
	 *
	 * @param input - the target input stream.
	 * @return The resulting compound, or NULL.
	 */
	public NbtCompound deserializeCompound(DataInputStream input) {
		if (READ_NBT_METHOD == null) {
			READ_NBT_METHOD = Accessors.getMethodAccessor(FuzzyReflection
					.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true)
					.getMethodByReturnTypeAndParameters("readNbtCompound", MinecraftReflection.getNBTCompoundClass()));
		}

		ByteBuf buf = NettyByteBufAdapter.packetReader(input);

		// deserialize and wrap if needed
		Object nmsCompound = READ_NBT_METHOD.invoke(buf);
		return nmsCompound == null ? null : NbtFactory.fromNMSCompound(nmsCompound);
	}

	/**
	 * Serialize a string using the standard Minecraft UTF-16 encoding.
	 * <p>
	 * Note that strings cannot exceed 32767 characters, regardless if maximum lenght.
	 *
	 * @param output - the output stream.
	 * @param text   - the string to serialize.
	 */
	public void serializeString(DataOutputStream output, String text) {
		if (WRITE_STRING_METHOD == null) {
			WRITE_STRING_METHOD = Accessors.getMethodAccessor(FuzzyReflection
					.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true)
					.getMethodByParameters("writeString", String.class));
		}

		ByteBuf buf = NettyByteBufAdapter.packetWriter(output);
		WRITE_STRING_METHOD.invoke(buf, text);
	}

	/**
	 * Deserialize a string using the standard Minecraft UTF-16 encoding.
	 * <p>
	 * Note that strings cannot exceed 32767 characters, regardless if maximum length.
	 *
	 * @param input         - the input stream.
	 * @param maximumLength - the maximum length of the string.
	 * @return The deserialized string.
	 */
	public String deserializeString(DataInputStream input, int maximumLength) {
		if (READ_STRING_METHOD == null) {
			READ_STRING_METHOD = Accessors.getMethodAccessor(FuzzyReflection
					.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true)
					.getMethodByReturnTypeAndParameters("readString", String.class, int.class));
		}

		ByteBuf buf = NettyByteBufAdapter.packetReader(input);
		return (String) READ_STRING_METHOD.invoke(buf, maximumLength);
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
		return Base64.getMimeEncoder().encodeToString(this.serializeItemStackToByteArray(stack));
	}

	/**
	 * Deserialize an item stack from a base-64 encoded string.
	 *
	 * @param input - base-64 encoded string.
	 * @return A deserialized item stack, or NULL if the serialized ItemStack was also NULL.
	 */
	public ItemStack deserializeItemStack(String input) {
		return this.deserializeItemStackFromByteArray(Base64.getMimeDecoder().decode(input));
	}

	/**
	 * Serialize an item stack as byte array.
	 * <p>
	 * Note: An ItemStack can be written to the serialized text even if it's NULL.
	 *
	 * @param stack - the item stack to serialize, or NULL to represent air/nothing.
	 * @return A binary representation of the given item stack.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public byte[] serializeItemStackToByteArray(ItemStack stack) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); DataOutputStream data = new DataOutputStream(out)) {
			this.serializeItemStack(data, stack);
			return out.toByteArray();
		}
	}

	/**
	 * Deserialize an item stack from a byte array.
	 *
	 * @param input - serialized item.
	 * @return A deserialized item stack, or NULL if the serialized ItemStack was also NULL.
	 */
	public ItemStack deserializeItemStackFromByteArray(byte[] input) {
		if (READ_ITEM_METHOD == null) {
			READ_ITEM_METHOD = Accessors.getMethodAccessor(FuzzyReflection
					.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true)
					.getMethodByReturnTypeAndParameters("readItemStack", MinecraftReflection.getItemStackClass()));
		}

		ByteBuf buf = Unpooled.wrappedBuffer(input);
		Object serializer = MinecraftReflection.getPacketDataSerializer(buf);

		try {
			// unwrap the item
			Object nmsItem = READ_ITEM_METHOD.invoke(serializer);
			return nmsItem != null ? MinecraftReflection.getBukkitItemStack(nmsItem) : null;
		} finally {
			ReferenceCountUtil.safeRelease(buf);
		}
	}

	/**
	 * Write or serialize an item stack to the given output stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayOutputStream ByteArrayOutputStream} and {@link
	 * java.io.DataOutputStream DataOutputStream}.
	 * <p>
	 * Note: An ItemStack can be written to a stream even if it's NULL.
	 *
	 * @param output - the target output stream.
	 * @param stack  - the item stack that will be written, or NULL to represent air/nothing.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public void serializeItemStack(DataOutputStream output, ItemStack stack) throws IOException {
		if (WRITE_ITEM_METHOD == null) {
			WRITE_ITEM_METHOD = Accessors.getMethodAccessor(FuzzyReflection
					.fromClass(MinecraftReflection.getPacketDataSerializerClass(), true)
					.getMethodByParameters("writeStack", MinecraftReflection.getItemStackClass()));
		}

		ByteBuf buf = Unpooled.buffer();
		Object serializer = MinecraftReflection.getPacketDataSerializer(buf);

		// Get the NMS version of the ItemStack and write it into the buffer
		Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);
		WRITE_ITEM_METHOD.invoke(serializer, nmsItem);

		// write the serialized content to the stream
		output.write(this.getBytesAndRelease(buf));
	}

	public byte[] getBytesAndRelease(ByteBuf buf) {
		try {
			if (buf.hasArray()) {
				// heap buffer, we can access the array directly
				return buf.array();
			} else {
				// direct buffer, we need to copy the bytes into an array
				byte[] bytes = new byte[buf.readableBytes()];
				buf.readBytes(bytes);
				return bytes;
			}
		} finally {
			ReferenceCountUtil.safeRelease(buf);
		}
	}
}
