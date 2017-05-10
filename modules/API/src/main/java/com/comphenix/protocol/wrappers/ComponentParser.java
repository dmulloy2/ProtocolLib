/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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
package com.comphenix.protocol.wrappers;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;

/**
 * Handles component parsing in 1.8
 * @author dmulloy2
 */
public class ComponentParser {

	private ComponentParser() {
	}

	public static Object deserialize(Object gson, Class<?> component, StringReader str) {
		try {
			com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(str);
			reader.setLenient(true);
			return ((com.google.gson.Gson) gson).getAdapter(component).read(reader);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read JSON", ex);
		} catch (LinkageError er) {
			return deserializeLegacy(gson, component, str);
		}
	}

	// Should only be needed on 1.8.
	private static Object deserializeLegacy(Object gson, Class<?> component, StringReader str) {
		try {
			Class<?> readerClass = Class.forName("org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonReader");
			Object reader = readerClass.getConstructor(Reader.class).newInstance(str);
			Method setLenient = readerClass.getMethod("setLenienent", boolean.class);
			setLenient.invoke(reader, true);
			Method getAdapter = gson.getClass().getMethod("getAdapter", Class.class);
			Object adapter = getAdapter.invoke(gson, component);
			Method read = adapter.getClass().getMethod("read", readerClass);
			return read.invoke(adapter, reader);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to read JSON", ex);
		}
	}
}
