package com.comphenix.protocol.reflect.definer;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A class definer which uses class loader based class defining.
 */
final class LoaderClassDefiner implements ClassDefiner {

	// can be static, the system loader will never change
	private static final WrappingClassLoader SYSTEM_LOADER = new WrappingClassLoader(ClassLoader.getSystemClassLoader());

	private final Map<ClassLoader, WrappingClassLoader> loaderCache = new WeakHashMap<>();

	@Override
	public boolean isAvailable() {
		return true; // always possible
	}

	@Override
	public Class<?> define(Class<?> hostClass, byte[] byteCode) {
		try {
			ClassLoader hostLoader = hostClass.getClassLoader();
			WrappingClassLoader loader = hostLoader == null
					? SYSTEM_LOADER
					: this.loaderCache.computeIfAbsent(hostLoader, WrappingClassLoader::new);
			return loader.defineClass(byteCode);
		} catch (Exception exception) {
			// shouldn't happen
			return null;
		}
	}

	private static final class WrappingClassLoader extends ClassLoader {

		WrappingClassLoader(ClassLoader loader) {
			super(loader);
		}

		public Class<?> defineClass(byte[] byteCode) {
			Class<?> defined = this.defineClass(null, byteCode, 0, byteCode.length, null);
			this.resolveClass(defined); // link the class, if needed
			return defined;
		}
	}
}
