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

import net.sf.cglib.asm.$MethodVisitor;
import net.sf.cglib.asm.$Opcodes;
import net.sf.cglib.asm.$Type;

/**
 * Used by the compiler to automatically box and unbox values.
 */
class BoxingHelper {

	private final static $Type BYTE_$Type = $Type.getObjectType("java/lang/Byte");
	private final static $Type BOOLEAN_$Type = $Type.getObjectType("java/lang/Boolean");
	private final static $Type SHORT_$Type = $Type.getObjectType("java/lang/Short");
	private final static $Type CHARACTER_$Type = $Type.getObjectType("java/lang/Character");
	private final static $Type INTEGER_$Type = $Type.getObjectType("java/lang/Integer");
	private final static $Type FLOAT_$Type = $Type.getObjectType("java/lang/Float");
	private final static $Type LONG_$Type = $Type.getObjectType("java/lang/Long");
	private final static $Type DOUBLE_$Type = $Type.getObjectType("java/lang/Double");
	private final static $Type NUMBER_$Type = $Type.getObjectType("java/lang/Number");
	private final static $Type OBJECT_$Type = $Type.getObjectType("java/lang/Object");

	private final static MethodDescriptor BOOLEAN_VALUE = MethodDescriptor.getMethod("boolean booleanValue()");
	private final static MethodDescriptor CHAR_VALUE = MethodDescriptor.getMethod("char charValue()");
	private final static MethodDescriptor INT_VALUE = MethodDescriptor.getMethod("int intValue()");
	private final static MethodDescriptor FLOAT_VALUE = MethodDescriptor.getMethod("float floatValue()");
	private final static MethodDescriptor LONG_VALUE = MethodDescriptor.getMethod("long longValue()");
	private final static MethodDescriptor DOUBLE_VALUE = MethodDescriptor.getMethod("double doubleValue()");

	private $MethodVisitor mv;
	
	public BoxingHelper($MethodVisitor mv) {
		this.mv = mv;
	}
	
	/**
	 * Generates the instructions to box the top stack value. This value is
	 * replaced by its boxed equivalent on top of the stack.
	 *
	 * @param type the $Type of the top stack value.
	 */
	public void box(final $Type type){
		if(type.getSort() == $Type.OBJECT || type.getSort() == $Type.ARRAY) {
			return;
		}
		
		if(type == $Type.VOID_TYPE) {
			push((String) null);
		} else {
			$Type boxed = type;
			
			switch(type.getSort()) {
				case $Type.BYTE:
					boxed = BYTE_$Type;
					break;
				case $Type.BOOLEAN:
					boxed = BOOLEAN_$Type;
					break;
				case $Type.SHORT:
					boxed = SHORT_$Type;
					break;
				case $Type.CHAR:
					boxed = CHARACTER_$Type;
					break;
				case $Type.INT:
					boxed = INTEGER_$Type;
					break;
				case $Type.FLOAT:
					boxed = FLOAT_$Type;
					break;
				case $Type.LONG:
					boxed = LONG_$Type;
					break;
				case $Type.DOUBLE:
					boxed = DOUBLE_$Type;
					break;
			}
			
			newInstance(boxed);
			if(type.getSize() == 2) {
				// Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
				dupX2();
				dupX2();
				pop();
			} else {
				// p -> po -> opo -> oop -> o
				dupX1();
				swap();
			}
			
			invokeConstructor(boxed, new MethodDescriptor("<init>", $Type.VOID_TYPE, new $Type[] {type}));
		}
	}
	
	/**
	 * Generates the instruction to invoke a constructor.
	 *
	 * @param $Type   the class in which the constructor is defined.
	 * @param method the constructor to be invoked.
	 */
	public void invokeConstructor(final $Type $Type, final MethodDescriptor method){
		invokeInsn($Opcodes.INVOKESPECIAL, $Type, method);
	}
	
	/**
	 * Generates a DUP_X1 instruction.
	 */
	public void dupX1(){
		mv.visitInsn($Opcodes.DUP_X1);
	}

	/**
	 * Generates a DUP_X2 instruction.
	 */
	public void dupX2(){
		mv.visitInsn($Opcodes.DUP_X2);
	}
	
	/**
	 * Generates a POP instruction.
	 */
	public void pop(){
		mv.visitInsn($Opcodes.POP);
	}

