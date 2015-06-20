package com.comphenix.protocol.wrappers.nbt;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Opcodes;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.block.BlockState;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.compiler.EmptyClassVisitor;
import com.comphenix.protocol.reflect.compiler.EmptyMethodVisitor;
import com.comphenix.protocol.utility.EnhancerFactory;
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

	// For CGLib detection
	private boolean writeDetected;
	private boolean readDetected;

	private TileEntityAccessor() {
		// Do nothing
	}

	/**
	 * Construct a new tile entity accessor.
	 * @param tileEntityField - the tile entity field.
	 * @param state - the block state.
	 */
	private TileEntityAccessor(FieldAccessor tileEntityField, T state) {
		if (tileEntityField != null) {
			this.tileEntityField = tileEntityField;
			Class<?> type = tileEntityField.getField().getType();

			// Possible read/write methods
			try {
				findMethodsUsingASM();
			} catch (IOException ex1) {
				try {
					// Much slower though
					findMethodUsingCGLib(state);
				} catch (Exception ex2) {
					throw new RuntimeException("Cannot find read/write methods in " + type, ex2);
				}
			}

			// Ensure we found them
			if (readCompound == null)
				throw new RuntimeException("Unable to find read method in " + type);
			if (writeCompound == null)
				throw new RuntimeException("Unable to find write method in " + type);
		}
	}

	/**
	 * Find the read/write methods in TileEntity.
	 * @throws IOException If we cannot find these methods.
	 */
	private void findMethodsUsingASM() throws IOException {
		final Class<?> nbtCompoundClass = MinecraftReflection.getNBTCompoundClass();
		final Class<?> tileEntityClass = MinecraftReflection.getTileEntityClass();
		final ClassReader reader = new ClassReader(tileEntityClass.getCanonicalName());

		final String tagCompoundName = getJarName(MinecraftReflection.getNBTCompoundClass());
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

						@Override
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
	}

	/**
	 * Find the read/write methods in TileEntity.
	 * @param blockState - the block state.
	 * @throws IOException If we cannot find these methods.
	 */
	private void findMethodUsingCGLib(T blockState) throws IOException {
		final Class<?> nbtCompoundClass = MinecraftReflection.getNBTCompoundClass();

		// This is a much slower method, but it is necessary in MCPC
		Enhancer enhancer = EnhancerFactory.getInstance().createEnhancer();
		enhancer.setSuperclass(nbtCompoundClass);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (method.getReturnType().equals(Void.TYPE)) {
					// Write method
					writeDetected = true;
				} else {
					// Read method
					readDetected = true;
				}
				throw new RuntimeException("Stop execution.");
			}
		});
		Object compound = enhancer.create();
		Object tileEntity = tileEntityField.get(blockState);

		// Look in every read/write like method
		for (Method method : FuzzyReflection.fromObject(tileEntity, true).
				getMethodListByParameters(Void.TYPE, new Class<?>[] { nbtCompoundClass })) {

			try {
				readDetected = false;
				writeDetected = false;
				method.invoke(tileEntity, compound);
			} catch (Exception e) {
				// Okay - see if we detected a write or read
				if (readDetected)
					readCompound = Accessors.getMethodAccessor(method, true);
				if (writeDetected)
					writeCompound = Accessors.getMethodAccessor(method, true);
			}
		}
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
				created = new TileEntityAccessor<T>(field, state);
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
