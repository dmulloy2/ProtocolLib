package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;

import org.bukkit.util.Vector;

public class MovingObjectPositionBlock implements Cloneable {
	private static final Class<?> NMS_CLASS = MinecraftReflection.getNullableNMS(
			"world.phys.MovingObjectPositionBlock", "world.phys.BlockHitResult", "MovingObjectPositionBlock");

	private BlockPosition position;
	private Vector posVector;
	private Direction direction;
	private boolean insideBlock;

	public MovingObjectPositionBlock() { }

	public MovingObjectPositionBlock(BlockPosition position, Vector posVector, Direction direction, boolean insideBlock) {
		this.position = position;
		this.posVector = posVector;
		this.direction = direction;
		this.insideBlock = insideBlock;
	}

	public static Class<?> getNmsClass() {
		return NMS_CLASS;
	}

	public BlockPosition getBlockPosition() {
		return position;
	}

	public void setBlockPosition(BlockPosition position) {
		this.position = position;
	}

	public Vector getPosVector() {
		return posVector;
	}

	public void setPosVector(Vector vector) {
		this.posVector = vector;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public boolean isInsideBlock() {
		return insideBlock;
	}

	public void setInsideBlock(boolean insideBlock) {
		this.insideBlock = insideBlock;
	}

	private static ConstructorAccessor constructor;

	public static EquivalentConverter<MovingObjectPositionBlock> getConverter() {
		return Converters.ignoreNull(new EquivalentConverter<MovingObjectPositionBlock>() {
			@Override
			public Object getGeneric(MovingObjectPositionBlock specific) {
				if (constructor == null) {
					constructor = Accessors.getConstructorAccessor(NMS_CLASS,
							MinecraftReflection.getVec3DClass(),
							EnumWrappers.getDirectionClass(),
							MinecraftReflection.getBlockPositionClass(),
							boolean.class);
				}

				Object nmsVector = BukkitConverters.getVectorConverter().getGeneric(specific.posVector);
				Object nmsDirection = EnumWrappers.getDirectionConverter().getGeneric(specific.direction);
				Object nmsBlockPos = BlockPosition.getConverter().getGeneric(specific.position);

				return constructor.invoke(nmsVector, nmsDirection, nmsBlockPos, specific.insideBlock);
			}

			@Override
			public MovingObjectPositionBlock getSpecific(Object generic) {
				StructureModifier<Object> modifier = new StructureModifier<>(generic.getClass()).withTarget(generic);
				Direction direction = modifier.withType(EnumWrappers.getDirectionClass(), EnumWrappers.getDirectionConverter()).read(0);
				BlockPosition blockPos = modifier.withType(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter()).read(0);
				Vector posVector = modifier.withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter()).read(0);
				boolean insideBlock = (boolean) modifier.withType(boolean.class).read(1);
				return new MovingObjectPositionBlock(blockPos, posVector, direction, insideBlock);
			}

			@Override
			public Class<MovingObjectPositionBlock> getSpecificType() {
				return MovingObjectPositionBlock.class;
			}
		});
	}
}
