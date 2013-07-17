package com.comphenix.protocol.injector.packet;

import java.io.DataOutput;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;

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
	
	private ClassLoader classLoader;
	private ErrorReporter reporter;

	private CallbackFilter filter;
	private boolean writePacketIntercepted;
	
	public InterceptWritePacket(ClassLoader classLoader, ErrorReporter reporter) {
		this.classLoader = classLoader;
		this.reporter = reporter;	
	}
	
	/**
	 * Construct a new instance of the proxy object.
	 * @return New instance of proxy.
	 */
	public Object constructProxy(Object proxyObject, PacketEvent event, NetworkMarker marker) {
		// Construct the proxy object
		Enhancer ex = new Enhancer();
		
		// Initialize the shared filter
		if (filter == null) {
			filter = new CallbackFilter() {
				@Override
				public int accept(Method method) {
					// Skip methods defined in Object
					if (WRITE_PACKET.isMatch(MethodInfo.fromMethod(method), null)) {
						return 1;
					} else {
						return 0;
					}
				}
			};
		}
		
		// Subclass the generic packet class
		ex.setSuperclass(MinecraftReflection.getPacketClass());
		ex.setCallbackFilter(filter);
		ex.setClassLoader(classLoader);
		ex.setCallbacks(new Callback[] { 
				NoOp.INSTANCE, 
				new WritePacketModifier(reporter, proxyObject, event, marker) 
		});
		
		Object proxy = ex.create();
		
		if (proxy != null) {
			// Check that we found the read method
			if (!writePacketIntercepted) {
				reporter.reportWarning(this, 
					Report.newBuilder(REPORT_CANNOT_FIND_WRITE_PACKET_METHOD).
						messageParam(MinecraftReflection.getPacketClass()));
			}
		}
		return proxy;
	}
}
