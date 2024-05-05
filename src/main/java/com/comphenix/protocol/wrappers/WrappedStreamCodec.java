package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Wrapper for StreamCodec class which is primarily used to de-/serialize
 * packets since 1.20.5
 */
public class WrappedStreamCodec extends AbstractWrapper {

	// use the de-/encoder interfaces to get the right method to avoid future errors
	private static final Class<?> DECODER_TYPE = MinecraftReflection.getMinecraftClass("network.codec.StreamDecoder");
	private static final Class<?> ENCODER_TYPE = MinecraftReflection.getMinecraftClass("network.codec.StreamEncoder");

	private static final MethodAccessor DECODE_ACCESSOR;
	private static final MethodAccessor ENCODE_ACCESSOR;

	static {
		DECODE_ACCESSOR = Accessors.getMethodAccessor(FuzzyReflection.fromClass(DECODER_TYPE)
				.getMethodByReturnTypeAndParameters("decode", Object.class, new Class[] { Object.class }));

		ENCODE_ACCESSOR = Accessors.getMethodAccessor(FuzzyReflection.fromClass(ENCODER_TYPE)
				.getMethodByReturnTypeAndParameters("encode", Void.TYPE, new Class[] { Object.class, Object.class }));
	}

	public WrappedStreamCodec(Object handle) {
		super(MinecraftReflection.getStreamCodecClass());
		setHandle(handle);
	}

	public Object decode(Object buffer) {
		return DECODE_ACCESSOR.invoke(handle, buffer);
	}

	public void encode(Object buffer, Object value) {
		ENCODE_ACCESSOR.invoke(handle, buffer, value);
	}
}
