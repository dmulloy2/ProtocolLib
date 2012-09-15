package com.comphenix.protocol.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.entity.Player;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.comphenix.protocol.reflect.instances.PrimitiveGenerator;

/**
 * Injection method that overrides the NetworkHandler itself, and it's sendPacket-method.
 * 
 * @author Kristian
 */
public class NetworkObjectInjector extends PlayerInjector {
	public NetworkObjectInjector(Player player, PacketFilterManager manager, Set<Integer> sendingFilters) throws IllegalAccessException {
		super(player, manager, sendingFilters);
	}

	// This is why we don't normally use this method. It will create two extra threads per user.
	private Set<Long> threadKillList = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
	
	@Override
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		Object networkDelegate = filtered ? networkManagerRef.getValue() : networkManagerRef.getOldValue();
		
		if (networkDelegate != null) {
			try {
				// Note that invocation target exception is a wrapper for a checked exception
				queueMethod.invoke(networkDelegate, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access queue method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load network mananager. Cannot send packet.");
		}
	}
	
	@Override
	public void injectManager() {
		
		if (networkManager != null) {
			final Object networkDelegate = networkManagerRef.getOldValue();
			final Class<?> networkType = networkManager.getClass();
			final Enhancer ex = new Enhancer();
			
			ex.setSuperclass(networkType);
			ex.setClassLoader(manager.getClassLoader());
			ex.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					
					// Kill this thread?
					if (threadKillList.size() > 0) {
						Thread current = Thread.currentThread();
						
						if (threadKillList.contains(current.getId())) {
							// Yes, die!
							threadKillList.remove(current.getId());
							System.out.println("[Thread " + current.getId() + "] I'm committing suicide!");
							
							// This is bad. Very bad. Thus, we prefer the NetworkFieldInjector ...
							throw new Error("Killing current thread. Ignore this.");
						}
					}
					
					// OH OH! The queue method!
					if (isEquivalentMethod(method, queueMethod)) {
						Packet packet = (Packet) args[0];
						
						if (packet != null) {
							packet = handlePacketRecieved(packet);
							
							// A NULL packet indicate cancelling
							if (packet != null)
								args[0] = packet;
							else
								return null;
						}
					}
					
					// Delegate to our underlying class
					try {
						return method.invoke(networkDelegate, args);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				}
			});
			
			// Hacks! Get your daily hack here!
			DefaultInstances generator = new DefaultInstances(PrimitiveGenerator.INSTANCE, new InstanceProvider() {
				@Override
				public Object create(@Nullable Class<?> type) {
					if (type.equals(Socket.class))
						try {
							return new MockSocket();
						} catch (Exception e) {
							return null;
						}
					else
						return null;
				}
				
			}) {
				@SuppressWarnings("unchecked")
				@Override
				protected <T> T createInstance(Class<T> type, Constructor<T> constructor, 
											   Class<?>[] types, Object[] params) {
					// Use cglib instead of standard reflection
					if (type.equals(networkType))
						return (T) ex.create(types, params);
					else
						return super.createInstance(type, constructor, types, params);
				}
			};

			// Create our proxy object
			Object networkProxy = generator.getDefault(networkType);
			
			// Get the two threads we'll have to kill
			try {
				for (Thread thread : networkModifier.withTarget(networkProxy).<Thread>withType(Thread.class).getValues()) {
					threadKillList.add(thread.getId());
				}
			} catch (FieldAccessException e) {
				// Oh damn
			}
			
			// Inject it, if we can.
			networkManagerRef.setValue(networkProxy);
		}
	}
	
	// See if the two methods are the same
	private boolean isEquivalentMethod(Method a, Method b) {
		return a.getName().equals(b.getName()) && 
			   Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
	}
	
	@Override
	public void cleanupAll() {
		// Clean up
		networkManagerRef.revertValue();
	}
}
