package com.comphenix.protocol.reflect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import com.comphenix.protocol.reflect.compiler.EmptyClassVisitor;
import com.comphenix.protocol.reflect.compiler.EmptyMethodVisitor;
import com.google.common.collect.Lists;

import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Type;

public class ClassAnalyser {	
	/**
	 * Represents a method in ASM.
	 * @author Kristian
	 */
	public static class AsmMethod {
		private final String ownerClass;
		private final String methodName;
		private final String signature;
		
		public AsmMethod(String ownerClass, String methodName, String signature) {
			this.ownerClass = ownerClass;
			this.methodName = methodName;
			this.signature = signature;
		}

		public String getOwnerName() {
			return ownerClass;
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
							output.add(new AsmMethod(owner, name, desc));
						}
					};
				}
				return null;
			}
			
		}, ClassReader.EXPAND_FRAMES);
		return output;
	}
}
