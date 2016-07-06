/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters.IgnoreNullConverter;

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

	public boolean equals(Object object) {
		if (object instanceof Vector3F) {
			Vector3F that = (Vector3F) object;
			return this.x == that.x && this.y == that.y && this.z == that.z;
		}

		return false;
	}

	private static Constructor<?> constructor = null;
	private static Class<?> clazz = MinecraftReflection.getMinecraftClass("Vector3f");

	public static Class<?> getMinecraftClass() {
		return clazz;
	}

	public static EquivalentConverter<Vector3F> getConverter() {
		return new IgnoreNullConverter<Vector3F>() {

			@Override
			public Class<Vector3F> getSpecificType() {
				return Vector3F.class;
			}

			@Override
			protected Object getGenericValue(Class<?> genericType, Vector3F specific) {
				if (constructor == null) {
					try {
						constructor = clazz.getConstructor(float.class, float.class, float.class);
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
			protected Vector3F getSpecificValue(Object generic) {
				StructureModifier<Float> modifier = new StructureModifier<Float>(generic.getClass())
						.withTarget(generic).withType(float.class);
				float x = modifier.read(0);
				float y = modifier.read(1);
				float z = modifier.read(2);
				return new Vector3F(x, y, z);
			}
		};
	}
}