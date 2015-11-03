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

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;


/**
 * @author dmulloy2
 */

// TODO: Sort out packet readers/writers
public interface NettyCompat {

	WrappedByteBuf createPacketBuffer();

	WrappedByteBuf copiedBuffer(byte[] array);

	WrappedByteBuf buffer();

	Class<?> getGenericFutureListenerArray();

	Class<?> getChannelHandlerContext();

	String toEncodedText(CompressedImage image);

	WrappedByteBuf decode(byte[] encoded);

	ProtocolInjector getProtocolInjector(Plugin plugin, ListenerInvoker invoker, ErrorReporter reporter);

	WrappedByteBuf packetReader(DataInputStream input);

	WrappedByteBuf packetWriter(DataOutputStream output);
}