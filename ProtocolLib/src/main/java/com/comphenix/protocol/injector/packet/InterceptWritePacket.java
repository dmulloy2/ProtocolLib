package com.comphenix.protocol.injector.packet;

import java.io.DataOutput;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

/**
 * Retrieve a packet instance that has its write method intercepted.
 * @author Kristian
 */
public class InterceptWritePacket {
	public static final ReportType REPORT_CANNOT_FIND_WRITE_PACKET_METHOD = new ReportType("Cannot find write packet method in %s.");
	public static final ReportType REPORT_CANNOT_CONSTRUCT_WRITE_PROXY = new ReportType("Cannot construct write proxy packet %s.");
	
	/**
	 * Matches the readPacketData(DataInputStream) method in Packet.
	 */
	private static FuzzyMethodContract WRITE_PACKET = FuzzyMethodContract.newBuilder().
			returnTypeVoid().
			parameterDerivedOf(DataOutput.class).
			parameterCount(1).
			build();
	
	private CallbackFilter filter;
	private boolean writePacketIntercepted;
	
	private ConcurrentMap<Integer, Class<?>> proxyClasses = Maps.newConcurrentMap();
	private ErrorReporter reporter;
	
	private WritePacketModifier modifierWrite;
	private WritePacketModifier modifierRest;

	public InterceptWritePacket(ErrorReporter reporter) {
		this.reporter = reporter;
		
		// Initialize modifiers
		this.modifierWrite = new WritePacketModifier(reporter, true);
		this.modifierRest = new WritePacketModifier(reporter, false);
	}
	
	// TODO: PacketId should probably do something...
	private Class<?> createProxyClass() {
		// Construct the proxy object
		Enhancer ex = EnhancerFactory.getInstance().createEnhancer();
		
		// Attempt to share callback filter
		if (filter == null) {
			filter = new CallbackFilter() {
				@Override
				public int accept(Method method) {
					// Skip methods defined in Object
					if (WRITE_PACKET.isMatch(MethodInfo.fromMethod(method), null)) {
						writePacketIntercepted = true;
						return 0;
					} else {
						return 1;
					}
				}
			};
		}
		
		// Subclass the generic packet class
		ex.setSuperclass(MinecraftReflection.getPacketClass());
		ex.setCallbackFilter(filter);
		ex.setUseCache(false);
		
		ex.setCallbackTypes( new Class[] { WritePacketModifier.class, WritePacketModifier.class });
		Class<?> proxyClass = ex.createClass();
		
		// Register write modifiers too
		Enhancer.registerStaticCallbacks(proxyClass, new Callback[] { modifierWrite, modifierRest });

		if (proxyClass != null) {
			// Check that we found the read method
			if (!writePacketIntercepted) {
				reporter.reportWarning(this,
					Report.newBuilder(REPORT_CANNOT_FIND_WRITE_PACKET_METHOD).
						messageParam(MinecraftReflection.getPacketClass()));
			}
		}
		return proxyClass;
	}

	@SuppressWarnings("deprecation")
	private Class<?> getProxyClass(int packetId) {
		Class<?> stored = proxyClasses.get(packetId);
		
		// Concurrent pattern
		if (stored == null) {
			final Class<?> created = createProxyClass();
			stored = proxyClasses.putIfAbsent(packetId, created);
			
			// We won!
			if (stored == null) {
				stored = created;
				PacketRegistry.getPacketToID().put(stored, packetId);
			}
		}
		return stored;
	}
	
	/**
	 * Construct a new instance of the proxy object.
	 * @param proxyObject - Object to construct proxy of
	 * @param event - Packet event
	 * @param marker - Network marker
	 * @return New instance of the proxy, or null if we failed.
	 */
	@SuppressWarnings("deprecation")
	public Object constructProxy(Object proxyObject, PacketEvent event, NetworkMarker marker) {
		Class<?> proxyClass = null;
		
		try {
			proxyClass = getProxyClass(event.getPacketID());
			Object generated = proxyClass.newInstance();
			
			modifierWrite.register(generated, proxyObject, event, marker);
			modifierRest.register(generated, proxyObject, event, marker);
			return generated;
			
		} catch (Exception e) {
			reporter.reportWarning(this,
					Report.newBuilder(REPORT_CANNOT_CONSTRUCT_WRITE_PROXY).
						messageParam(proxyClass));
			return null;
		}
	}
	
	/**
	 * Invoked when the write packet proxy class should be removed.
	 */
	@SuppressWarnings("deprecation")
	public void cleanup() {
		// Remove all proxy classes from the registry
		for (Class<?> stored : proxyClasses.values()) {
			PacketRegistry.getPacketToID().remove(stored);
		}
	}
}