	/**
	 * Generates a SWAP instruction.
	 */
	public void swap(){
		mv.visitInsn($Opcodes.SWAP);
	}
	
	/**
	 * Generates the instruction to push the given value on the stack.
	 *
	 * @param value the value to be pushed on the stack.
	 */
	public void push(final boolean value){
		push(value ? 1 : 0);
	}
	
	/**
	 * Generates the instruction to push the given value on the stack.
	 * 
	 * @param value the value to be pushed on the stack.
	 */
	public void push(final int value) {
		if (value >= -1 && value <= 5) {
			mv.visitInsn($Opcodes.ICONST_0 + value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			mv.visitIntInsn($Opcodes.BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn($Opcodes.SIPUSH, value);
		} else {
			mv.visitLdcInsn(new Integer(value));
		}
	}

	/**
	 * Generates the instruction to create a new object.
	 *
	 * @param $Type the class of the object to be created.
	 */
	public void newInstance(final $Type $Type){
		$TypeInsn($Opcodes.NEW, $Type);
	}
	
	/**
	 * Generates the instruction to push the given value on the stack.
	 * 
	 * @param value the value to be pushed on the stack. May be <tt>null</tt>.
	 */
	public void push(final String value) {
		if (value == null) {
			mv.visitInsn($Opcodes.ACONST_NULL);
		} else {
			mv.visitLdcInsn(value);
		}
	}

	/**
	 * Generates the instructions to unbox the top stack value. This value is
	 * replaced by its unboxed equivalent on top of the stack.
	 * 
	 * @param type
	 *            the $Type of the top stack value.
	 */
	public void unbox(final $Type type){
		$Type t = NUMBER_$Type;
		MethodDescriptor sig = null;
		
		switch(type.getSort()) {
			case $Type.VOID:
				return;
			case $Type.CHAR:
				t = CHARACTER_$Type;
				sig = CHAR_VALUE;
				break;
			case $Type.BOOLEAN:
				t = BOOLEAN_$Type;
				sig = BOOLEAN_VALUE;
				break;
			case $Type.DOUBLE:
				sig = DOUBLE_VALUE;
				break;
			case $Type.FLOAT:
				sig = FLOAT_VALUE;
				break;
			case $Type.LONG:
				sig = LONG_VALUE;
				break;
			case $Type.INT:
			case $Type.SHORT:
			case $Type.BYTE:
				sig = INT_VALUE;
			}
		
		if(sig == null) {
			checkCast(type);
		} else {
			checkCast(t);
			invokeVirtual(t, sig);
		}
	}

	/**
	 * Generates the instruction to check that the top stack value is of the
	 * given $Type.
	 *
	 * @param $Type a class or interface $Type.
	 */
	public void checkCast(final $Type $Type){
		if(!$Type.equals(OBJECT_$Type)) {
			$TypeInsn($Opcodes.CHECKCAST, $Type);
		}
	}
	
	/**
	 * Generates the instruction to invoke a normal method.
	 *
	 * @param owner  the class in which the method is defined.
	 * @param method the method to be invoked.
	 */
	public void invokeVirtual(final $Type owner, final MethodDescriptor method){
		invokeInsn($Opcodes.INVOKEVIRTUAL, owner, method);
	}
	
	/**
	 * Generates an invoke method instruction.
	 *
	 * @param opcode the instruction's opcode.
	 * @param $Type   the class in which the method is defined.
	 * @param method the method to be invoked.
	 */
	private void invokeInsn(final int opcode, final $Type $Type, final MethodDescriptor method){
		String owner = $Type.getSort() == $Type.ARRAY ? $Type.getDescriptor() : $Type.getInternalName();
		mv.visitMethodInsn(opcode, owner, method.getName(), method.getDescriptor());
	}
	
	/**
	 * Generates a $Type dependent instruction.
	 *
	 * @param opcode the instruction's opcode.
	 * @param $Type   the instruction's operand.
	 */
	private void $TypeInsn(final int opcode, final $Type $Type){
		String desc;
		
		if($Type.getSort() == $Type.ARRAY) {
			desc = $Type.getDescriptor();
		} else { 
			desc = $Type.getInternalName();
		}
		
		mv.visitTypeInsn(opcode, desc);
	}
}
