package com.comphenix.protocol.wrappers.nbt;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Opcodes;

import org.bukkit.block.BlockState;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.compiler.EmptyClassVisitor;
import com.comphenix.protocol.reflect.compiler.EmptyMethodVisitor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

/**
 * Manipulate tile entities.
 * @author Kristian
 */
class TileEntityAccessor<T extends BlockState> {
	/**
	 * Token indicating that the given block state doesn't contany any tile entities.
	 */
	private static final TileEntityAccessor<BlockState> EMPTY_ACCESSOR = new TileEntityAccessor<BlockState>();
	
	/**
	 * Cached field accessors - {@link #EMPTY_ACCESSOR} represents no valid tile entity.
	 */
	private static final ConcurrentMap<Class<?>, TileEntityAccessor<?>> cachedAccessors = Maps.newConcurrentMap();
	
	private FieldAccessor tileEntityField;
	private MethodAccessor readCompound;
	private MethodAccessor writeCompound;
	
	private TileEntityAccessor() {
		// Do nothing
	}
	
	/**
	 * Construct a new tile entity accessor.
	 * @param tileEntityField - the tile entity field.
	 * @param tileEntity - the tile entity.
	 * @throws IOException Cannot read tile entity.
	 */
	private TileEntityAccessor(FieldAccessor tileEntityField) {
		if (tileEntityField != null) {
			this.tileEntityField = tileEntityField;
			
			// Possible read/write methods
			try {
				findSerializationMethods(tileEntityField.getField().getType());
			} catch (IOException e) {
				throw new RuntimeException("Cannot find read/write methods.", e);
			}
		}
	}

	/**
	 * Find the read/write methods in TileEntity.
	 * @param tileEntityClass - the tile entity class.
	 * @param nbtCompoundClass - the compound clas.
	 * @throws IOException If we cannot find these methods.
	 */
	private void findSerializationMethods(final Class<?> tileEntityClass) throws IOException {
		final Class<?> nbtCompoundClass = MinecraftReflection.getNBTCompoundClass();
		
		final ClassReader reader = new ClassReader(tileEntityClass.getCanonicalName());
		final String tagCompoundName = getJarName(NBTTagCompound.class);
		final String expectedDesc = "(L" + tagCompoundName + ";)V";
		
		reader.accept(new EmptyClassVisitor() {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				final String methodName = name;
				
				// Detect read/write calls to NBTTagCompound
				if (expectedDesc.equals(desc)) {
					return new EmptyMethodVisitor() {
						private int readMethods;
						private int writeMethods;
						
						public void visitMethodInsn(int opcode, String owner, String name, String desc) {
							// This must be a virtual call on NBTTagCompound that accepts a String
							if (opcode == Opcodes.INVOKEVIRTUAL && 
								tagCompoundName.equals(owner) && 
								desc.startsWith("(Ljava/lang/String")) {
								
								// Is this a write call?
								if (desc.endsWith(")V")) {
									writeMethods++;
								} else {
									readMethods++;
								}
							} 							
						}
						
						@Override
						public void visitEnd() {
							if (readMethods > writeMethods) {
								readCompound = Accessors.getMethodAccessor(tileEntityClass, methodName, nbtCompoundClass);
							} else if (writeMethods > readMethods) {
								writeCompound = Accessors.getMethodAccessor(tileEntityClass, methodName, nbtCompoundClass);
							}
							super.visitEnd();
						}
					};
				}
				return null;
			}
		}, 0);
		
		// Ensure we found them
		if (readCompound == null)
			throw new RuntimeException("Unable to find read method in " + tileEntityClass);
		if (writeCompound == null)
			throw new RuntimeException("Unable to find write method in " + tileEntityClass);
	}
	
	/**
	 * Retrieve the JAR name (slash instead of dots) of the given clas.
	 * @param clazz - the class.
	 * @return The JAR name.
	 */
	private static String getJarName(Class<?> clazz) {
		return clazz.getCanonicalName().replace('.', '/');
	}
	
	/**
	 * Read the NBT compound that represents a given tile entity.
	 * @param state - tile entity represented by a block state.
	 * @return The compound.
	 */
	public NbtCompound readBlockState(T state) {
		NbtCompound output = NbtFactory.ofCompound("");
		Object tileEntity = tileEntityField.get(state);
		
		// Write the block state to the output compound
		writeCompound.invoke(tileEntity, NbtFactory.fromBase(output).getHandle());
		return output;
	}
	
	/**
	 * Write the NBT compound as a tile entity.
	 * @param state - target block state.
	 * @param compound - the compound.
	 */
	public void writeBlockState(T state, NbtCompound compound) {
		Object tileEntity = tileEntityField.get(state);
		
		// Ensure the block state is set to the compound
		readCompound.invoke(tileEntity, NbtFactory.fromBase(compound).getHandle());
	}
	
	/**
	 * Retrieve an accessor for the tile entity at a specific location.
	 * @param state - the block state.
	 * @return The accessor, or NULL if this block state doesn't contain any tile entities.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BlockState> TileEntityAccessor<T> getAccessor(T state) {
		Class<?> craftBlockState = state.getClass();
		TileEntityAccessor<?> accessor = cachedAccessors.get(craftBlockState);
		
		// Attempt to construct the accessor
		if (accessor == null ) {
			TileEntityAccessor<?> created = null;
			FieldAccessor field = null;
			
			try {
				field = Accessors.getFieldAccessor(craftBlockState, MinecraftReflection.getTileEntityClass(), true);
			} catch (Exception e) {
				created = EMPTY_ACCESSOR;
			} 
			if (field != null) {
				created = new TileEntityAccessor<T>(field);
			}
			accessor = cachedAccessors.putIfAbsent(craftBlockState, created);
		
			// We won the race
			if (accessor == null) {
				accessor = created;
			}
		}
		return (TileEntityAccessor<T>) (accessor != EMPTY_ACCESSOR ? accessor : null);
	}
}
