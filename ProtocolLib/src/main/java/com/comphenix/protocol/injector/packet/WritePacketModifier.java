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
import java.util.PriorityQueue;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class WritePacketModifier implements MethodInterceptor {
	public static final ReportType REPORT_CANNOT_WRITE_SERVER_PACKET = new ReportType("Cannot write server packet.");
	
	// Report errors
	private final ErrorReporter reporter;
	
	// Marker that contains custom writers
	private final Object proxyObject;
	private final PacketEvent event;
	private final NetworkMarker marker;
	
	public WritePacketModifier(ErrorReporter reporter, Object proxyObject, PacketEvent event, NetworkMarker marker) {
		this.proxyObject = proxyObject;
		this.event = event;
		this.marker = marker;
		this.reporter = reporter;
	}

	@Override
	public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		PriorityQueue<PacketOutputHandler> handlers = (PriorityQueue<PacketOutputHandler>) marker.getOutputHandlers();

		// If every output handler has been removed - ignore everything
		if (!handlers.isEmpty()) {
			try {
				DataOutput output = (DataOutput) args[0];

				// First - we need the initial buffer
				ByteArrayOutputStream outputBufferStream = new ByteArrayOutputStream();
				proxy.invokeSuper(proxyObject, new Object[] { new DataOutputStream(outputBufferStream) });
				byte[] outputBuffer = outputBufferStream.toByteArray();
				
				// Let each handler prepare the actual output
				while (!handlers.isEmpty()) {
					PacketOutputHandler handler = handlers.poll();
					
					try {
						byte[] changed = handler.handle(event, outputBuffer);
						
						// Don't break just because a plugin returned NULL
						if (changed != null) {
							outputBuffer = changed;
						} else {
							throw new IllegalStateException("Handler cannot return a NULL array.");
						}
					} catch (Exception e) {
						reporter.reportMinimal(handler.getPlugin(), "PacketOutputHandler.handle()", e);
					}
				}

				// Write that output to the network stream
				output.write(outputBuffer);
				
			} catch (Throwable e) {
				// Minecraft cannot handle this error
				reporter.reportDetailed(this, 
						Report.newBuilder(REPORT_CANNOT_WRITE_SERVER_PACKET).callerParam(args[0]).error(e)
				);
			}
		}

		// Default to the super method
		return proxy.invokeSuper(proxyObject, args);
	}

	/**
	 * Retrieve the proxied Minecraft object.
	 * @return The proxied object.
	 */
	public Object getProxyObject() {
		return proxyObject;
	}

	/**
	 * Retrieve the associated packet event.
	 * @return The packet event.
	 */
	public PacketEvent getEvent() {
		return event;
	}

	/**
	 * Retrieve the network marker that is in use.
	 * @return The network marker.
	 */
	public NetworkMarker getMarker() {
		return marker;
	}
}
