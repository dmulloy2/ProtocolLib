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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.compiler.StructureCompiler.StructureKey;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Compiles structure modifiers on a background thread.
 * <p>
 * This is necessary as we cannot block the main thread.
 * 
 * @author Kristian
 */
public class BackgroundCompiler {
	public static final ReportType REPORT_CANNOT_COMPILE_STRUCTURE_MODIFIER = new ReportType("Cannot compile structure. Disabing compiler.");
	public static final ReportType REPORT_CANNOT_SCHEDULE_COMPILATION = new ReportType("Unable to schedule compilation task.");
	
	/**
	 * The default format for the name of new worker threads.
	 */
	public static final String THREAD_FORMAT = "ProtocolLib-StructureCompiler %s";
	
	// How long to wait for a shutdown
	public static final int SHUTDOWN_DELAY_MS = 2000;
	
	/**
	 * The default fraction of perm gen space after which the background compiler will be disabled.
	 */
	public static final double DEFAULT_DISABLE_AT_PERM_GEN = 0.65;
	
	// The single background compiler we're using
	private static BackgroundCompiler backgroundCompiler;
	
	// Classes we're currently compiling
	private Map<StructureKey, List<CompileListener<?>>> listeners = Maps.newHashMap();
	private Object listenerLock = new Object();
	
	private StructureCompiler compiler;
	private boolean enabled;
	private boolean shuttingDown;
	
	private ExecutorService executor;
	private ErrorReporter reporter;

	private double disablePermGenFraction = DEFAULT_DISABLE_AT_PERM_GEN;
	
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
	 * <p>
	 * Uses the default {@link #THREAD_FORMAT} to name worker threads.
	 * @param loader - class loader from Bukkit.
	 * @param reporter - current error reporter.
	 */
	public BackgroundCompiler(ClassLoader loader, ErrorReporter reporter) {
		ThreadFactory factory = new ThreadFactoryBuilder().
			setDaemon(true).
			setNameFormat(THREAD_FORMAT).
			build();
		initializeCompiler(loader, reporter, Executors.newSingleThreadExecutor(factory));
	}
	
	/**
	 * Initialize a background compiler utilizing the given thread pool.
	 * @param loader - class loader from Bukkit.
	 * @param reporter - current error reporter.
	 * @param executor - thread pool we'll use.
	 */
	public BackgroundCompiler(ClassLoader loader, ErrorReporter reporter, ExecutorService executor) {
		initializeCompiler(loader, reporter, executor);
	}

	// Avoid "Constructor call must be the first statement".
	private void initializeCompiler(ClassLoader loader, ErrorReporter reporter, ExecutorService executor) {
		if (loader == null)
			throw new IllegalArgumentException("loader cannot be NULL");
		if (executor == null)
			throw new IllegalArgumentException("executor cannot be NULL");
		if (reporter == null)
			throw new IllegalArgumentException("reporter cannot be NULL.");
		
		this.compiler = new StructureCompiler(loader);
		this.reporter = reporter;
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
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <TKey> void scheduleCompilation(final StructureModifier<TKey> uncompiled, final CompileListener<TKey> listener) {
		// Only schedule if we're enabled
		if (enabled && !shuttingDown) {
			// Check perm gen
			if (getPermGenUsage() > disablePermGenFraction)
				return;
			
			// Don't try to schedule anything
			if (executor == null || executor.isShutdown())
				return;

			// Use to look up structure modifiers
			final StructureKey key = new StructureKey(uncompiled);
			
			// Allow others to listen in too
			synchronized (listenerLock) {
				List list = listeners.get(key);
				
				if (!listeners.containsKey(key)) {
					listeners.put(key, (List) Lists.newArrayList(listener)); 
				} else {
					// We're currently compiling
					list.add(listener);
					return;
				}
			}
			
			// Create the worker that will compile our modifier
			Callable<?> worker = new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					StructureModifier<TKey> modifier = uncompiled;
					List list = null;
					
					// Do our compilation
					try {
						modifier = compiler.compile(modifier);
						
						synchronized (listenerLock) {
							list = listeners.get(key);
							
							// Prevent ConcurrentModificationExceptions
							if (list != null) {
								list = Lists.newArrayList(list);
							}
						}
						
						// Only execute the listeners if there is a list
						if (list != null) {
							for (Object compileListener : list) {
								((CompileListener<TKey>) compileListener).onCompiled(modifier);
							}
							
							// Remove it when we're done
							synchronized (listenerLock) {
								list = listeners.remove(key);
							}
						}
						
					} catch (OutOfMemoryError e) {
						setEnabled(false);
						throw e;
					} catch (ThreadDeath e) {
						setEnabled(false);
						throw e;
					} catch (Throwable e) {
						// Disable future compilations!
						setEnabled(false);
						
						// Inform about this error as best as we can
						reporter.reportDetailed(BackgroundCompiler.this,
								Report.newBuilder(REPORT_CANNOT_COMPILE_STRUCTURE_MODIFIER).callerParam(uncompiled).error(e)
						);
					}
					
					// We'll also return the new structure modifier
					return modifier;
					
				}
			};
			
			try {
				// Lookup the previous class name on the main thread.
				// This is necessary as the Bukkit class loaders are not thread safe
				if (compiler.lookupClassLoader(uncompiled)) {
					try {
						worker.call();
					} catch (Exception e) {
						// Impossible!
						e.printStackTrace();
					}
					
				} else {
					
					// Perform the compilation on a seperate thread
					executor.submit(worker);
				}
				
			} catch (RejectedExecutionException e) {
				// Occures when the underlying queue is overflowing. Since the compilation  
				// is only an optmization and not really essential we'll just log this failure 
				// and move on.
				reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_SCHEDULE_COMPILATION).error(e));
			}
		}
	}
	
	/**
	 * Add a compile listener if we are still waiting for the structure modifier to be compiled.
	 * @param uncompiled - the structure modifier that may get compiled.
	 * @param listener - the listener to invoke in that case.
	 */
	@SuppressWarnings("unchecked")
	public <TKey> void addListener(final StructureModifier<TKey> uncompiled, final CompileListener<TKey> listener) {
		synchronized (listenerLock) {
			StructureKey key = new StructureKey(uncompiled);
			
			@SuppressWarnings("rawtypes")
			List list = listeners.get(key); 
			
			if (list != null) {
				list.add(listener);
			}
		}
	}
	
	/**
	 * Retrieve the current usage of the Perm Gen space in percentage.
	 * @return Usage of the perm gen space.
	 */
	private double getPermGenUsage() {
		for (MemoryPoolMXBean item : ManagementFactory.getMemoryPoolMXBeans()) {
			if (item.getName().contains("Perm Gen")) {
				MemoryUsage usage = item.getUsage();
				return usage.getUsed() / (double) usage.getCommitted();
			}
		}
		
		// Unknown
		return 0;
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
			// Unlikely to ever occur - it's the main thread
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
	 * Retrieve the fraction of perm gen space used after which the background compiler will be disabled.
	 * @return The fraction after which the background compiler is disabled.
	 */
	public double getDisablePermGenFraction() {
		return disablePermGenFraction;
	}
	
	/**
	 * Set the fraction of perm gen space used after which the background compiler will be disabled.
	 * @param fraction - the maximum use of perm gen space.
	 */
	public void setDisablePermGenFraction(double fraction) {
		this.disablePermGenFraction = fraction;
	}
	
	/**
	 * Retrieve the current structure compiler.
	 * @return Current structure compiler.
	 */
	public StructureCompiler getCompiler() {
		return compiler;
	}
}
