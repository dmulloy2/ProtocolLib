package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.instances.CollectionGenerator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.ExistingGenerator;
import com.comphenix.protocol.reflect.instances.PrimitiveGenerator;

/**
 * Represents a player hook into the NetServerHandler class. 
 * 
 * @author Kristian
 */
public class NetworkServerInjector extends PlayerInjector {

	private static Method sendPacketMethod;
	
	public NetworkServerInjector(Player player, PacketFilterManager manager, Set<Integer> sendingFilters) throws IllegalAccessException {
		super(player, manager, sendingFilters);
	}
	
	@Override
	protected void initialize() throws IllegalAccessException {
		super.initialize();
		
		// Get the send packet method!
		if (hasInitialized) {
			if (sendPacketMethod == null)
				sendPacketMethod = FuzzyReflection.fromObject(serverHandler).getMethodByName("sendPacket.*");
		}
	}

	@Override
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		Object serverDeleage = filtered ? serverHandlerRef.getValue() : serverHandlerRef.getOldValue();
		
		if (serverDeleage != null) {
			try {
				// Note that invocation target exception is a wrapper for a checked exception
				sendPacketMethod.invoke(serverDeleage, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access send packet method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load server handler. Cannot send packet.");
		}
	}

	@Override
	public void injectManager() {
		
		if (serverHandlerRef == null)
			throw new IllegalStateException("Cannot find server handler.");
		// Don't inject twice
		if (serverHandlerRef.getValue() instanceof Factory)
			return;
		
		Class<?> serverClass = serverHandler.getClass();
		
		Enhancer ex = new Enhancer();
		ex.setClassLoader(manager.getClassLoader());
		ex.setSuperclass(serverClass);
		ex.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				
				// The send packet method!
				if (method.equals(sendPacketMethod)) {
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
					return method.invoke(serverHandler, args);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
			}
		});
		
		// Use the existing field values when we create our copy
		DefaultInstances serverInstances = DefaultInstances.fromArray(
				ExistingGenerator.fromObjectFields(serverHandler),
				PrimitiveGenerator.INSTANCE, 
				CollectionGenerator.INSTANCE);

		Object proxyObject = serverInstances.forEnhancer(ex).getDefault(serverClass);

		// Inject it now
		if (proxyObject != null) {
			serverHandlerRef.setValue(proxyObject);
		} else {
			throw new RuntimeException(
					"Cannot hook player: Unable to find a valid constructor for the NetServerHandler object.");
		}
	}

	@Override
	public void cleanupAll() {
		if (serverHandlerRef != null) {
			serverHandlerRef.revertValue();
		}
		
		try {
			if (getNetHandler() != null) {
				// Restore packet listener
				try {
					FieldUtils.writeField(netHandlerField, networkManager, serverHandlerRef.getOldValue(), true);
				} catch (IllegalAccessException e) {
					// Oh well
					e.printStackTrace();
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void checkListener(PacketListener listener) {
		// We support everything
	}
}
