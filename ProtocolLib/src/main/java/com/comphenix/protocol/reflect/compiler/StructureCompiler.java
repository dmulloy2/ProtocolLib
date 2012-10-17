/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.reflect.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.comphenix.protocol.reflect.PrimitiveUtils;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.base.Objects;

import net.sf.cglib.asm.*;

// public class CompiledStructureModifierPacket20<TField> extends CompiledStructureModifier<TField> {
//	
//		private Packet20NamedEntitySpawn typedTarget;
//	
//		public CompiledStructureModifierPacket20(StructureModifier<TField> other, StructureCompiler compiler) {
//			super();
//			initialize(other);
//			this.target = other.getTarget();
//			this.typedTarget = (Packet20NamedEntitySpawn) target;
//			this.compiler = compiler;
//		}
//	
//		@Override
//		protected Object readGenerated(int fieldIndex) throws FieldAccessException {
//	
//			Packet20NamedEntitySpawn target = typedTarget;
//			
//			switch (fieldIndex) {
//			case 0: return (Object) target.a;
//			case 1: return (Object) target.b;
//			case 2: return (Object) target.c;
//			case 3: return super.read(fieldIndex);
//			case 4: return super.read(fieldIndex);
//			case 5: return (Object) target.f;
//			case 6: return (Object) target.g;
//			case 7: return (Object) target.h;
//			default:
//				throw new FieldAccessException("Invalid index " + fieldIndex);
//			}
//		}
//	
//		@Override
//		protected StructureModifier<TField> writeGenerated(int index, Object value) throws FieldAccessException {
//			
//			Packet20NamedEntitySpawn target = typedTarget;
//			
//			switch (index) {
//			case 0: target.a = (Integer) value; break;
//			case 1: target.b = (String) value; break;
//			case 2: target.c = (Integer) value; break;
//			case 3: target.d = (Integer) value; break;
//			case 4: super.write(index, value); break;
//			case 5: super.write(index, value); break;
//			case 6: target.g = (Byte) value; break;
//			case 7: target.h = (Integer) value; break;
//			default:
//				throw new FieldAccessException("Invalid index " + index);
//			}
//			
//			// Chaining
//			return this;
//		}
//	}

/**
 * Represents a StructureModifier compiler.
 * 
 * @author Kristian
 */
public final class StructureCompiler {

	// Used to store generated classes of different types
	@SuppressWarnings("rawtypes")
	private class StructureKey {
		private Class targetType;
		private Class fieldType;
		
