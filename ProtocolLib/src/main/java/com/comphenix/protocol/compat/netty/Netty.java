/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.compat.netty;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.compat.netty.independent.IndependentNetty;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;

/**
 * @author dmulloy2
 */

public class Netty {
	private static NettyCompat compat;

	static {
		try {
			Class.forName("io.netty.buffer.ByteBuf");
			compat = new IndependentNetty();
		} catch (ClassNotFoundException ex) {
			try {
				Class<?> clazz = Class.forName("com.comphenix.protocol.compat.netty.shaded.ShadedNetty");
				compat = (NettyCompat) clazz.newInstance();
			} catch (Exception ex1) {
				ProtocolLibrary.getStaticLogger().log(Level.SEVERE, "Failed to create legacy netty compat:", ex1);
			}
		}
	}

	private static NettyCompat getCompat() {
		return compat;
	}

	public static WrappedByteBuf createPacketBuffer() {
		return getCompat().createPacketBuffer();
	}

	public static WrappedByteBuf allocateUnpooled() {
		return getCompat().allocateUnpooled();
	}

	public static Class<?> getGenericFutureListenerArray() {
		return getCompat().getGenericFutureListenerArray();
	}

	public static Class<?> getChannelHandlerContext() {
		return getCompat().getChannelHandlerContext();
	}

	public static String toEncodedText(CompressedImage image) {
		return getCompat().toEncodedText(image);
	}

	public static WrappedByteBuf decode(byte[] encoded) {
		return getCompat().decode(encoded);
	}

	public static ProtocolInjector getProtocolInjector(Plugin library, PacketFilterManager packetFilterManager, ErrorReporter reporter) {
		return getCompat().getProtocolInjector(library, packetFilterManager, reporter);
	}

	public static WrappedByteBuf packetReader(DataInputStream input) {
		return getCompat().packetReader(input);
	}

	public static WrappedByteBuf packetWriter(DataOutputStream output) {
		return getCompat().packetWriter(output);
	}
}