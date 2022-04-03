package com.comphenix.protocol.reflect.compiler;

import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.base.Objects;

final class StructureKey {

	private final Class<?> targetType;
	private final Class<?> fieldType;

	public StructureKey(Class<?> targetType, Class<?> fieldType) {
		this.targetType = targetType;
		this.fieldType = fieldType;
	}

	public static StructureKey forStructureModifier(StructureModifier<?> modifier) {
		return new StructureKey(modifier.getTargetType(), modifier.getFieldType());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.targetType, this.fieldType);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof StructureKey) {
			StructureKey other = (StructureKey) obj;
			return Objects.equal(this.targetType, other.targetType)
					&& Objects.equal(this.fieldType, other.fieldType);
		}

		return false;
	}
}
