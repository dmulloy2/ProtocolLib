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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.bukkit.entity.Player;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.ExistingGenerator;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

/**
 * Represents a player hook into the NetServerHandler class. 
 * 
 * @author Kristian
 */
class NetworkServerInjector extends PlayerInjector {
	// Disconnected field
	public static final ReportType REPORT_ASSUMING_DISCONNECT_FIELD = new ReportType("Unable to find 'disconnected' field. Assuming %s.");
	public static final ReportType REPORT_DISCONNECT_FIELD_MISSING = new ReportType("Cannot find disconnected field. Is ProtocolLib up to date?");
	public static final ReportType REPORT_DISCONNECT_FIELD_FAILURE = new ReportType("Unable to update disconnected field. Player quit event may be sent twice.");
	
	private volatile static CallbackFilter callbackFilter;
	private volatile static boolean foundSendPacket;
	
	private volatile static Field disconnectField;
	private InjectedServerConnection serverInjection;
	
	// Determine if we're listening
	private IntegerSet sendingFilters;
	
	// Whether or not the player has disconnected
	private boolean hasDisconnected;
	
	// Used to copy fields
	private final ObjectWriter writer = new ObjectWriter();
	
	public NetworkServerInjector(
			ErrorReporter reporter, Player player, 
			ListenerInvoker invoker, IntegerSet sendingFilters, 
			InjectedServerConnection serverInjection) throws IllegalAccessException {
		
		super(reporter, player, invoker);
		this.sendingFilters = sendingFilters;
		this.serverInjection = serverInjection;
	}
	
	@Override
	protected boolean hasListener(int packetID) {
		return sendingFilters.contains(packetID);
	}

	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
		Object serverDelegate = filtered ? serverHandlerRef.getValue() : serverHandlerRef.getOldValue();
		
		if (serverDelegate != null) {
			try {
				if (marker != null) {
					queuedMarkers.put(packet, marker);
				}
				
				// Note that invocation target exception is a wrapper for a checked exception
				MinecraftMethods.getSendPacketMethod().invoke(serverDelegate, packet);
				
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
			Class<?> serverHandlerClass = MinecraftReflection.getNetServerHandlerClass();
			
			// Try to override the proxied object
			if (proxyServerField != null) {
				serverHandlerRef = new VolatileField(proxyServerField, serverHandler, true);
				serverHandler = serverHandlerRef.getValue();
				
				if (serverHandler == null)
					throw new RuntimeException("Cannot hook player: Inner proxy object is NULL.");
				else
					serverHandlerClass = serverHandler.getClass();
				
				// Try again
				if (tryInjectManager()) {
					// It worked - probably
					return;
				}
			}
			
			throw new RuntimeException(
					"Cannot hook player: Unable to find a valid constructor for the " 
						+ serverHandlerClass.getName() + " object.");
		}
	}

	private boolean tryInjectManager() {
		Class<?> serverClass = serverHandler.getClass();
		
		Enhancer ex = EnhancerFactory.getInstance().createEnhancer();
		Callback sendPacketCallback = new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				Object packet = args[0];
				
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
		
		// Share callback filter - that way, we avoid generating a new class for 
		// every logged in player.
		if (callbackFilter == null) {
			callbackFilter = new SendMethodFilter();
		}
		
		ex.setSuperclass(serverClass);
		ex.setCallbacks(new Callback[] { sendPacketCallback, noOpCallback });
		ex.setCallbackFilter(callbackFilter);
		
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
			// Did we override a sendPacket method?
			if (!foundSendPacket) {
				throw new IllegalArgumentException("Unable to find a sendPacket method in " + serverClass);
			}

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
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				// Oh well
			}
		}
		
		return null;
	}
	
	private Class<?> getFirstMinecraftSuperClass(Class<?> clazz) {
		if (MinecraftReflection.isMinecraftClass(clazz))
			return clazz;
		else if (clazz.equals(Object.class))
			return clazz;
		else
			return getFirstMinecraftSuperClass(clazz.getSuperclass());
	}
		
	@Override
	protected void cleanHook() {
		if (serverHandlerRef != null && serverHandlerRef.isCurrentSet()) {
			writer.copyTo(serverHandlerRef.getValue(), serverHandlerRef.getOldValue(), serverHandler.getClass());
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
			
			// Prevent the PlayerQuitEvent from being sent twice
			if (hasDisconnected) {
				setDisconnect(serverHandlerRef.getValue(), true);
			}
		}

		serverInjection.revertServerHandler(serverHandler);
	}
	
	@Override
	public void handleDisconnect() {
		hasDisconnected = true;
	}
	
	/**
	 * Set the disconnected field in a NetServerHandler.
	 * @param handler - the NetServerHandler.
	 * @param value - the new value.
	 */
	private void setDisconnect(Object handler, boolean value) {
		// Set it 
		try {
			// Load the field
			if (disconnectField == null) {
				disconnectField = FuzzyReflection.fromObject(handler).getFieldByName("disconnected.*");
			}
			FieldUtils.writeField(disconnectField, handler, value);
		
		} catch (IllegalArgumentException e) {			
			// Assume it's the first ...
			if (disconnectField == null) {
				disconnectField = FuzzyReflection.fromObject(handler).getFieldByType("disconnected", boolean.class);
				reporter.reportWarning(this, Report.newBuilder(REPORT_ASSUMING_DISCONNECT_FIELD).messageParam(disconnectField));
				
				// Try again
				if (disconnectField != null) {
					setDisconnect(handler, value);
					return;
				}
			}
			
			// This is really bad
			reporter.reportDetailed(this, Report.newBuilder(REPORT_DISCONNECT_FIELD_MISSING).error(e));
				
		} catch (IllegalAccessException e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_DISCONNECT_FIELD_FAILURE).error(e));
		}
	}
	
	@Override
	public UnsupportedListener checkListener(MinecraftVersion version, PacketListener listener) {
		// We support everything
		return null;
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
	
	/**
	 * Represents a CallbackFilter that only matches the SendPacket method.
	 * @author Kristian
	 */
	private static class SendMethodFilter implements CallbackFilter {
		private Method sendPacket = MinecraftMethods.getSendPacketMethod();
		
		@Override
		public int accept(Method method) {
			if (isCallableEqual(sendPacket, method)) {
				NetworkServerInjector.foundSendPacket = true;
				return 0;
			} else {
				return 1;
			}
		}
		
		/**
		 * Determine if the two methods are equal in terms of call semantics.
		 * <p>
		 * Two methods are equal if they have the same name, parameter types and return type.
		 * @param first - first method.
		 * @param second - second method.
		 * @return TRUE if they are, FALSE otherwise.
		 */
		private boolean isCallableEqual(Method first, Method second) {
			return first.getName().equals(second.getName()) &&
				   first.getReturnType().equals(second.getReturnType()) &&
				   Arrays.equals(first.getParameterTypes(), second.getParameterTypes());
		}
	}
}
