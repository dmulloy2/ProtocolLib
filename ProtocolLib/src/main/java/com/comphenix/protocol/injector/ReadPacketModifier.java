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

package com.comphenix.protocol.injector;

import java.io.DataInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

class ReadPacketModifier implements MethodInterceptor {

	@SuppressWarnings("rawtypes")
	private static Class[] parameters = { DataInputStream.class };

	// A cancel marker
	private static final Object CANCEL_MARKER = new Object();
	
	// Common for all packets of the same type
	private PacketInjector packetInjector;
	private int packetID;
	
	// Report errors
	private ErrorReporter reporter;
	
	// Whether or not a packet has been cancelled
	private static Map<Object, Object> override = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
	
	public ReadPacketModifier(int packetID, PacketInjector packetInjector, ErrorReporter reporter) {
		this.packetID = packetID;
		this.packetInjector = packetInjector;
		this.reporter = reporter;
	}
	
	/**
	 * Remove any packet overrides.
	 * @param packet - the packet to rever
	 */
	public void removeOverride(Object packet) {
		override.remove(packet);
	}
	
	/**
	 * Retrieve the packet that overrides the methods of the given packet.
	 * @param packet - the given packet.
	 * @return Overriden object.
	 */
	public Object getOverride(Object packet) {
		return override.get(packet);
	}

	/**
	 * Determine if the given packet has been cancelled before.
	 * @param packet - the packet to check.
	 * @return TRUE if it has been cancelled, FALSE otherwise.
	 */
	public boolean hasCancelled(Object packet) {
		return getOverride(packet) == CANCEL_MARKER;
	}
	
	@Override
	public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		
		Object returnValue = null;
		String methodName = method.getName();

		// We always pass these down (otherwise, we'll end up with a infinite loop)
		if (methodName.equals("hashCode") || methodName.equals("equals") || methodName.equals("toString")) {
			return proxy.invokeSuper(thisObj, args);
		}
		
		// Atomic retrieval
		Object overridenObject = override.get(thisObj);
		
		if (overridenObject != null) {
			// This packet has been cancelled
			if (overridenObject == CANCEL_MARKER) {
				// So, cancel all void methods
				if (method.getReturnType().equals(Void.TYPE))
					return null;
				else // Revert to normal for everything else
					overridenObject = thisObj;
			}
			
			returnValue = proxy.invokeSuper(overridenObject, args);
		} else {
			returnValue = proxy.invokeSuper(thisObj, args);
		}
		
		// Is this a readPacketData method?
		if (returnValue == null && 
				Arrays.equals(method.getParameterTypes(), parameters)) {
			
			try {
				// We need this in order to get the correct player
				DataInputStream input = (DataInputStream) args[0];
	
				// Let the people know
				PacketContainer container = new PacketContainer(packetID, thisObj);
				PacketEvent event = packetInjector.packetRecieved(container, input);
				
				// Handle override
				if (event != null) {
					Object result = event.getPacket().getHandle();
					
					if (event.isCancelled()) {
						override.put(thisObj, CANCEL_MARKER);
					} else if (!objectEquals(thisObj, result)) {
						override.put(thisObj, result);
					}
					
					// Update DataInputStream next time
					if (!event.isCancelled() && packetID == Packets.Server.KEY_RESPONSE) {
						packetInjector.scheduleDataInputRefresh(event.getPlayer());
					}
				}
			} catch (Throwable e) {
				// Minecraft cannot handle this error
				reporter.reportDetailed(this, "Cannot handle clienet packet.", e, args[0]);
			}
		}
		
		return returnValue;
	}
	
	private boolean objectEquals(Object a, Object b) {
		return System.identityHashCode(a) != System.identityHashCode(b);
	}
}
