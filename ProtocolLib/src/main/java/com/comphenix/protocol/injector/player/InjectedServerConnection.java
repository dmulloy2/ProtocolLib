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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Factory;

import org.bukkit.Server;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Used to ensure that the 1.3 server is referencing the correct server handler.
 * 
 * @author Kristian
 */
class InjectedServerConnection {
	
	private static Field listenerThreadField;
	private static Field minecraftServerField;
	private static Field listField;
	private static Field dedicatedThreadField;
	
	private static Method serverConnectionMethod;
	
	private List<VolatileField> listFields;
	private List<ReplacedArrayList<Object>> replacedLists;

	// Used to inject net handlers
	private NetLoginInjector netLoginInjector;
	
	private Server server;
	private ErrorReporter reporter;
	private boolean hasAttempted;
	private boolean hasSuccess;
	
	private Object minecraftServer = null;
	
	public InjectedServerConnection(ErrorReporter reporter, Server server, NetLoginInjector netLoginInjector) {
		this.listFields = new ArrayList<VolatileField>();
		this.replacedLists = new ArrayList<ReplacedArrayList<Object>>();
		this.reporter = reporter;
		this.server = server;
		this.netLoginInjector = netLoginInjector;
	}

	public void injectList() {

		// Only execute this method once
		if (!hasAttempted)
			hasAttempted = true;
		else
			return;
		
		if (minecraftServerField == null)
			minecraftServerField = FuzzyReflection.fromObject(server, true).
				getFieldByType("MinecraftServer", MinecraftReflection.getMinecraftServerClass());

		try {
			minecraftServer = FieldUtils.readField(minecraftServerField, server, true);
		} catch (IllegalAccessException e1) {
			reporter.reportWarning(this, "Cannot extract minecraft server from Bukkit.");
			return;
		}
		
		try {
			if (serverConnectionMethod == null)
				serverConnectionMethod = FuzzyReflection.fromClass(minecraftServerField.getType()).
											getMethodByParameters("getServerConnection", ".*ServerConnection", new String[] {});
			// We're using Minecraft 1.3.1
			injectServerConnection();
		
		} catch (IllegalArgumentException e) {

			// Minecraft 1.2.5 or lower
			injectListenerThread();
			
		} catch (Exception e) {
			// Oh damn - inform the player
			reporter.reportDetailed(this, "Cannot inject into server connection. Bad things will happen.", e);
		}
	}
	
	private void injectListenerThread() {
	
		try {
		
		if (listenerThreadField == null)
			listenerThreadField = FuzzyReflection.fromObject(minecraftServer).
				getFieldByType(".*NetworkListenThread");
		} catch (RuntimeException e) {
			reporter.reportDetailed(this, "Cannot find listener thread in MinecraftServer.", e, minecraftServer);
			return;
		}

		Object listenerThread = null;
		
		// Attempt to get the thread
		try {
			listenerThread = listenerThreadField.get(minecraftServer);
		} catch (Exception e) {
			reporter.reportWarning(this, "Unable to read the listener thread.", e);
			return;
		}
		
		// Just inject every list field we can get
		injectEveryListField(listenerThread, 1);
		hasSuccess = true;
	}
	
	private void injectServerConnection() {
		
		Object serverConnection = null;
		
		// Careful - we might fail
		try {
			serverConnection = serverConnectionMethod.invoke(minecraftServer);
		} catch (Exception ex) {
			reporter.reportDetailed(this, "Unable to retrieve server connection", ex, minecraftServer);
			return;
		}
		
		if (listField == null)
			listField = FuzzyReflection.fromClass(serverConnectionMethod.getReturnType(), true).
							getFieldByType("netServerHandlerList", List.class);
		if (dedicatedThreadField == null) {
			List<Field> matches = FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(Thread.class);
		
			// Verify the field count
			if (matches.size() != 1) 
				reporter.reportWarning(this, "Unexpected number of threads in " + serverConnection.getClass().getName());
			else
				dedicatedThreadField = matches.get(0);
		}
		
		// Next, try to get the dedicated thread
		try {
			if (dedicatedThreadField != null)
				injectEveryListField(FieldUtils.readField(dedicatedThreadField, serverConnection, true), 1);
		} catch (IllegalAccessException e) {
			reporter.reportWarning(this, "Unable to retrieve net handler thread.", e);
		}
		
		injectIntoList(serverConnection, listField);
		hasSuccess = true;
	}
	
