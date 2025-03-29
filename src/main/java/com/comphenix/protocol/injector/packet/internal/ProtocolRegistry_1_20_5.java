package com.comphenix.protocol.injector.packet.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.injector.packet.PacketRegistry.Register;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Class only works for version 1.20.5+
 */
public class ProtocolRegistry_1_20_5 {

    private static final Class<?> PACKET_TYPE_CLASS = MinecraftReflection.getMinecraftClass("network.protocol.PacketType");;

    /**
     * List of all class containing PacketTypes
     */
    public static final List<String> PACKET_TYPES_CLASS_NAMES = List.of(
            "common.CommonPacketTypes",
            "configuration.ConfigurationPacketTypes",
            "cookie.CookiePacketTypes",
            "game.GamePacketTypes",
            "handshake.HandshakePacketTypes",
            "login.LoginPacketTypes",
            "ping.PingPacketTypes",
            "status.StatusPacketTypes");

    /**
     * List of all class containing ProtocolInfos
     */
    public static final List<Protocol> PROTOCOLS = List.of(
            new Protocol("configuration.ConfigurationProtocols", null),
            new Protocol("game.GameProtocols", ProtocolContexts::createGameProtocolContext),
            new Protocol("handshake.HandshakeProtocols", null),
            new Protocol("login.LoginProtocols", null),
            new Protocol("status.StatusProtocols", null));

    public static void fillRegister(Register register) {
        final Map<Object, Class<?>> packetTypeMap = getPacketTypes();

        for (Protocol protocol : PROTOCOLS) {
            Class<?> protocolClass = MinecraftReflection.getNullableNMS("network.protocol." + protocol.className());
            if (protocolClass == null) {
                ProtocolLogger.debug("Can't find protocol class: {0}, will skip it", protocol);
                continue;
            }

            for (Field field : protocolClass.getDeclaredFields()) {
                try {
                    // ignore none static and final fields
                    if (!Modifier.isFinal(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    Object fieldValue = field.get(null);
                    
                    // Skip any field that isn't a unbound ProtocolInfo.
                    // We need to bind the ProtocolInfo ourself as we apply our own stream mapping
                    // inside the ProtocolInfoWrapper::fromUnbound method
                    if (!ProtocolInfoWrapper.isUnboundProtocol(fieldValue)) {
                        continue;
                    }

                    // create optional context object to bind protocol to
                    Object context = protocol.context() != null
                            ? protocol.context().get()
                            : null;

                    // wrap bound protocol info for ease of use
                    ProtocolInfoWrapper protocolInfo = ProtocolInfoWrapper.fromUnbound(fieldValue, context);
                    PacketType.Protocol packetProtocol = protocolInfo.id();
                    PacketType.Sender packetSender = protocolInfo.flow();

                    // register each packet to Register instance
                    List<IdCodecWrapper.Entry> entries = protocolInfo.codec().getById();
                    for (int id = 0; id < entries.size(); id++) {
                        IdCodecWrapper.Entry entry = entries.get(id);

                        Class<?> packetClass = packetTypeMap.get(entry.type());
                        if (packetClass == null) {
                            throw new RuntimeException("packetType missing packet " + entry.type());
                        }

                        PacketType packetType = PacketType.fromCurrent(packetProtocol, packetSender, id, packetClass);
                        // TODO(fix): validate that every codec pass in and expects FriendlyByteBuf
                        register.registerPacket(packetType, packetClass, packetSender, entry.serializer());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * Returns a PacketType to packet class lookup map
     */
    private static Map<Object, Class<?>> getPacketTypes() {
        Map<Object, Class<?>> packetTypeMap = new HashMap<>();

        for (String packetTypesClassName : PACKET_TYPES_CLASS_NAMES) {
            Class<?> packetTypesClass = MinecraftReflection.getNullableNMS("network.protocol." + packetTypesClassName);
            if (packetTypesClass == null) {
                ProtocolLogger.debug("Can't find PacketType class: {0}, will skip it", packetTypesClassName);
                continue;
            }

            // check every field for "static final PacketType<?>"
            for (Field field : packetTypesClass.getDeclaredFields()) {
                try {
                    if (!Modifier.isFinal(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    Object packetType = field.get(null);
                    if (!PACKET_TYPE_CLASS.isInstance(packetType)) {
                        continue;
                    }

                    // retrieve the generic type T of the PacketType<T> field
                    Type packet = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (packet instanceof Class<?>) {
                        packetTypeMap.put(packetType, (Class<?>) packet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }

        return packetTypeMap;
    }

    private record Protocol(@NotNull String className, @Nullable Supplier<Object> context) {
    }
}
