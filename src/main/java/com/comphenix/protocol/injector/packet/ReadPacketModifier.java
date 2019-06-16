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
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.NetworkProcessor;
import com.google.common.collect.MapMaker;

class ReadPacketModifier implements MethodInterceptor {
	public static final ReportType REPORT_CANNOT_HANDLE_CLIENT_PACKET = new ReportType("Cannot handle client packet.");
	
	// A cancel marker
	private static final Object CANCEL_MARKER = new Object();
	
	// Common for all packets of the same type
	private ProxyPacketInjector packetInjector;
	private int packetID;
	
	// Report errors
	private ErrorReporter reporter;
	private NetworkProcessor processor;
	
	// If this is a read packet data method
	private boolean isReadPacketDataMethod;
	
	// Whether or not a packet has been cancelled
	private static Map<Object, Object> override = new MapMaker().weakKeys().makeMap();

	public ReadPacketModifier(int packetID, ProxyPacketInjector packetInjector, ErrorReporter reporter, boolean isReadPacketDataMethod) {
		this.packetID = packetID;
		this.packetInjector = packetInjector;
		this.reporter = reporter;
		this.processor = new NetworkProcessor(reporter);
		this.isReadPacketDataMethod = isReadPacketDataMethod;
	}
	
	/**
	 * Remove any packet overrides.
	 * @param packet - the packet to rever
	 */
	public static void removeOverride(Object packet) {
		override.remove(packet);
	}
	
	/**
	 * Retrieve the packet that overrides the methods of the given packet.
	 * @param packet - the given packet.
	 * @return Overriden object.
	 */
	public static Object getOverride(Object packet) {
		return override.get(packet);
	}
	
	/**
	 * Set the packet instance to delegate to instead, or mark the packet as cancelled.
	 * <p>
	 * To undo a override, use {@link #removeOverride(Object)}.
	 * @param packet - the packet.
	 * @param override - the override method. NULL to cancel this packet.
	 */
	public static void setOverride(Object packet, Object overridePacket) {
		override.put(packet, overridePacket != null ? overridePacket : CANCEL_MARKER);
	}

	/**
	 * Determine if the given packet has been cancelled before.
	 * @param packet - the packet to check.
	 * @return TRUE if it has been cancelled, FALSE otherwise.
	 */
	public static boolean isCancelled(Object packet) {
		return getOverride(packet) == CANCEL_MARKER;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		// Atomic retrieval
		Object overridenObject = override.get(thisObj);
		Object returnValue = null;
		
		// We need this in order to get the correct player
		InputStream input = isReadPacketDataMethod ? (InputStream) args[0] : null;
		ByteArrayOutputStream bufferStream = null;
		
		// See if we need to buffer the read data
		if (isReadPacketDataMethod && packetInjector.requireInputBuffers(packetID)) {
			CaptureInputStream captured = new CaptureInputStream(
				input, bufferStream = new ByteArrayOutputStream()); 
			
			// Swap it with our custom stream
			args[0] = new DataInputStream(captured);
		}
		
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
		if (isReadPacketDataMethod) {
			// Swap back custom stream
			args[0] = input;
			
			try {
				byte[] buffer = bufferStream != null ? bufferStream.toByteArray() : null;
				
				// Let the people know
				PacketType type = PacketType.findLegacy(packetID, Sender.CLIENT);
				PacketContainer container = new PacketContainer(type, thisObj);
				PacketEvent event = packetInjector.packetRecieved(container, input, buffer);
				
				// Handle override
				if (event != null) {
					Object result = event.getPacket().getHandle();
					
					if (event.isCancelled()) {
						override.put(thisObj, CANCEL_MARKER);
						return returnValue;
					} else if (!objectEquals(thisObj, result)) {
						override.put(thisObj, result);
					}
					
					// This is fine - received packets are enqueued in any case
					NetworkMarker marker = NetworkMarker.getNetworkMarker(event);
					processor.invokePostEvent(event, marker);
				}
				
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				// Minecraft cannot handle this error
				reporter.reportDetailed(this, 
						Report.newBuilder(REPORT_CANNOT_HANDLE_CLIENT_PACKET).callerParam(args[0]).error(e)
				);
			}
		}
		return returnValue;
	}
	
	private boolean objectEquals(Object a, Object b) {
		return System.identityHashCode(a) != System.identityHashCode(b);
	}
}
