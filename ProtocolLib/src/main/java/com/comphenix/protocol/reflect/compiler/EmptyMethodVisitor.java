package com.comphenix.protocol.reflect.compiler;

import net.sf.cglib.asm.AnnotationVisitor;
import net.sf.cglib.asm.Attribute;
import net.sf.cglib.asm.Label;
import net.sf.cglib.asm.MethodVisitor;

public class EmptyMethodVisitor implements MethodVisitor {
	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		// NOP
		return null;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// NOP
		return null;
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		// NOP
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {
		// NOP
	}

	@Override
	public void visitCode() {
		// NOP
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		// NOP
	}

	@Override
	public void visitInsn(int opcode) {
		// NOP
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		// NOP
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		// NOP
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		// NOP
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// NOP
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// NOP
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		// NOP
	}

	@Override
	public void visitLabel(Label label) {
		// NOP
	}

	@Override
	public void visitLdcInsn(Object cst) {
		// NOP
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		// NOP
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		// NOP
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		// NOP
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		// NOP
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// NOP
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start,
			Label end, int index) {
		// NOP
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		// NOP
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// NOP
	}

	@Override
	public void visitEnd() {
		// NOP
	}
}
