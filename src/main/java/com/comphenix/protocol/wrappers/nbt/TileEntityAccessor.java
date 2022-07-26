package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.ByteBuddyFactory;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.block.BlockState;

/**
 * Manipulate tile entities.
 *
 * @author Kristian
 */
class TileEntityAccessor<T extends BlockState> {

	private static final boolean BLOCK_DATA_INCL = MinecraftVersion.NETHER_UPDATE.atOrAbove()
			&& !MinecraftVersion.CAVES_CLIFFS_1.atOrAbove();

	/**
	 * Token indicating that the given block state doesn't contain any tile entities.
	 */
	private static final TileEntityAccessor<BlockState> EMPTY_ACCESSOR = new TileEntityAccessor<>();

	/**
	 * Cached field accessors - {@link #EMPTY_ACCESSOR} represents no valid tile entity.
	 */
	private static final Map<Class<?>, TileEntityAccessor<?>> cachedAccessors = new HashMap<>();

	private static Constructor<?> nbtCompoundParserConstructor;

	private FieldAccessor tileEntityField;
	private MethodAccessor readCompound;
	private MethodAccessor writeCompound;

	TileEntityAccessor() {
		// Do nothing
	}

	/**
	 * Construct a new tile entity accessor.
	 *
	 * @param tileEntityField - the tile entity field.
	 * @param state           - the block state.
	 */
	private TileEntityAccessor(FieldAccessor tileEntityField, T state) {
		if (tileEntityField != null) {
			this.tileEntityField = tileEntityField;
			Class<?> type = tileEntityField.getField().getType();
			findMethods(type, state);
		}
	}

	/**
	 * Retrieve an accessor for the tile entity at a specific location.
	 *
	 * @param state - the block state.
	 * @return The accessor, or NULL if this block state doesn't contain any tile entities.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BlockState> TileEntityAccessor<T> getAccessor(T state) {
		Class<?> craftBlockState = state.getClass();
		TileEntityAccessor<?> accessor = cachedAccessors.get(craftBlockState);

		// Attempt to construct the accessor
		if (accessor == null) {
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

	void findMethods(Class<?> type, T state) {
		if (BLOCK_DATA_INCL) {
			Class<?> tileEntityClass = MinecraftReflection.getTileEntityClass();
			Class<?> iBlockData = MinecraftReflection.getIBlockDataClass();
			Class<?> nbtCompound = MinecraftReflection.getNBTCompoundClass();

			FuzzyReflection fuzzy = FuzzyReflection.fromClass(tileEntityClass, false);
			writeCompound = Accessors.getMethodAccessor(fuzzy.getMethod(
					FuzzyMethodContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.returnTypeVoid()
							.parameterExactArray(iBlockData, nbtCompound)
							.build()));

			// this'll point to 2 methods, one of which points to the other
			readCompound = Accessors.getMethodAccessor(fuzzy.getMethod(
					FuzzyMethodContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.returnTypeExact(nbtCompound)
							.parameterExactArray(nbtCompound)
							.build()));
		}

		// Possible read/write methods
		try {
			findMethodsUsingASM();
		} catch (IOException exception) {
			throw new RuntimeException("Cannot find read/write methods in " + type, exception);
		}

		// Ensure we found them
		if (readCompound == null) {
			throw new RuntimeException("Unable to find read method in " + type);
		}
		if (writeCompound == null) {
			throw new RuntimeException("Unable to find write method in " + type);
		}
	}

	/**
	 * Find the read/write methods in TileEntity.
	 *
	 * @throws IOException If we cannot find these methods.
	 */
	private void findMethodsUsingASM() throws IOException {
		Class<?> tileEntityClass = MinecraftReflection.getTileEntityClass();
		Class<?> nbtCompoundClass = MinecraftReflection.getNBTCompoundClass();

		// the expected method descriptor (NBTTagCompound): Any
		String tagCompoundName = Type.getInternalName(nbtCompoundClass);
		String expectedDesc = "(L" + tagCompoundName + ";)";

		// parse the tile entity class
		final ClassReader reader = new ClassReader(tileEntityClass.getCanonicalName());
		reader.accept(new ClassVisitor(Opcodes.ASM9) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				// Detect read/write calls to NBTTagCompound
				if (desc.startsWith(expectedDesc)) {
					return new MethodVisitor(Opcodes.ASM9) {
						// keep track of the amount of read/write calls to NBTTagCompound
						private int readMethods;
						private int writeMethods;

						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
							// This must be a virtual call on NBTTagCompound that accepts a String
							if (opcode == Opcodes.INVOKEVIRTUAL
									&& tagCompoundName.equals(owner)
									&& desc.startsWith("(Ljava/lang/String")) {
								// write calls return nothing, read calls do
								if (desc.endsWith(")V")) {
									this.writeMethods++;
								} else {
									this.readMethods++;
								}
							}
						}

						@Override
						public void visitEnd() {
							// more reads than writes? that is probably the read method then
							if (this.readMethods > this.writeMethods) {
								TileEntityAccessor.this.readCompound = Accessors.getMethodAccessor(
										tileEntityClass,
										name,
										nbtCompoundClass);
							} else if (this.writeMethods > this.readMethods) {
								TileEntityAccessor.this.writeCompound = Accessors.getMethodAccessor(
										tileEntityClass,
										name,
										nbtCompoundClass);
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
	 * Read the NBT compound that represents a given tile entity.
	 *
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
	 *
	 * @param state    - target block state.
	 * @param compound - the compound.
	 */
	public void writeBlockState(T state, NbtCompound compound) {
		Object tileEntity = tileEntityField.get(state);

		// Ensure the block state is set to the compound
		if (BLOCK_DATA_INCL) {
			Object blockData = BukkitUnwrapper.getInstance().unwrapItem(state);
			readCompound.invoke(tileEntity, blockData, NbtFactory.fromBase(compound).getHandle());
		} else {
			readCompound.invoke(tileEntity, NbtFactory.fromBase(compound).getHandle());
		}
	}
}
