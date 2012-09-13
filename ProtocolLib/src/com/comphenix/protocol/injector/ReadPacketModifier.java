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
import java.util.WeakHashMap;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

class ReadPacketModifier implements MethodInterceptor {

	@SuppressWarnings("rawtypes")
	private static Class[] parameters = { DataInputStream.class };

	// Common for all packets of the same type
	private PacketInjector packetInjector;
	private int packetID;
	
	// Whether or not a packet has been cancelled
	private static WeakHashMap<Object, Object> override = new WeakHashMap<Object, Object>();
	
	public ReadPacketModifier(int packetID, PacketInjector packetInjector) {
		this.packetID = packetID;
		this.packetInjector = packetInjector;
	}

	@Override
	public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		
		Object returnValue = null;
		String methodName = method.getName();

		// We always pass these down (otherwise, we'll end up with a infinite loop)
		if (methodName.equals("hashCode") || methodName.equals("equals") || methodName.equals("toString")) {
			return proxy.invokeSuper(thisObj, args);
		}
		
		if (override.containsKey(thisObj)) {
			Object overridenObject = override.get(thisObj);
			
			// Cancel EVERYTHING, including "processPacket"
			if (overridenObject == null)
				return null;
			
			returnValue = proxy.invokeSuper(overridenObject, args);
		} else {
			returnValue = proxy.invokeSuper(thisObj, args);
		}
		
		// Is this a readPacketData method?
		if (returnValue == null && 
				Arrays.equals(method.getParameterTypes(), parameters)) {
			
			// We need this in order to get the correct player
			DataInputStream input = (DataInputStream) args[0];
			
			// Let the people know
			PacketContainer container = new PacketContainer(packetID, (Packet) thisObj);
			PacketEvent event = packetInjector.packetRecieved(container, input);
			Packet result = event.getPacket().getHandle();
			
			// Handle override
			if (event != null) {
				if (event.isCancelled()) {
					override.put(thisObj, null);
				} else if (!objectEquals(thisObj, result)) {
					override.put(thisObj, result);
				}
			}
		}
		
		return returnValue;
	}
	
	private boolean objectEquals(Object a, Object b) {
		return System.identityHashCode(a) != System.identityHashCode(b);
	}
}
