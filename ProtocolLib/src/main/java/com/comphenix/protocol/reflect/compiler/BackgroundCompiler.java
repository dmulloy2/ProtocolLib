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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comphenix.protocol.reflect.StructureModifier;

/**
 * Compiles structure modifiers on a background thread.
 * <p>
 * This is necessary as we cannot block the main thread.
 * 
 * @author Kristian
 */
public class BackgroundCompiler {

	// How long to wait for a shutdown
	public static final int SHUTDOWN_DELAY_MS = 2000;
	
	// The single background compiler we're using
	private static BackgroundCompiler backgroundCompiler;
	
	private StructureCompiler compiler;
	private boolean enabled;
	private boolean shuttingDown;
	
	private ExecutorService executor;
	
	/**
	 * Retrieves the current background compiler.
	 * @return Current background compiler.
	 */
	public static BackgroundCompiler getInstance() {
		return backgroundCompiler;
	}
	
	/**
	 * Sets the single background compiler we're using.
	 * @param backgroundCompiler - current background compiler, or NULL if the library is not loaded.
	 */
	public static void setInstance(BackgroundCompiler backgroundCompiler) {
		BackgroundCompiler.backgroundCompiler = backgroundCompiler;
	}
	
	/**
	 * Initialize a background compiler.
	 * @param loader - class loader from Bukkit.
	 */
	public BackgroundCompiler(ClassLoader loader) {
		this(loader, Executors.newSingleThreadExecutor());
	}

	/**
	 * Initialize a background compiler utilizing the given thread pool.
	 * @param loader - class loader from Bukkit.
	 * @param executor - thread pool we'll use.
	 */
	public BackgroundCompiler(ClassLoader loader, ExecutorService executor) {
		if (loader == null)
			throw new IllegalArgumentException("loader cannot be NULL");
		if (executor == null)
			throw new IllegalArgumentException("executor cannot be NULL");
		
		this.compiler = new StructureCompiler(loader);
		this.executor = executor;
		this.enabled = true;
	}
	
	/**
	 * Ensure that the indirectly given structure modifier is eventually compiled.
	 * @param cache - store of structure modifiers.
	 * @param key - key of the structure modifier to compile.
	 */
	@SuppressWarnings("rawtypes")
	public void scheduleCompilation(final Map<Class, StructureModifier> cache, final Class key) {
		
		@SuppressWarnings("unchecked")
		final StructureModifier<Object> uncompiled = cache.get(key);
		
		if (uncompiled != null) {
			scheduleCompilation(uncompiled, new CompileListener<Object>() {
				@Override
				public void onCompiled(StructureModifier<Object> compiledModifier) {
					// Update cache
					cache.put(key, compiledModifier);
				}
			});
		}
	}
	
	/**
	 * Ensure that the given structure modifier is eventually compiled.
	 * @param uncompiled - structure modifier to compile.
	 * @param listener - listener responsible for responding to the compilation.
	 */
	public <TKey> void scheduleCompilation(final StructureModifier<TKey> uncompiled, final CompileListener<TKey> listener) {

		// Only schedule if we're enabled
		if (enabled && !shuttingDown) {
			
			// Don't try to schedule anything
			if (executor == null || executor.isShutdown())
				return;
			
			try {
				executor.submit(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
		
						StructureModifier<TKey> modifier = uncompiled;
						
						// Do our compilation
						modifier = compiler.compile(modifier);
						listener.onCompiled(modifier);
						
						// We'll also return the new structure modifier
						return modifier;
					}
				});
			} catch (RejectedExecutionException e) {
				// Occures when the underlying queue is overflowing. Since the compilation  
				// is only an optmization and not really essential we'll just log this failure 
				// and move on.
				Logger.getLogger("Minecraft").log(Level.WARNING, "Unable to schedule compilation task.", e);
			}
		}
	}
	
	/**
	 * Clean up after ourselves using the default timeout.
	 */
	public void shutdownAll() {
		shutdownAll(SHUTDOWN_DELAY_MS, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Clean up after ourselves.
	 * @param timeout - the maximum time to wait.
	 * @param unit - the time unit of the timeout argument.
	 */
	public void shutdownAll(long timeout, TimeUnit unit) {
		setEnabled(false);
		shuttingDown = true;
		executor.shutdown();
		
		try {
			executor.awaitTermination(timeout, unit);
		} catch (InterruptedException e) {
			// Unlikely to ever occur.
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve whether or not the background compiler is enabled.
	 * @return TRUE if it is enabled, FALSE otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether or not the background compiler is enabled.
	 * @param enabled - TRUE to enable it, FALSE otherwise.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Retrieve the current structure compiler.
	 * @return Current structure compiler.
	 */
	public StructureCompiler getCompiler() {
		return compiler;
	}
}
