package com.comphenix.protocol.injector.netty;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;

import net.minecraft.util.com.google.common.collect.Sets;
import net.minecraft.util.io.netty.channel.Channel;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

abstract class ChannelProxy {
	private static Set<Method> WRITE_METHODS;
	
	/**
	 * Retrieve the channel proxy object.
	 * @param proxyInstance - the proxy instance object.
	 * @return The channel proxy.
	 */
	public Channel asChannel(final Channel proxyInstance) {
		// Simple way to match all the write methods
		if (WRITE_METHODS == null) {
			List<Method> writers = FuzzyReflection.fromClass(Channel.class).
					getMethodList(FuzzyMethodContract.newBuilder().nameRegex("write.*").build());
			WRITE_METHODS = Sets.newHashSet(writers);
		}
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(Channel.class);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (WRITE_METHODS.contains(method)) {
					args[0] = onMessageWritten(args[0]);
					
					// If we should skip this object
					if (args[0] == null)
						return null;
				}
				// Forward to proxy
				return proxy.invoke(proxyInstance, args);
			}
		});
		return (Channel) enhancer.create();
	}
	
	/**
	 * Invoked when a packet is being transmitted.
	 * @param message - the packet to transmit.
	 * @return The object to transmit.
	 */
	protected abstract Object onMessageWritten(Object message);
}
