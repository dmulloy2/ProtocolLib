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

import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.player.NetworkFieldInjector.FakePacket;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * The array list that notifies when packets are sent by the server.
 * 
 * @author Kristian
 */
class InjectedArrayList extends ArrayList<Packet> {

	/**
	 * Silly Eclipse.
	 */
	private static final long serialVersionUID = -1173865905404280990L;
	
	private transient PlayerInjector injector;
	private transient Set<Packet> ignoredPackets;
	private transient ClassLoader classLoader;
	
	public InjectedArrayList(ClassLoader classLoader, PlayerInjector injector, Set<Packet> ignoredPackets) {
		this.classLoader = classLoader;
		this.injector = injector;
		this.ignoredPackets = ignoredPackets;
	}

	@Override
	public boolean add(Packet packet) {

		Packet result = null;
		
		// Check for fake packets and ignored packets
		if (packet instanceof FakePacket) {
			return true;
		} else if (ignoredPackets.contains(packet)) {
			ignoredPackets.remove(packet);
		} else {
			result = injector.handlePacketSending(packet);
		}
		
		// A NULL packet indicate cancelling
		try {
			if (result != null) {
				super.add(result);
			} else {
				// We'll use the FakePacket marker instead of preventing the filters
				injector.sendServerPacket(createNegativePacket(packet), true);
			}

			// Collection.add contract
			return true;
			
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Reverting cancelled packet failed.", e.getTargetException());
		}
	}
	
	/**
	 * Used by a hack that reverses the effect of a cancelled packet. Returns a packet
	 * whereby every int method's return value is inverted (a => -a).
	 * 
	 * @param source - packet to invert.
	 * @return The inverted packet.
	 */
	Packet createNegativePacket(Packet source) {
		ListenerInvoker invoker = injector.getInvoker();
		
		int packetID = invoker.getPacketID(source);
		Class<?> type = invoker.getPacketClassFromID(packetID, true);

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
		Enhancer ex = new Enhancer();
		ex.setSuperclass(type);
		ex.setInterfaces(new Class[] { FakePacket.class } );
		ex.setUseCache(true);
		ex.setClassLoader(classLoader);
		ex.setCallbackType(InvertedIntegerCallback.class);

		Class<?> proxyClass = ex.createClass();

		// Temporarily associate the fake packet class
		invoker.registerPacketClass(proxyClass, packetID);

		Packet fake = (Packet) Enhancer.create(proxyClass, new InvertedIntegerCallback());
		
		// Remove this association
		invoker.unregisterPacketClass(proxyClass);
		return fake;
	}
	
	/**
	 * Inverts the integer result of every integer method.
	 * @author Kristian
	 */
	private class InvertedIntegerCallback implements MethodInterceptor {
		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if (method.getReturnType().equals(int.class) && args.length == 0) {
				Integer result = (Integer) proxy.invokeSuper(obj, args);
				return -result;
			} else {
				return proxy.invokeSuper(obj, args);
			}
		}
	}
}