		public StructureKey(Class targetType, Class fieldType) {
			this.targetType = targetType;
			this.fieldType = fieldType;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(targetType, fieldType);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StructureKey) {
				StructureKey other = (StructureKey) obj;
				return Objects.equal(targetType, other.targetType) &&
					   Objects.equal(fieldType, other.fieldType);
			}
			return false;
		}
	}
	
	// Used to load classes
	private volatile static Method defineMethod;
	
	@SuppressWarnings("rawtypes")
	private Map<StructureKey, Class> compiledCache = new HashMap<StructureKey, Class>();
	
	// The class loader we'll store our classes
	private ClassLoader loader;
	
	// References to other classes
	private static String PACKAGE_NAME = "com/comphenix/protocol/reflect/compiler";
	private static String SUPER_CLASS = "com/comphenix/protocol/reflect/StructureModifier";
	private static String COMPILED_CLASS = PACKAGE_NAME + "/CompiledStructureModifier";
	private static String FIELD_EXCEPTION_CLASS = "com/comphenix/protocol/reflect/FieldAccessException";
	
	/**
	 * Construct a structure compiler.
	 * @param loader - main class loader.
	 */
	StructureCompiler(ClassLoader loader) {
		this.loader = loader;
	}
	
	/**
	 * Compiles the given structure modifier.
	 * <p>
	 * WARNING: Do NOT call this method in the main thread. Compiling may easily take 10 ms, which is already 
	 * over 1/4 of a tick (50 ms). Let the background thread automatically compile the structure modifiers instead.
	 * @param source - structure modifier to compile.
	 * @return A compiled structure modifier.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <TField> StructureModifier<TField> compile(StructureModifier<TField> source) {

		// We cannot optimize a structure modifier with no public fields
		if (!isAnyPublic(source.getFields())) {
			return source;
		}
		
		StructureKey key = new StructureKey(source.getTargetType(), source.getFieldType());
		Class<?> compiledClass = compiledCache.get(key);
		
		if (!compiledCache.containsKey(key)) {
			compiledClass = generateClass(source);
			compiledCache.put(key, compiledClass);
		}
		
		// Next, create an instance of this class
		try {
			return (StructureModifier<TField>) compiledClass.getConstructor(
					StructureModifier.class, StructureCompiler.class).
					 newInstance(source, this);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Used invalid parameters in instance creation", e);
		} catch (SecurityException e) {
			throw new RuntimeException("Security limitation!", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Error occured while instancing generated class.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Security limitation! Cannot create instance of dynamic class.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error occured while instancing generated class.", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Cannot happen.", e);
		}
	}
	
	private <TField> Class<?> generateClass(StructureModifier<TField> source) {
		
		ClassWriter cw = new ClassWriter(0);

		@SuppressWarnings("rawtypes")
		Class targetType = source.getTargetType();
		
		String className = "CompiledStructure$" + targetType.getSimpleName() + source.getFieldType().getSimpleName();
		String targetSignature = Type.getDescriptor(targetType);
		String targetName = targetType.getName().replace('.', '/');
		
		try {
			// This class might have been generated before. Try to load it.
			Class<?> before = loader.loadClass(PACKAGE_NAME.replace('/', '.') + "." + className);
			
			if (before != null)
				return before;
		} catch (ClassNotFoundException e) {
			// That's ok. 
		}
		
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, PACKAGE_NAME + "/" + className, 
				"<TField:Ljava/lang/Object;>L" + COMPILED_CLASS + "<TTField;>;", 
				COMPILED_CLASS, null);

		createFields(cw, targetSignature);
		createConstructor(cw, className, targetSignature, targetName);
		createReadMethod(cw, className, source.getFields(), targetSignature, targetName);
		createWriteMethod(cw, className, source.getFields(), targetSignature, targetName);
		cw.visitEnd();

		byte[] data = cw.toByteArray();
		
		// Call the define method
		try {
			if (defineMethod == null) {
				Method defined = ClassLoader.class.getDeclaredMethod("defineClass", 
					new Class<?>[] { String.class, byte[].class, int.class, int.class });
			
				// Awesome. Now, create and return it.
				defined.setAccessible(true);
				defineMethod = defined;
			}
			
			@SuppressWarnings("rawtypes")
			Class clazz = (Class) defineMethod.invoke(loader, null, data, 0, data.length);
			
			// DEBUG CODE: Print the content of the generated class.
			//org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(data);
	        //cr.accept(new ASMifierClassVisitor(new PrintWriter(System.out)), 0);
			
			return clazz;
			
		} catch (SecurityException e) {
			throw new RuntimeException("Cannot use reflection to dynamically load a class.", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Incompatible JVM.", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Cannot call defineMethod - wrong JVM?", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Security limitation! Cannot dynamically load class.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error occured in code generator.", e);
		}
	}
	
	/**
	 * Determine if at least one of the given fields is public.
	 * @param fields - field to test.
	 * @return TRUE if one or more field is publically accessible, FALSE otherwise.
	 */
	private boolean isAnyPublic(List<Field> fields) {
		// Are any of the fields public?
		for (int i = 0; i < fields.size(); i++) {
			if (isPublic(fields.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isPublic(Field field) {
		return Modifier.isPublic(field.getModifiers());
	}
	
	private void createFields(ClassWriter cw, String targetSignature) {
		FieldVisitor typedField = cw.visitField(Opcodes.ACC_PRIVATE, "typedTarget", targetSignature, null, null);
		typedField.visitEnd();
	}
	
	private void createWriteMethod(ClassWriter cw, String className, List<Field> fields, String targetSignature, String targetName) {
		
		String methodDescriptor = "(ILjava/lang/Object;)L" + SUPER_CLASS + ";";
		String methodSignature = "(ITTField;)L" + SUPER_CLASS + "<TTField;>;";
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "writeGenerated", methodDescriptor, methodSignature, 
									new String[] { FIELD_EXCEPTION_CLASS });
		BoxingHelper boxingHelper = new BoxingHelper(mv);
		
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, PACKAGE_NAME + "/" + className, "typedTarget", targetSignature);
		mv.visitVarInsn(Opcodes.ASTORE, 3);
		mv.visitVarInsn(Opcodes.ILOAD, 1);

		// The last label is for the default switch
		Label[] labels = new Label[fields.size()];
		Label errorLabel = new Label();
		Label returnLabel = new Label();
		
		// Generate labels
		for (int i = 0; i < fields.size(); i++) {
			labels[i] = new Label();
		}
		
		mv.visitTableSwitchInsn(0, labels.length - 1, errorLabel, labels);
		
		for (int i = 0; i < fields.size(); i++) {
			
			Class<?> outputType = fields.get(i).getType();
			Class<?> inputType = PrimitiveUtils.wrap(outputType);
			String typeDescriptor = Type.getDescriptor(outputType);
			String inputPath = inputType.getName().replace('.', '/');
			
			mv.visitLabel(labels[i]);
			
			// Push the compare object
			if (i == 0)
				mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { targetName }, 0, null);
			else
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			
			// Only write to public fields
			if (isPublic(fields.get(i))) {
				mv.visitVarInsn(Opcodes.ALOAD, 3);
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				
				if (!PrimitiveUtils.isPrimitive(outputType))
					mv.visitTypeInsn(Opcodes.CHECKCAST, inputPath);
				else
					boxingHelper.unbox(Type.getType(outputType));
				
				mv.visitFieldInsn(Opcodes.PUTFIELD, targetName, fields.get(i).getName(), typeDescriptor);
			
			} else {
				// Use reflection. We don't have a choice, unfortunately.
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, COMPILED_CLASS, "writeReflected", "(ILjava/lang/Object;)V;");
			}
		
			mv.visitJumpInsn(Opcodes.GOTO, returnLabel);
		}
		
		mv.visitLabel(errorLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitTypeInsn(Opcodes.NEW, FIELD_EXCEPTION_CLASS);
		mv.visitInsn(Opcodes.DUP);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("Invalid index ");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, FIELD_EXCEPTION_CLASS, "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(Opcodes.ATHROW);
		
		mv.visitLabel(returnLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(5, 4);
		mv.visitEnd();
	}

	private void createReadMethod(ClassWriter cw, String className, List<Field> fields, String targetSignature, String targetName) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "readGenerated", "(I)Ljava/lang/Object;", null, 
									new String[] { "com/comphenix/protocol/reflect/FieldAccessException" });
		BoxingHelper boxingHelper = new BoxingHelper(mv);
		
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, PACKAGE_NAME + "/" + className, "typedTarget", targetSignature);
		mv.visitVarInsn(Opcodes.ASTORE, 2);
		mv.visitVarInsn(Opcodes.ILOAD, 1);

		// The last label is for the default switch
		Label[] labels = new Label[fields.size()];
		Label errorLabel = new Label();
		
		// Generate labels
		for (int i = 0; i < fields.size(); i++) {
			labels[i] = new Label();
		}
		
		mv.visitTableSwitchInsn(0, fields.size() - 1, errorLabel, labels);
		
		for (int i = 0; i < fields.size(); i++) {
			Class<?> outputType = fields.get(i).getType();
			String typeDescriptor = Type.getDescriptor(outputType);
			
			mv.visitLabel(labels[i]);
			
			// Push the compare object
			if (i == 0)
				mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { targetName }, 0, null);
			else
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			
			// Note that byte code cannot access non-public fields
			if (isPublic(fields.get(i))) {
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				mv.visitFieldInsn(Opcodes.GETFIELD, targetName, fields.get(i).getName(), typeDescriptor);
				
				boxingHelper.box(Type.getType(outputType));
			} else {
				// We have to use reflection for private and protected fields.
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, COMPILED_CLASS, "readReflected", "(I)Ljava/lang/Object;");
			}
			
			mv.visitInsn(Opcodes.ARETURN);
		}

		mv.visitLabel(errorLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitTypeInsn(Opcodes.NEW, FIELD_EXCEPTION_CLASS);
		mv.visitInsn(Opcodes.DUP);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("Invalid index ");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, FIELD_EXCEPTION_CLASS, "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(Opcodes.ATHROW);
		mv.visitMaxs(5, 3);
		mv.visitEnd();
	}

	private void createConstructor(ClassWriter cw, String className, String targetSignature, String targetName) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", 
				"(L" + SUPER_CLASS + ";L" + PACKAGE_NAME + "/StructureCompiler;)V", 
				"(L" + SUPER_CLASS + "<TTField;>;L" + SUPER_CLASS + ";)V", null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, COMPILED_CLASS, "<init>", "()V");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PACKAGE_NAME + "/" + className, "initialize", "(L" + SUPER_CLASS + ";)V");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SUPER_CLASS, "getTarget", "()Ljava/lang/Object;");
		mv.visitFieldInsn(Opcodes.PUTFIELD, PACKAGE_NAME + "/" + className, "target", "Ljava/lang/Object;");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, PACKAGE_NAME + "/" + className, "target", "Ljava/lang/Object;");
		mv.visitTypeInsn(Opcodes.CHECKCAST, targetName);
		mv.visitFieldInsn(Opcodes.PUTFIELD, PACKAGE_NAME + "/" + className, "typedTarget", targetSignature);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitFieldInsn(Opcodes.PUTFIELD, PACKAGE_NAME + "/" + className, "compiler", "L" + PACKAGE_NAME + "/StructureCompiler;");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
	}
}
