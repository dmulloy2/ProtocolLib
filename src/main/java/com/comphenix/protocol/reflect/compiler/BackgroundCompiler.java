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

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Compiles structure modifiers on a background thread.
 * <p>
 * This is necessary as we cannot block the main thread.
 *
 * @author Kristian
 */
public class BackgroundCompiler {

	public static final ReportType REPORT_CANNOT_COMPILE_STRUCTURE_MODIFIER = new ReportType(
			"Cannot compile structure. Disabling compiler.");
	public static final ReportType REPORT_CANNOT_SCHEDULE_COMPILATION = new ReportType(
			"Unable to schedule compilation task.");

	/**
	 * The default format for the name of new worker threads.
	 */
	public static final String THREAD_FORMAT = "ProtocolLib-StructureCompiler %s";

	/**
	 * The default time to wait for the compiler thread pool to terminate on shutdown.
	 */
	public static final int SHUTDOWN_DELAY_MS = 2000;

	// the instance of the background compiler, null if the compiler is disabled
	private static BackgroundCompiler backgroundCompiler;

	private final ErrorReporter reporter;
	private final ExecutorService executor;
	private final StructureCompiler compiler;

	// Classes we're currently compiling
	private final Map<StructureKey, Set<CompileListener<?>>> compileListeners = Maps.newHashMap();
	// if this compiler is enabled
	private final AtomicBoolean enabled = new AtomicBoolean(true);

	/**
	 * Initialize a background compiler.
	 * <p>
	 * Uses the default {@link #THREAD_FORMAT} to name worker threads.
	 *
	 * @param reporter - current error reporter.
	 */
	public BackgroundCompiler(ErrorReporter reporter) {
		Objects.requireNonNull(reporter, "reporter must be given");

		this.reporter = reporter;
		this.compiler = new StructureCompiler();
		this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
				.setDaemon(true)
				.setPriority(Thread.MIN_PRIORITY)
				.setNameFormat(THREAD_FORMAT)
				.build());
	}

	/**
	 * Retrieves the current background compiler.
	 *
	 * @return Current background compiler.
	 */
	public static BackgroundCompiler getInstance() {
		return backgroundCompiler;
	}

	/**
	 * Sets the single background compiler we're using.
	 *
	 * @param backgroundCompiler - current background compiler, or NULL if the library is not loaded.
	 */
	public static void setInstance(BackgroundCompiler backgroundCompiler) {
		BackgroundCompiler.backgroundCompiler = backgroundCompiler;
	}

	/**
	 * Ensure that the indirectly given structure modifier is eventually compiled.
	 *
	 * @param cache - store of structure modifiers.
	 * @param key   - key of the structure modifier to compile.
	 */
	public void scheduleCompilation(final Map<Class<?>, StructureModifier<?>> cache, final Class<?> key) {
		StructureModifier<?> uncompiled = cache.get(key);
		if (uncompiled != null) {
			this.scheduleCompilation(uncompiled, compiledModifier -> cache.put(key, compiledModifier));
		}
	}

	/**
	 * Ensure that the given structure modifier is eventually compiled.
	 *
	 * @param <T>        Type
	 * @param uncompiled - structure modifier to compile.
	 * @param listener   - listener responsible for responding to the compilation.
	 */
	@SuppressWarnings("unchecked")
	public <T> void scheduleCompilation(StructureModifier<T> uncompiled, CompileListener<T> listener) {
		// Only schedule if we're enabled
		if (this.enabled.get() && !this.executor.isShutdown()) {
			StructureKey key = StructureKey.forStructureModifier(uncompiled);

			// check if we're currently compiling
			Set<CompileListener<?>> listeners = this.compileListeners.get(key);
			if (listeners == null) {
				// not compiling - register and proceed
				this.compileListeners.put(key, Sets.newHashSet(listener));
			} else {
				// compiling already - do not compile twice
				listeners.add(listener);
				return;
			}

			// Create the worker that will compile our modifier
			Runnable runnable = () -> {
				try {
					// compile the modifier
					StructureModifier<T> compiled = this.compiler.compile(uncompiled);

					// post the result to all listeners
					Set<CompileListener<?>> registeredListeners = this.compileListeners.get(key);
					if (registeredListeners != null) {
						registeredListeners.forEach(l -> ((CompileListener<T>) l).onCompiled(compiled));
					}
				} catch (Exception exception) {
					this.setEnabled(false);
					this.reporter.reportDetailed(
							BackgroundCompiler.this,
							Report.newBuilder(REPORT_CANNOT_COMPILE_STRUCTURE_MODIFIER).callerParam(uncompiled).error(exception));
				}
			};

			try {
				// try to submit the task to the background worker
				this.executor.execute(runnable);
			} catch (RejectedExecutionException exception) {
				// might occur if the compiler queue is too full or the executor is now shutting down
				this.reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_SCHEDULE_COMPILATION).error(exception));
			}
		}
	}

	/**
	 * Add a compile listener if we are still waiting for the structure modifier to be compiled.
	 *
	 * @param <T>        Type
	 * @param uncompiled - the structure modifier that may get compiled.
	 * @param listener   - the listener to invoke in that case.
	 */
	public <T> void addListener(final StructureModifier<T> uncompiled, final CompileListener<T> listener) {
		Set<CompileListener<?>> listeners = this.compileListeners.get(StructureKey.forStructureModifier(uncompiled));
		if (listeners != null) {
			listeners.add(listener);
		}
	}

	/**
	 * Clean up after ourselves using the default timeout.
	 */
	public void shutdownAll() {
		this.shutdownAll(SHUTDOWN_DELAY_MS, TimeUnit.MILLISECONDS);
	}

	/**
	 * Clean up after ourselves.
	 *
	 * @param timeout - the maximum time to wait.
	 * @param unit    - the time unit of the timeout argument.
	 */
	public void shutdownAll(long timeout, TimeUnit unit) {
		if (this.enabled.getAndSet(false)) {
			try {
				this.executor.shutdown();
				this.executor.awaitTermination(timeout, unit);
			} catch (InterruptedException ignored) {
			}
		}
	}

	/**
	 * Retrieve whether the background compiler is enabled.
	 *
	 * @return TRUE if it is enabled, FALSE otherwise.
	 */
	public boolean isEnabled() {
		return this.enabled.get();
	}

	/**
	 * Sets whether the background compiler is enabled.
	 *
	 * @param enabled - TRUE to enable it, FALSE otherwise.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}

	/**
	 * Retrieve the current structure compiler.
	 *
	 * @return Current structure compiler.
	 */
	public StructureCompiler getCompiler() {
		return this.compiler;
	}
}
