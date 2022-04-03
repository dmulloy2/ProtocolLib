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

import static net.bytebuddy.jar.asm.Opcodes.AASTORE;
import static net.bytebuddy.jar.asm.Opcodes.ACC_FINAL;
import static net.bytebuddy.jar.asm.Opcodes.ACC_PROTECTED;
import static net.bytebuddy.jar.asm.Opcodes.ACC_PUBLIC;
import static net.bytebuddy.jar.asm.Opcodes.ALOAD;
import static net.bytebuddy.jar.asm.Opcodes.ANEWARRAY;
import static net.bytebuddy.jar.asm.Opcodes.ARETURN;
import static net.bytebuddy.jar.asm.Opcodes.ATHROW;
import static net.bytebuddy.jar.asm.Opcodes.CHECKCAST;
import static net.bytebuddy.jar.asm.Opcodes.DUP;
import static net.bytebuddy.jar.asm.Opcodes.GETFIELD;
import static net.bytebuddy.jar.asm.Opcodes.GOTO;
import static net.bytebuddy.jar.asm.Opcodes.ICONST_0;
import static net.bytebuddy.jar.asm.Opcodes.ICONST_1;
import static net.bytebuddy.jar.asm.Opcodes.ILOAD;
import static net.bytebuddy.jar.asm.Opcodes.INVOKESPECIAL;
import static net.bytebuddy.jar.asm.Opcodes.INVOKESTATIC;
import static net.bytebuddy.jar.asm.Opcodes.INVOKEVIRTUAL;
import static net.bytebuddy.jar.asm.Opcodes.NEW;
import static net.bytebuddy.jar.asm.Opcodes.PUTFIELD;
import static net.bytebuddy.jar.asm.Opcodes.RETURN;
import static net.bytebuddy.jar.asm.Opcodes.V1_8;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.definer.ClassDefiners;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

/**
 * Represents a StructureModifier compiler.
 *
 * @author Kristian
 */
public final class StructureCompiler {

	// the format of the class names we generate
	private static final String GENERATED_CLASS_FORMAT = "%s$CompiledStructureModifier_%s";

	// shared types
	private static final Type OBJECT_TYPE = Type.getType(Object.class);
	private static final Type STRING_TYPE = Type.getType(String.class);

	// the super class of our generated class
	private static final String MODIFIER_CLASS = Type.getInternalName(StructureModifier.class);
	private static final String COMPILED_MODIFIER_CLASS = Type.getInternalName(CompiledStructureModifier.class);

