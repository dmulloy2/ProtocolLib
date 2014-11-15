package com.comphenix.protocol.reflect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Opcodes;
import net.sf.cglib.asm.Type;

import com.comphenix.protocol.reflect.ClassAnalyser.AsmMethod.AsmOpcodes;
import com.comphenix.protocol.reflect.compiler.EmptyClassVisitor;
import com.comphenix.protocol.reflect.compiler.EmptyMethodVisitor;
import com.google.common.collect.Lists;

public class ClassAnalyser {	
	/**
	 * Represents a method in ASM.
	 * <p>
	 * Keep in mind that this may also invoke a constructor.
	 * @author Kristian
	 */
	public static class AsmMethod {
		public enum AsmOpcodes {
			INVOKE_VIRTUAL,
			INVOKE_SPECIAL,
			INVOKE_STATIC,
			INVOKE_INTERFACE,
			INVOKE_DYNAMIC;
			
			public static AsmOpcodes fromIntOpcode(int opcode) {
				switch (opcode) {
					case Opcodes.INVOKEVIRTUAL: return AsmOpcodes.INVOKE_VIRTUAL;
					case Opcodes.INVOKESPECIAL: return AsmOpcodes.INVOKE_SPECIAL;
					case Opcodes.INVOKESTATIC: return AsmOpcodes.INVOKE_STATIC;
					case Opcodes.INVOKEINTERFACE: return AsmOpcodes.INVOKE_INTERFACE;
					case Opcodes.INVOKEDYNAMIC: return AsmOpcodes.INVOKE_DYNAMIC;
					default: throw new IllegalArgumentException("Unknown opcode: " + opcode);
				}
			}
		}
		
		private final AsmOpcodes opcode;
		private final String ownerClass;
		private final String methodName;
		private final String signature;
		
		public AsmMethod(AsmOpcodes opcode, String ownerClass, String methodName, String signature) {
			this.opcode = opcode;
			this.ownerClass = ownerClass;
			this.methodName = methodName;
			this.signature = signature;
		}

		public String getOwnerName() {
			return ownerClass;
		}
		
		/**
		 * Retrieve the opcode used to invoke this method or constructor.
		 * @return The opcode.
		 */
		public AsmOpcodes getOpcode() {
			return opcode;
		}
		
		/**
		 * Retrieve the associated owner class.
		 * @return The owner class.
		 * @throws ClassNotFoundException
		 */
		public Class<?> getOwnerClass() throws ClassNotFoundException {
			return AsmMethod.class.getClassLoader().loadClass(getOwnerName().replace('/', '.'));
		}
		
		public String getMethodName() {
			return methodName;
		}
		
		public String getSignature() {
			return signature;
		}
	}
	private static final ClassAnalyser DEFAULT = new ClassAnalyser();
	
	/**
	 * Retrieve the default instance.
	 * @return The default.
	 */
	public static ClassAnalyser getDefault() {
		return DEFAULT;
	}

	/**
	 * Retrieve every method calls in the given method.
	 * @param method - the method to analyse.
	 * @return The method calls.
	 * @throws IOException Cannot access the parent class.
	 */
	public List<AsmMethod> getMethodCalls(Method method) throws IOException {
		return getMethodCalls(method.getDeclaringClass(), method);
	}
	
	/**
	 * Retrieve every method calls in the given method.
	 * @param clazz - the parent class.
	 * @param method - the method to analyse.
	 * @return The method calls.
	 * @throws IOException Cannot access the parent class.
	 */
	public List<AsmMethod> getMethodCalls(Class<?> clazz, Method method) throws IOException {
		final ClassReader reader = new ClassReader(clazz.getCanonicalName());
		final List<AsmMethod> output = Lists.newArrayList();
		
		// The method we are looking for
		final String methodName = method.getName();
		final String methodDescription = Type.getMethodDescriptor(method);
		
		reader.accept(new EmptyClassVisitor() {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				// Check method
				if (methodName.equals(name) && methodDescription.equals(desc)) {
					return new EmptyMethodVisitor() {
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc) {
							output.add(new AsmMethod(AsmOpcodes.fromIntOpcode(opcode), owner, methodName, desc));
						}
					};
				}
				return null;
			}
			
		}, ClassReader.EXPAND_FRAMES);
		return output;
	}
}
