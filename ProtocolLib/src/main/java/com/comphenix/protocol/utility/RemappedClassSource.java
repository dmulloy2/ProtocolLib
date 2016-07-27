package com.comphenix.protocol.utility;

// Thanks to Bergerkiller for his excellent hack. :D

// Copyright (C) 2013 bergerkiller
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of 
// this software and associated documentation files (the "Software"), to deal in 
// the Software without restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
// Software, and to permit persons to whom the Software is furnished to do so, 
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.lang.reflect.Method;

import org.bukkit.Bukkit;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.MethodUtils;
import com.comphenix.protocol.utility.RemappedClassSource.RemapperUnavailableException.Reason;

class RemappedClassSource extends ClassSource {
	private Object classRemapper;
	private Method mapType;
	private ClassLoader loader;
	
	/**
	 * Construct a new remapped class source using the default class loader.
	 */
	public RemappedClassSource() {
		this(RemappedClassSource.class.getClassLoader());
	}

	/**
	 * Construct a new renampped class source with the provided class loader.
	 * @param loader - the class loader.
	 */
	public RemappedClassSource(ClassLoader loader) {
		this.loader = loader;
	}
	
	/**
	 * Attempt to load the MCPC remapper.
	 * @return TRUE if we succeeded, FALSE otherwise.
	 * @throws RemapperUnavailableException If the remapper is not present.
	 */
	public RemappedClassSource initialize() {
		try {
			if (Bukkit.getServer() == null) {
				throw new RemapperUnavailableException(Reason.BUKKIT_NOT_INIT);
			}
			
			if (!triesForgeIntegration(Bukkit.getVersion())) {
				throw new RemapperUnavailableException(Reason.MCPC_NOT_PRESENT);
			}
			
			// Obtain the Class remapper used by MCPC+/Cauldron/What have you
			this.classRemapper = FieldUtils.readField(getClass().getClassLoader(), "remapper", true);
			
			if (this.classRemapper == null) {
				throw new RemapperUnavailableException(Reason.REMAPPER_DISABLED);
			}
			
			// Initialize some fields and methods used by the Jar Remapper
			Class<?> renamerClazz = classRemapper.getClass();
	
			this.mapType = MethodUtils.getAccessibleMethod(renamerClazz, "map", 
				new Class<?>[] { String.class });
			
			return this;
			
		} catch (RemapperUnavailableException e) {
			throw e;
		} catch (Exception e) {
			// Damn it
			throw new RuntimeException("Cannot access MCPC remapper.", e);
		}
	}

	private boolean triesForgeIntegration(String version) {
		return version.contains("MCPC") || version.contains("Cauldron") || version.contains("Thermos");
	}

	@Override
	public Class<?> loadClass(String canonicalName) throws ClassNotFoundException {
		final String remapped = getClassName(canonicalName);
		
		try {
			return loader.loadClass(remapped);
		} catch (ClassNotFoundException e) {
			throw new ClassNotFoundException("Cannot find " + canonicalName + "(Remapped: " + remapped + ")"); 
		}
	}
	
	/**
	 * Retrieve the obfuscated class name given an unobfuscated canonical class name.
	 * @param path - the canonical class name.
	 * @return The obfuscated class name.
	 */
	public String getClassName(String path) {
		try {
			String remapped = (String) mapType.invoke(classRemapper, path.replace('.', '/'));
			return remapped.replace('/', '.');
		} catch (Exception e) {
			throw new RuntimeException("Cannot remap class name.", e);
		}
	}

	public static class RemapperUnavailableException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public enum Reason {
			BUKKIT_NOT_INIT("Bukkit is not initialized"),
			MCPC_NOT_PRESENT("The server is not running Forge+Bukkit"),
			REMAPPER_DISABLED("Running a Forge+Bukkit server but the remapper is unavailable. Please turn it on!");
			
			private final String message;
			
			private Reason(String message) {
				this.message = message;
			}
			
			/**
			 * Retrieve a human-readable version of this reason.
			 * @return The human-readable verison.
			 */
			public String getMessage() {
				return message;
			}
		}
		
		private final Reason reason;
		
		public RemapperUnavailableException(Reason reason) {
			super(reason.getMessage());
			this.reason = reason;
		}
		
		/**
		 * Retrieve the reason the remapper is unavailable.
		 * @return The reason.
		 */
		public Reason getReason() {
			return reason;
		}
	}
}
