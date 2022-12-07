/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.lang.reflect.Constructor;

/**
 * @author dmulloy2
 */
public class Vector3F {
	protected float x;
	protected float y;
	protected float z;

	public Vector3F() {
		this(0, 0, 0);
	}

	public Vector3F(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public Vector3F setX(float x) {
		this.x = x;
		return this;
	}

	public float getY() {
		return y;
	}

	public Vector3F setY(float y) {
		this.y = y;
		return this;
	}

	public float getZ() {
		return z;
	}

	public Vector3F setZ(float z) {
		this.z = z;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof Vector3F) {
			Vector3F that = (Vector3F) obj;
			if (Float.floatToIntBits(x) != Float.floatToIntBits(that.x))
				return false;
			if (Float.floatToIntBits(y) != Float.floatToIntBits(that.y))
				return false;
			if (Float.floatToIntBits(z) != Float.floatToIntBits(that.z))
				return false;
			return true;
		}

		return false;
	}

	private static Constructor<?> constructor = null;
	private static final Class<?> NMS_CLASS = MinecraftReflection.getNullableNMS("core.Vector3f", "core.Rotations", "Vector3f");

	public static Class<?> getMinecraftClass() {
		return NMS_CLASS;
	}

	public static EquivalentConverter<Vector3F> getConverter() {
		return Converters.ignoreNull(new EquivalentConverter<Vector3F>() {
			@Override
			public Class<Vector3F> getSpecificType() {
				return Vector3F.class;
			}

			@Override
			public Object getGeneric(Vector3F specific) {
				if (constructor == null) {
					try {
						constructor = NMS_CLASS.getConstructor(float.class, float.class, float.class);
					} catch (ReflectiveOperationException ex) {
						throw new RuntimeException("Failed to find constructor for Vector3f", ex);
					}
				}

				try {
					return constructor.newInstance(specific.x, specific.y, specific.z);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Failed to create new instance of Vector3f", ex);
				}
			}

			@Override
			public Vector3F getSpecific(Object generic) {
				StructureModifier<Float> modifier = new StructureModifier<Float>(generic.getClass())
						.withTarget(generic).withType(float.class);
				float x = modifier.read(0);
				float y = modifier.read(1);
				float z = modifier.read(2);
				return new Vector3F(x, y, z);
			}
		});
	}
}