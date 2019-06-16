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

package com.comphenix.protocol.injector.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.NetworkProcessor;
import com.google.common.collect.MapMaker;

public class WritePacketModifier implements MethodInterceptor {
	public static final ReportType REPORT_CANNOT_WRITE_SERVER_PACKET = new ReportType("Cannot write server packet.");
	
	private static class ProxyInformation {
		// Marker that contains custom writers
		public final Object proxyObject;
		public final PacketEvent event;
		public final NetworkMarker marker;

		public ProxyInformation(Object proxyObject, PacketEvent event, NetworkMarker marker) {
			this.proxyObject = proxyObject;
			this.event = event;
			this.marker = marker;
		}
	}
	
	private Map<Object, ProxyInformation> proxyLookup = new MapMaker().weakKeys().makeMap();

	// Report errors
	private final ErrorReporter reporter;
	private final NetworkProcessor processor;
	
	// Whether or not this represents the write method
	private boolean isWriteMethod;
	
	public WritePacketModifier(ErrorReporter reporter,  boolean isWriteMethod) {
		this.reporter = reporter;
		this.processor = new NetworkProcessor(reporter);
		this.isWriteMethod = isWriteMethod;
	}

	/**
	 * Associate the given generated instance of a class and the given parameteters.
	 * @param generatedClass - the generated class.
	 * @param proxyObject - the object to call from the generated class.
	 * @param event - the packet event.
	 * @param marker - the network marker.
	 */
	public void register(Object generatedClass, Object proxyObject, PacketEvent event, NetworkMarker marker) {
		proxyLookup.put(generatedClass, new ProxyInformation(proxyObject, event, marker));
	}
	
	@Override
	public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		ProxyInformation information = proxyLookup.get(thisObj);
		
		if (information == null) {
			// This is really bad - someone forgot to register the proxy
			throw new RuntimeException("Cannot find proxy information for " + thisObj);
		}
		
		if (isWriteMethod) {
			// If every output handler has been removed - ignore everything
			if (!information.marker.getOutputHandlers().isEmpty()) {
				try {
					DataOutput output = (DataOutput) args[0];

					// First - we need the initial buffer
					ByteArrayOutputStream outputBufferStream = new ByteArrayOutputStream();
					proxy.invoke(information.proxyObject, new Object[] { new DataOutputStream(outputBufferStream) });
					
					// Let each handler prepare the actual output
					byte[] outputBuffer = processor.processOutput(information.event, information.marker, 
							outputBufferStream.toByteArray());
					
					// Write that output to the network stream
					output.write(outputBuffer);

					// We're done
					processor.invokePostEvent(information.event, information.marker);
					return null;
					
				} catch (OutOfMemoryError e) {
					throw e;
				} catch (ThreadDeath e) {
					throw e;
				} catch (Throwable e) {
					// Minecraft cannot handle this error
					reporter.reportDetailed(this, 
							Report.newBuilder(REPORT_CANNOT_WRITE_SERVER_PACKET).callerParam(args[0]).error(e)
					);
				}
			}
			
			// Invoke this write method first
			proxy.invoke(information.proxyObject, args);
			processor.invokePostEvent(information.event, information.marker);
			return null;
		}
		
		// Default to the super method
		return proxy.invoke(information.proxyObject, args);
	}
}
