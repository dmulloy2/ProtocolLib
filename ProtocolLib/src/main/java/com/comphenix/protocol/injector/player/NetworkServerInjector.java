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

package com.comphenix.protocol.injector.player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;

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
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectCloner;
import com.comphenix.protocol.reflect.VolatileField;
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
	
	// Determine if we're listening
	private Set<Integer> sendingFilters;
	
	// Used to create proxy objects
	private ClassLoader classLoader;
	
	public NetworkServerInjector(
			ClassLoader classLoader, Logger logger, Player player, 
			ListenerInvoker invoker, Set<Integer> sendingFilters, 
			InjectedServerConnection serverInjection) throws IllegalAccessException {
		
		super(logger, player, invoker);
		this.classLoader = classLoader;
		this.sendingFilters = sendingFilters;
		this.serverInjection = serverInjection;
	}
	
	@Override
	protected boolean hasListener(int packetID) {
		return sendingFilters.contains(packetID);
	}

	@Override
	public void initialize(Object injectionSource) throws IllegalAccessException {
		super.initialize(injectionSource);

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
		
		if (!tryInjectManager()) {
			
			// Try to override the proxied object
			if (proxyServerField != null) {
				serverHandlerRef = new VolatileField(proxyServerField, serverHandler, true);
				serverHandler = serverHandlerRef.getValue();
				
				if (serverHandler == null)
					throw new RuntimeException("Cannot hook player: Inner proxy object is NULL.");
				
				// Try again
				if (tryInjectManager()) {
					// It worked - probably
					return;
				}
			}
			
			throw new RuntimeException(
					"Cannot hook player: Unable to find a valid constructor for the NetServerHandler object.");
		}
	}
	
	private boolean tryInjectManager() {
		Class<?> serverClass = serverHandler.getClass();
		
		Enhancer ex = new Enhancer();
		Callback sendPacketCallback = new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

				Packet packet = (Packet) args[0];
				
				if (packet != null) {
					packet = handlePacketSending(packet);
					
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

		ex.setClassLoader(classLoader);
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
		
		// Find the Minecraft NetServerHandler superclass
		Class<?> minecraftSuperClass = getFirstMinecraftSuperClass(serverHandler.getClass());
		ExistingGenerator generator = ExistingGenerator.fromObjectFields(serverHandler, minecraftSuperClass);
		DefaultInstances serverInstances = null;
		
		// Maybe the proxy instance can help?
		Object proxyInstance = getProxyServerHandler();
		
		// Use the existing server proxy when we create one
		if (proxyInstance != null && proxyInstance != serverHandler) {
			serverInstances = DefaultInstances.fromArray(generator, 
					ExistingGenerator.fromObjectArray(new Object[] { proxyInstance }));
		} else {
			serverInstances = DefaultInstances.fromArray(generator);
		}
		
		serverInstances.setNonNull(true);
		serverInstances.setMaximumRecursion(1);
		
		Object proxyObject = serverInstances.forEnhancer(ex).getDefault(serverClass);
		
		// Inject it now
		if (proxyObject != null) {
			// This will be done by InjectedServerConnection instead
			//copyTo(serverHandler, proxyObject);
			serverInjection.replaceServerHandler(serverHandler, proxyObject);
			serverHandlerRef.setValue(proxyObject);
			return true;
		} else {
			return false;
		}
	}
	
	private Object getProxyServerHandler() {
		if (proxyServerField != null && !proxyServerField.equals(serverHandlerRef.getField())) {
			try {
				return FieldUtils.readField(proxyServerField, serverHandler, true);
			} catch (Throwable e) {
				// Oh well
			}
		}
		
		return null;
	}
	
	private Class<?> getFirstMinecraftSuperClass(Class<?> clazz) {
		if (clazz.getName().startsWith("net.minecraft.server."))
			return clazz;
		else if (clazz.equals(Object.class))
			return clazz;
		else
			return getFirstMinecraftSuperClass(clazz.getSuperclass());
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

	@Override
	public boolean canInject(GamePhase phase) {
		// Doesn't work when logging in
		return phase == GamePhase.PLAYING;
	}

	@Override
	public PlayerInjectHooks getHookType() {
		return PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	}
}
