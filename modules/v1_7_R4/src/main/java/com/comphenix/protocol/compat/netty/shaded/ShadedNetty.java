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
package com.comphenix.protocol.compat.netty.shaded;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.buffer.UnpooledByteBufAllocator;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.handler.codec.base64.Base64;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.compat.netty.NettyCompat;
import com.comphenix.protocol.compat.netty.ProtocolInjector;
import com.comphenix.protocol.compat.netty.WrappedByteBuf;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;
import com.google.common.base.Charsets;

/**
 * @author dmulloy2
 */

public class ShadedNetty implements NettyCompat {

	@Override
	public WrappedByteBuf createPacketBuffer() {
		ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
		Class<?> packetSerializer = MinecraftReflection.getPacketDataSerializerClass();

		try {
			return new ShadedByteBuf((ByteBuf) packetSerializer.getConstructor(ByteBuf.class).newInstance(buffer));
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct packet serializer.", e);
		}
	}

	@Override
	public Class<?> getGenericFutureListenerArray() {
		return GenericFutureListener[].class;
	}

	@Override
	public Class<?> getChannelHandlerContext() {
		return ChannelHandlerContext.class;
	}

	@Override
	public String toEncodedText(CompressedImage image) {
		final ByteBuf buffer = Unpooled.wrappedBuffer(image.getDataCopy());
		String computed = "data:" + image.getMime() + ";base64," +
			Base64.encode(buffer).toString(Charsets.UTF_8);
		return computed;
	}

	@Override
	public WrappedByteBuf decode(byte[] encoded) {
		return new ShadedByteBuf(Base64.decode(Unpooled.wrappedBuffer(encoded)));
	}

	@Override
	public ProtocolInjector getProtocolInjector(Plugin plugin, ListenerInvoker invoker, ErrorReporter reporter) {
		return new ShadedProtocolInjector(plugin, invoker, reporter);
	}

	@Override
	public WrappedByteBuf packetReader(DataInputStream input) {
		return new ShadedByteBuf(ShadedByteBufAdapter.packetReader(input));
	}

	@Override
	public WrappedByteBuf packetWriter(DataOutputStream output) {
		return new ShadedByteBuf(ShadedByteBufAdapter.packetWriter(output));
	}

	@Override
	public WrappedByteBuf copiedBuffer(byte[] array) {
		return new ShadedByteBuf(Unpooled.copiedBuffer(array));
	}

	@Override
	public WrappedByteBuf buffer() {
		return new ShadedByteBuf(Unpooled.buffer());
	}
}