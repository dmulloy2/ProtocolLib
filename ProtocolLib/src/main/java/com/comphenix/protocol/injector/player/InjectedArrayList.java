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
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.player.NetworkFieldInjector.FakePacket;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.MapMaker;

/**
 * The array list that notifies when packets are sent by the server.
 * 
 * @author Kristian
 */
class InjectedArrayList extends ArrayList<Object> {
	public static final ReportType REPORT_CANNOT_REVERT_CANCELLED_PACKET = new ReportType("Reverting cancelled packet failed.");
	
	/**
	 * Silly Eclipse.
	 */
	private static final long serialVersionUID = -1173865905404280990L;
	
	// Fake inverted proxy objects
	private static ConcurrentMap<Object, Object> delegateLookup = new MapMaker().weakKeys().makeMap();
	
	private transient PlayerInjector injector;
	private transient Set<Object> ignoredPackets;
	
	private transient InvertedIntegerCallback callback;
	
	public InjectedArrayList(PlayerInjector injector, Set<Object> ignoredPackets) {
		this.injector = injector;
		this.ignoredPackets = ignoredPackets;
		this.callback = new InvertedIntegerCallback();
	}

	@Override
	public boolean add(Object packet) {

		Object result = null;
		
		// Check for fake packets and ignored packets
		if (packet instanceof FakePacket) {
			return true;
		} else if (ignoredPackets.contains(packet)) {
			// Don't send it to the filters
			result = ignoredPackets.remove(packet);
		} else {
			result = injector.handlePacketSending(packet);
		}
		
		// A NULL packet indicate cancelling
		try {
			if (result != null) {
				super.add(result);
			} else {
				// We'll use the FakePacket marker instead of preventing the filters
				injector.sendServerPacket(createNegativePacket(packet), null, true);
			}

			// Collection.add contract
			return true;
			
		} catch (InvocationTargetException e) {
			// Prefer to report this to the user, instead of risking sending it to Minecraft
			ProtocolLibrary.getErrorReporter().reportDetailed(this, 
					Report.newBuilder(REPORT_CANNOT_REVERT_CANCELLED_PACKET).error(e).callerParam(packet)
			);
			
			// Failure
			return false;
		}
	}
	
	/**
	 * Used by a hack that reverses the effect of a cancelled packet. Returns a packet
	 * whereby every int method's return value is inverted (a => -a).
	 * 
	 * @param source - packet to invert.
	 * @return The inverted packet.
	 */
	@SuppressWarnings("deprecation")
	Object createNegativePacket(Object source) {
		ListenerInvoker invoker = injector.getInvoker();
		
		PacketType type = invoker.getPacketType(source);

		// We want to subtract the byte amount that were added to the running
		// total of outstanding packets. Otherwise, cancelling too many packets
		// might cause a "disconnect.overflow" error.
		//
		// We do that by constructing a special packet of the same type that returns 
		// a negative integer for all zero-parameter integer methods. This includes the
		// size() method, which is used by the queue method to count the number of
		// bytes to add.
		//
		// Essentially, we have:
		//
		//   public class NegativePacket extends [a packet] {
		//      @Override
		//      public int size() {
		//         return -super.size();
		//      }
		//   ect.
		//   }
		Enhancer ex = EnhancerFactory.getInstance().createEnhancer();
		ex.setSuperclass(MinecraftReflection.getPacketClass());
		ex.setInterfaces(new Class[] { FakePacket.class } );
		ex.setUseCache(true);
		ex.setCallbackType(InvertedIntegerCallback.class);

		Class<?> proxyClass = ex.createClass();
		Enhancer.registerCallbacks(proxyClass, new Callback[] { callback });
		
		try {
			// Temporarily associate the fake packet class
			invoker.registerPacketClass(proxyClass, type.getLegacyId());
			Object proxy = proxyClass.newInstance();
			
			InjectedArrayList.registerDelegate(proxy, source);
			return proxy;
			
		} catch (Exception e) {
			// Don't pollute the throws tree
			throw new RuntimeException("Cannot create fake class.", e);
		} finally {
			// Remove this association
			invoker.unregisterPacketClass(proxyClass);
		}
	}
	
	/**
	 * Ensure that the inverted integer proxy uses the given object as source.
	 * @param proxy - inverted integer proxy.
	 * @param source - source object.
	 */
	private static void registerDelegate(Object proxy, Object source) {
		delegateLookup.put(proxy, source);
	}
	
	/**
	 * Inverts the integer result of every integer method.
	 * @author Kristian
	 */
	private class InvertedIntegerCallback implements MethodInterceptor {		
		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			final Object delegate = delegateLookup.get(obj);
			
			if (delegate == null) {
				throw new IllegalStateException("Unable to find delegate source for " + obj);
			}
				
			if (method.getReturnType().equals(int.class) && args.length == 0) {
				Integer result = (Integer) proxy.invoke(delegate, args);
				return -result;
			} else {
				return proxy.invoke(delegate, args);
			}
		}
	}
}
