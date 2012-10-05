package com.comphenix.protocol.injector.player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import com.comphenix.protocol.injector.player.NetworkFieldInjector.FakePacket;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
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
	
	private PlayerInjector injector;
	private Set<Packet> ignoredPackets;
	private ClassLoader classLoader;
	
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
			result = injector.handlePacketRecieved(packet);
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
		Enhancer ex = new Enhancer();
		Class<?> type = source.getClass();
		
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
		ex.setInterfaces(new Class[] { FakePacket.class } );
		ex.setUseCache(true);
		ex.setClassLoader(classLoader);
		ex.setSuperclass(type);
		ex.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (method.getReturnType().equals(int.class) && args.length == 0) {
					Integer result = (Integer) proxy.invokeSuper(obj, args);
					return -result;
				} else {
					return proxy.invokeSuper(obj, args);
				}
			}
		});
		
		return (Packet) ex.create();
	}
}