	// exception throwing
	private static final String ILLEGAL_ARGUMENT_CLASS = Type.getInternalName(IllegalArgumentException.class);
	private static final String STRING_FORMAT_DESC = Type.getMethodDescriptor(
			STRING_TYPE,
			STRING_TYPE,
			Type.getType(Object[].class));
	private static final String MESSAGE_EXCEPTION_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, STRING_TYPE);

	// methods we use which are inherited from StructureModifier
	private static final String WRITE_DESC = Type.getMethodDescriptor(
			Type.getType(StructureModifier.class),
			Type.INT_TYPE,
			OBJECT_TYPE);
	private static final String READ_DESC = Type.getMethodDescriptor(OBJECT_TYPE, Type.INT_TYPE);

	// method descriptors of the methods we are generating
	private static final String WRITE_METHOD_DESC = Type.getMethodDescriptor(
			Type.VOID_TYPE,
			OBJECT_TYPE,
			Type.INT_TYPE,
			OBJECT_TYPE);
	private static final String CLASS_CONSTRUCTOR_DESC = Type.getMethodDescriptor(
			Type.VOID_TYPE,
			Type.getType(StructureCompiler.class),
			Type.getType(StructureModifier.class));
	private static final String READ_METHOD_DESC = Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE, Type.INT_TYPE);

	// all modifiers which were already compiled
	private final Map<StructureKey, ConstructorAccessor> compiledCache = new HashMap<>();

	/**
	 * Construct a structure compiler.
	 */
	StructureCompiler() {
	}

	/**
	 * Compiles the given structure modifier.
	 * <p>
	 * WARNING: Do NOT call this method in the main thread. Compiling may easily take 10 ms, which is already over 1/4 of
	 * a tick (50 ms). Let the background thread automatically compile the structure modifiers instead.
	 *
	 * @param <T>    Type
	 * @param source - structure modifier to compile.
	 * @return A compiled structure modifier.
	 */
	@SuppressWarnings("unchecked")
	public <T> StructureModifier<T> compile(StructureModifier<T> source) {
		StructureKey key = StructureKey.forStructureModifier(source);

		// check if we already compiled the modifier
		ConstructorAccessor compiledModifierAcc = this.compiledCache.get(key);
		if (compiledModifierAcc != null) {
			return (StructureModifier<T>) compiledModifierAcc.invoke(source, this, source);
		}

		// try to compile the modifier
		Class<?> compiledClass = this.generateClass(source);
		compiledModifierAcc = Accessors.getConstructorAccessor(
				compiledClass,
				StructureCompiler.class,
				StructureModifier.class);

		// cache and instantiate
		this.compiledCache.put(key, compiledModifierAcc);
		return (StructureModifier<T>) compiledModifierAcc.invoke(source, this, source);
	}

	/**
	 * Compile a structure modifier.
	 *
	 * @param source - structure modifier.
	 * @return The compiled structure modifier.
	 */
	private <T> Class<?> generateClass(StructureModifier<T> source) {
		String targetType = Type.getInternalName(source.getTargetType());
		String className = String.format(GENERATED_CLASS_FORMAT, targetType, UUID.randomUUID().toString().split("-")[0]);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, className, null, COMPILED_MODIFIER_CLASS, null);

		// put all members in
		this.createConstructor(cw);
		this.createReadMethod(cw, className, source.getFields(), source.getTargetType());
		this.createWriteMethod(cw, targetType, source.getFields(), source.getTargetType());

		// finish the class
		cw.visitEnd();
		return ClassDefiners.availableDefiner().define(source.getTargetType(), cw.toByteArray());
	}

	private void createWriteMethod(ClassWriter cw, String targetName, List<FieldAccessor> fields, Class<?> host) {
		// the labels for each switch case
		Label[] caseLabels = new Label[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			caseLabels[i] = new Label();
		}

		// other labels
		Label defaultLabel = new Label();
		Label methodExitLabel = new Label();

		// begin the method visit
		MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "writeGenerated", WRITE_METHOD_DESC, null, null);
		mv.visitCode();

		// begins the switch case
		mv.visitTableSwitchInsn(0, caseLabels.length - 1, defaultLabel, caseLabels);
		for (int i = 0; i < caseLabels.length; i++) {
			Field field = fields.get(i).getField();
			Class<?> fieldType = field.getType();

			// visits the next switch case, keeping the same locals as the previous one
			mv.visitLabel(caseLabels[i]);
			if (i == 0) {
				mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{targetName}, 0, null);
			} else {
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}

			// we can only write to fields which are within our current scope
			if (this.canAccess(host, field)) {
				// loads the needed stack items
				mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 3);

				// cast the input object to the correct type
				if (fieldType.isPrimitive()) {
					CodeGenUtil.unbox(mv, fieldType);
				} else {
					mv.visitTypeInsn(CHECKCAST, Type.getInternalName(fieldType));
				}

				// rewrite the field
				mv.visitFieldInsn(
						PUTFIELD,
						Type.getInternalName(field.getDeclaringClass()),
						field.getName(),
						Type.getDescriptor(fieldType));
			} else {
				// use reflection :(
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ILOAD, 2);
				mv.visitVarInsn(ALOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, MODIFIER_CLASS, "write", WRITE_DESC, false);
			}

			// jump to the end of the method
			mv.visitJumpInsn(GOTO, methodExitLabel);
		}

		// throw an exception if the write is out of bounds
		mv.visitLabel(defaultLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		this.throwIllegalIndexException(mv);

		mv.visitLabel(methodExitLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void createReadMethod(ClassWriter cw, String targetName, List<FieldAccessor> fields, Class<?> host) {
		// the labels for each switch case
		Label[] caseLabels = new Label[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			caseLabels[i] = new Label();
		}

		// other labels
		Label defaultLabel = new Label();

		// begin the method visit
		MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "readGenerated", READ_METHOD_DESC, null, null);
		mv.visitCode();

		// begins the switch case
		mv.visitTableSwitchInsn(0, caseLabels.length - 1, defaultLabel, caseLabels);
		for (int i = 0; i < caseLabels.length; i++) {
			Field field = fields.get(i).getField();
			Class<?> fieldType = field.getType();

			// visits the next switch case, keeping the same locals as the previous one
			mv.visitLabel(caseLabels[i]);
			if (i == 0) {
				mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{targetName}, 0, null);
			} else {
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}

			// check if the host class is allowed to access the field
			if (this.canAccess(host, field)) {
				// get the field
				mv.visitVarInsn(ALOAD, 1);
				mv.visitFieldInsn(
						GETFIELD,
						Type.getInternalName(field.getDeclaringClass()),
						field.getName(),
						Type.getDescriptor(fieldType));
				// box before returning if needed
				if (fieldType.isPrimitive()) {
					CodeGenUtil.box(mv, fieldType);
				}
			} else {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ILOAD, 2);
				mv.visitMethodInsn(INVOKEVIRTUAL, MODIFIER_CLASS, "read", READ_DESC, false);
			}

			// jump to the end of the method
			mv.visitInsn(ARETURN);
		}

		// throw an exception if the read is out of bounds
		mv.visitLabel(defaultLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		this.throwIllegalIndexException(mv);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void createConstructor(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", CLASS_CONSTRUCTOR_DESC, null, null);
		mv.visitCode();

		// invoke the super constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKESPECIAL, COMPILED_MODIFIER_CLASS, "<init>", CLASS_CONSTRUCTOR_DESC, false);

		// exit from the constructor
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void throwIllegalIndexException(MethodVisitor mv) {
		mv.visitTypeInsn(NEW, ILLEGAL_ARGUMENT_CLASS);
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Invalid field index %d");
		mv.visitInsn(ICONST_1);
		mv.visitTypeInsn(ANEWARRAY, OBJECT_TYPE.getInternalName());
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ILOAD, 2);
		CodeGenUtil.box(mv, int.class);
		mv.visitInsn(AASTORE);
		mv.visitMethodInsn(INVOKESTATIC, STRING_TYPE.getInternalName(), "format", STRING_FORMAT_DESC, false);
		mv.visitMethodInsn(INVOKESPECIAL, ILLEGAL_ARGUMENT_CLASS, "<init>", MESSAGE_EXCEPTION_DESC, false);
		mv.visitInsn(ATHROW);
	}

	private boolean canAccess(Class<?> host, Field field) {
		// public and protected fields are always accessible
		int mods = field.getModifiers();
		if (Modifier.isPublic(mods) || Modifier.isProtected(mods)) {
			return true;
		}

		// we can only access private members which are in the same host class
		Class<?> declaring = field.getDeclaringClass();
		if (Modifier.isPrivate(mods)) {
			return host.equals(declaring);
		}

		// at this point the field must be package-private, validate that we have access by checking the package
		Package hostPackage = host.getPackage();
		Package declaringPackage = declaring.getPackage();

		return (hostPackage == null && declaringPackage == null) || Objects.equals(hostPackage, declaringPackage);
	}
}
