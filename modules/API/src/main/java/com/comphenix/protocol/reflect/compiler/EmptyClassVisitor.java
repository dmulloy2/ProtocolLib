package com.comphenix.protocol.reflect.compiler;

import net.sf.cglib.asm.AnnotationVisitor;
import net.sf.cglib.asm.Attribute;
import net.sf.cglib.asm.ClassVisitor;
import net.sf.cglib.asm.FieldVisitor;
import net.sf.cglib.asm.MethodVisitor;

public abstract class EmptyClassVisitor implements ClassVisitor {
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// NOP
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// NOP
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {
		// NOP
	}

	@Override
	public void visitEnd() {
		// NOP
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// NOP
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// NOP
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// NOP
		return null;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		// NOP
	}

	@Override
	public void visitSource(String source, String debug) {
		// NOP
	}
}
