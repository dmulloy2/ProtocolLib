package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Wrapper for Entity Pose Enum, 1.13+
 * @author Lewys Davies (Lew_)
 */
public class WrappedEntityPose extends AbstractWrapper implements ClonableWrapper {
	private static final Class<?> NMS_ENTITY_POSE = MinecraftReflection.getNullableNMS("EntityPose");

	private static EquivalentConverter<EntityPose> POSE_CONVERTER;

	static {
		if (NMS_ENTITY_POSE != null) {
			POSE_CONVERTER = new EnumWrappers.FauxEnumConverter<>(EntityPose.class, NMS_ENTITY_POSE);
		}
	}

	public enum EntityPose {
		STANDING, FALL_FLYING, SLEEPING, SWIMMING, SPIN_ATTACK, CROUCHING, DYING;
	}

	private WrappedEntityPose(Object handle) {
        super(NMS_ENTITY_POSE);
        setHandle(handle);
    }

    public static WrappedEntityPose fromHandle(Object handle) {
        return new WrappedEntityPose(handle);
    }

    public static WrappedEntityPose fromValue(EntityPose pose) {
    	Object genericPose = POSE_CONVERTER.getGeneric(pose);
    	return new WrappedEntityPose(genericPose);
    }
	
	public EntityPose getEntityPose() {
		return POSE_CONVERTER.getSpecific(getHandle());
	}
	
	public void setEntityPose(EntityPose pose) {
		setHandle(POSE_CONVERTER.getGeneric(pose));
	}
	
	public static Class<?> getNmsClass() {
		return NMS_ENTITY_POSE;
	}

	@Override
	public WrappedEntityPose deepClone() {
		return new WrappedEntityPose(this.getEntityPose());
	}
}