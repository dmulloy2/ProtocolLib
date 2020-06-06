package com.comphenix.protocol.executors;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class BukkitFutures {
	// Represents empty classes
	private static Listener EMPTY_LISTENER = new Listener() {};

	/**
	 * Retrieve a future representing the next invocation of the given event.
	 * @param plugin - owner plugin.
	 * @return Future event invocation.
	 */
	public static <TEvent extends Event> ListenableFuture<TEvent> nextEvent(Plugin plugin, Class<TEvent> eventClass) {
		return BukkitFutures.nextEvent(plugin, eventClass, EventPriority.NORMAL, false);
	}

	/**
	 * Retrieve a future representing the next invocation of the given event.
	 * @param plugin - owner plugin.
	 * @return Future event invocation.
	 */
	public static <TEvent extends Event> ListenableFuture<TEvent> nextEvent(
			Plugin plugin, Class<TEvent> eventClass, EventPriority priority, boolean ignoreCancelled) {
		
		// Event and future
		final HandlerList list = getHandlerList(eventClass);
		final SettableFuture<TEvent> future = SettableFuture.create();
		
		EventExecutor executor = new EventExecutor() {
			private final AtomicBoolean once = new AtomicBoolean();
			
			@SuppressWarnings("unchecked")
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				// Fire the future
				if (!future.isCancelled() && !once.getAndSet(true)) {
					future.set((TEvent) event);
				}
			}
		};
		RegisteredListener listener = new RegisteredListener(EMPTY_LISTENER, executor, priority, plugin, ignoreCancelled) {
			@Override
			public void callEvent(Event event) throws EventException {
				super.callEvent(event);
				list.unregister(this);
			}
		};
		
		// Ensure that the future is cleaned up when the plugin is disabled
		PluginDisabledListener.getListener(plugin).addFuture(future);
		
		// Add the listener
		list.register(listener);
		return future;
	}

	/**
	 * Register a given event executor.
	 * @param plugin - the owner plugin.
	 * @param eventClass - the event to register.
	 * @param priority - the event priority.
	 * @param executor - the event executor.
	 */
	public static void registerEventExecutor(Plugin plugin, Class<? extends Event> eventClass, EventPriority priority, EventExecutor executor) {
		getHandlerList(eventClass).register(
				new RegisteredListener(EMPTY_LISTENER, executor, priority, plugin, false)
		);
	}
	
	/**
	 * Retrieve the handler list associated with the given class.
	 * @param clazz - given event class.
	 * @return Associated handler list.
	 */
	private static HandlerList getHandlerList(Class<? extends Event> clazz) {
		// Class must have Event as its superclass
		while (clazz.getSuperclass() != null && Event.class.isAssignableFrom(clazz.getSuperclass())) {
			try {
				Method method = clazz.getDeclaredMethod("getHandlerList");
				method.setAccessible(true);
				return (HandlerList) method.invoke(null);
			} catch (NoSuchMethodException e) {
				// Keep on searching
				clazz = clazz.getSuperclass().asSubclass(Event.class);
			} catch (Exception e) {
				throw new IllegalPluginAccessException(e.getMessage());
			}
		}
		throw new IllegalPluginAccessException("Unable to find handler list for event "
				+ clazz.getName());
	}
}
