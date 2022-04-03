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

import com.google.common.primitives.Primitives;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

/**
 * Used by the compiler to automatically box and unbox values.
 */
final class CodeGenUtil {

	private CodeGenUtil() {
		// seal this class
	}

	/**
	 * Generates the instructions to box the top stack value. This value is replaced by its boxed equivalent on top of the
	 * stack.
	 *
	 * @param type the Type of the top stack value.
	 */
	public static void box(MethodVisitor mv, Class<?> type) {
		// don't do anything for objects and arrays
		if (!type.isPrimitive()) {
			return;
		}

		// push 'null' for void
		if (type == void.class) {
			mv.visitInsn(Opcodes.ACONST_NULL);
			return;
		}

		// find the correct wrapper for the given type
		Class<?> wrapperClass = Primitives.wrap(type);
		// invoke the valueOf method in the wrapper to class to convert the primitive type to an object
		mv.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				Type.getInternalName(wrapperClass),
				"valueOf",
				Type.getMethodDescriptor(Type.getType(wrapperClass), Type.getType(type)),
				false);
	}

	/**
	 * Generates the instructions to unbox the top stack value. This value is replaced by its unboxed equivalent on top of
	 * the stack.
	 *
	 * @param type the Type of the top stack value.
	 */
	public static void unbox(MethodVisitor mv, Class<?> type) {
		if (type != void.class) {
			Class<?> wrapper = Primitives.wrap(type);
			mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(wrapper));
			mv.visitMethodInsn(
					Opcodes.INVOKEVIRTUAL,
					Type.getInternalName(wrapper),
					type.getSimpleName() + "Value", // for example: int.class.getSimpleName() + Value = intValue()
					Type.getMethodDescriptor(Type.getType(type)),
					false);
		}
	}
}
