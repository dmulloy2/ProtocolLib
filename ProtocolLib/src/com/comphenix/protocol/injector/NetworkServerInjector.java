package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectCloner;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.ExistingGenerator;

/**
 * Represents a player hook into the NetServerHandler class. 
 * 
 * @author Kristian
 */
public class NetworkServerInjector extends PlayerInjector {

	private static Method sendPacketMethod;
	private InjectedServerConnection serverInjection;
	
	public NetworkServerInjector(Player player, PacketFilterManager manager, 
								 Set<Integer> sendingFilters, InjectedServerConnection serverInjection) throws IllegalAccessException {
		super(player, manager, sendingFilters);
		this.serverInjection = serverInjection;
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
		Callback sendPacketCallback = new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

				Packet packet = (Packet) args[0];
				
				if (packet != null) {
					packet = handlePacketRecieved(packet);
					
					// A NULL packet indicate cancelling
					if (packet != null)
						args[0] = packet;
					else
						return null;
				}
				
				// Call the method directly
				return proxy.invokeSuper(obj, args);
			};
		};
		Callback noOpCallback = NoOp.INSTANCE;

		ex.setClassLoader(manager.getClassLoader());
		ex.setSuperclass(serverClass);
		ex.setCallbacks(new Callback[] { sendPacketCallback, noOpCallback });
		ex.setCallbackFilter(new CallbackFilter() {
			@Override
			public int accept(Method method) {
				if (method.equals(sendPacketMethod))
					return 0;
				else
					return 1;
			}
		});
		
		// Use the existing field values when we create our copy
		DefaultInstances serverInstances = DefaultInstances.fromArray(
				ExistingGenerator.fromObjectFields(serverHandler));
		serverInstances.setNonNull(true);
		serverInstances.setMaximumRecursion(1);
		
		Object proxyObject = serverInstances.forEnhancer(ex).getDefault(serverClass);
		
		// Inject it now
		if (proxyObject != null) {
			// This will be done by InjectedServerConnection instead
			//copyTo(serverHandler, proxyObject);
			serverInjection.replaceServerHandler(serverHandler, proxyObject);
			serverHandlerRef.setValue(proxyObject);
		} else {
			throw new RuntimeException(
					"Cannot hook player: Unable to find a valid constructor for the NetServerHandler object.");
		}
	}
		
	@Override
	public void cleanupAll() {
		if (serverHandlerRef != null && serverHandlerRef.isCurrentSet()) {
			ObjectCloner.copyTo(serverHandlerRef.getValue(), serverHandlerRef.getOldValue(), serverHandler.getClass());
			serverHandlerRef.revertValue();
			
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

		serverInjection.revertServerHandler(serverHandler);
	}
	
	@Override
	public void checkListener(PacketListener listener) {
		// We support everything
	}
}
