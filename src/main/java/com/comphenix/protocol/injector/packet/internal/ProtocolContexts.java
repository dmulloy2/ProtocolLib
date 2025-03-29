package com.comphenix.protocol.injector.packet.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Class only works for version 1.21.5+
 */
public class ProtocolContexts {

    private static final Class<?> GAME_PROTOCOL_CONTEXT_CLASS;

    static {
        GAME_PROTOCOL_CONTEXT_CLASS = MinecraftReflection.getNullableNMS(
                "network.protocol.game.GameProtocols$a" /*Spigot Mapping*/,
                "network.protocol.game.GameProtocols$Context" /*Mojang Mapping*/);
    }

    public static Object createGameProtocolContext() {
        return createContext(GAME_PROTOCOL_CONTEXT_CLASS, (proxy, method, args) -> {
            if (method.getName().equals("hasInfiniteMaterials") || (method.getReturnType() == boolean.class && method.getParameterCount() == 0)) {
                return true;
            }
            return null;
        });
    }

    private static Object createContext(Class<?> interfaceClass, InvocationHandler handler) {
    	if (interfaceClass == null) {
    		return null;
    	}

        return Proxy.newProxyInstance(ProtocolContexts.class.getClassLoader(), new Class[] { interfaceClass }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return handler.invoke(proxy, method, args);
            }
        });
    }
}