	/**
	 * Automatically inject into every List-compatible public or private field of the given object.
	 * @param container - container object with the fields to inject.
	 * @param minimum - the minimum number of fields we expect exists.
	 */
	private void injectEveryListField(Object container, int minimum) {
		// Ok, great. Get every list field
		List<Field> lists = FuzzyReflection.fromObject(container, true).getFieldListByType(List.class);
		
		for (Field list : lists) {
			injectIntoList(container, list);
		}
		
		// Warn about unexpected errors
		if (lists.size() < minimum) {
			reporter.reportWarning(this, "Unable to inject " + minimum + " lists in " + container.getClass().getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void injectIntoList(Object instance, Field field) {
		VolatileField listFieldRef = new VolatileField(field, instance, true);
		List<Object> list = (List<Object>) listFieldRef.getValue();

		// Careful not to inject twice
		if (list instanceof ReplacedArrayList) {
			replacedLists.add((ReplacedArrayList<Object>) list);
		} else {
			ReplacedArrayList<Object> injectedList = createReplacement(list);
			
			replacedLists.add(injectedList);
			listFieldRef.setValue(injectedList);
			listFields.add(listFieldRef);
		}
	}
	
	// Hack to avoid the "moved to quickly" error
	private ReplacedArrayList<Object> createReplacement(List<Object> list) {
		return new ReplacedArrayList<Object>(list) {
			/**
			 * Shut up Eclipse!
			 */
			private static final long serialVersionUID = 2070481080950500367L;

			@Override
			protected void onReplacing(Object inserting, Object replacement) {
				// Is this a normal Minecraft object?
				if (!(inserting instanceof Factory)) {
					// If so, copy the content of the old element to the new
					try {
						ObjectWriter.copyTo(inserting, replacement, inserting.getClass());
					} catch (Throwable e) {
						reporter.reportDetailed(InjectedServerConnection.this, "Cannot copy old " + inserting + 
								 				" to new.", e, inserting, replacement);
					}
				}
			}
			
			@Override
			protected void onInserting(Object inserting) {
				// Ready for some login handler injection?
				if (MinecraftReflection.isLoginHandler(inserting)) {
					Object replaced = netLoginInjector.onNetLoginCreated(inserting); 
					
					// Only replace if it has changed
					if (inserting != replaced)
						addMapping(inserting, replaced, true);
				}
			}
			
			@Override
			protected void onRemoved(Object removing) {
				// Clean up?
				if (MinecraftReflection.isLoginHandler(removing)) {
					netLoginInjector.cleanup(removing);
				} 
			}
		};
	}
	
	/**
	 * Replace the server handler instance kept by the "keep alive" object.
	 * @param oldHandler - old server handler.
	 * @param newHandler - new, proxied server handler.
	 */
	public void replaceServerHandler(Object oldHandler, Object newHandler) {
		if (!hasAttempted) {
			injectList();
		}
		
		if (hasSuccess) {
			for (ReplacedArrayList<Object> replacedList : replacedLists) {
				replacedList.addMapping(oldHandler, newHandler);
			}
		}
	}
	
	/**
	 * Revert to the old vanilla server handler, if it has been replaced.
	 * @param oldHandler - old vanilla server handler.
	 */
	public void revertServerHandler(Object oldHandler) {
		if (hasSuccess) {
			for (ReplacedArrayList<Object> replacedList : replacedLists) {
				replacedList.removeMapping(oldHandler);
			}
		}
	}
	
	/**
	 * Undoes everything.
	 */
	public void cleanupAll() {
		if (replacedLists.size() > 0) {
			// Repair the underlying lists
			for (ReplacedArrayList<Object> replacedList : replacedLists) {
				replacedList.revertAll();
			}
			for (VolatileField field : listFields) {
				field.revertValue();
			}
			
			listFields.clear();
			replacedLists.clear();
		}
	}
}
