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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelOption;
import net.minecraft.util.io.netty.channel.socket.SocketChannel;

/**
 * This class wraps a Netty {@link Channel} in a {@link Socket}. It overrides
 * all methods in {@link Socket} to ensure that calls are not mistakingly made
 * to the unsupported super socket. All operations that can be sanely applied to
 * a {@link Channel} are implemented here. Those which cannot will throw an
 * {@link UnsupportedOperationException}.
 */
// Thanks MD5. :)
public class ShadedSocketAdapter extends Socket {
    private final SocketChannel ch;

    private ShadedSocketAdapter(SocketChannel ch) {
        this.ch = ch;
    }

    public static ShadedSocketAdapter adapt(SocketChannel ch) {
        return new ShadedSocketAdapter(ch);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        ch.bind(bindpoint).syncUninterruptibly();
    }

    @Override
    public synchronized void close() throws IOException {
        ch.close().syncUninterruptibly();
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        ch.connect(endpoint).syncUninterruptibly();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        ch.config().setConnectTimeoutMillis(timeout);
        ch.connect(endpoint).syncUninterruptibly();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ShadedSocketAdapter && ch.equals(((ShadedSocketAdapter) obj).ch);
    }

    @Override
    public java.nio.channels.SocketChannel getChannel() {
         throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public InetAddress getInetAddress() {
        return ch.remoteAddress().getAddress();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_KEEPALIVE);
    }

    @Override
    public InetAddress getLocalAddress() {
        return ch.localAddress().getAddress();
    }

    @Override
    public int getLocalPort() {
        return ch.localAddress().getPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return ch.localAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public int getPort() {
        return ch.remoteAddress().getPort();
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_RCVBUF);
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return ch.remoteAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_REUSEADDR);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_SNDBUF);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_LINGER);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return ch.config().getOption(ChannelOption.TCP_NODELAY);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return ch.config().getOption(ChannelOption.IP_TOS);
    }

    @Override
    public int hashCode() {
        return ch.hashCode();
    }

    @Override
    public boolean isBound() {
        return ch.localAddress() != null;
    }

    @Override
    public boolean isClosed() {
        return !ch.isOpen();
    }

    @Override
    public boolean isConnected() {
        return ch.isActive();
    }

    @Override
    public boolean isInputShutdown() {
        return ch.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return ch.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        ch.config().setOption(ChannelOption.SO_KEEPALIVE, on);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        ch.config().setOption(ChannelOption.SO_RCVBUF, size);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        ch.config().setOption(ChannelOption.SO_REUSEADDR, on);
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        ch.config().setOption(ChannelOption.SO_SNDBUF, size);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        ch.config().setOption(ChannelOption.SO_LINGER, linger);
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        ch.config().setOption(ChannelOption.TCP_NODELAY, on);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        ch.config().setOption(ChannelOption.IP_TOS, tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void shutdownOutput() throws IOException {
        ch.shutdownOutput().syncUninterruptibly();
    }

    @Override
    public String toString() {
        return ch.toString();
    }
}
