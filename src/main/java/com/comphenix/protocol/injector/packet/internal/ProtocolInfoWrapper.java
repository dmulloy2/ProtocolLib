package com.comphenix.protocol.injector.packet.internal;

import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AbstractWrapper;

/**
 * Class only works for version 1.20.5+
 */
public class ProtocolInfoWrapper extends AbstractWrapper {

    private static final @NotNull Class<?> SIMPLE_UNBOUND_PROTOCOL_CLASS;
    private static final @NotNull MethodAccessor BIND_SIMPLE_ACCESSOR;

    private static final @Nullable Class<?> UNBOUND_PROTOCOL_CLASS;
    private static final @Nullable MethodAccessor BIND_CONTEXT_ACCESSOR;

    private static final @NotNull MethodAccessor ID_ACCESSOR;
    private static final @NotNull MethodAccessor FLOW_ACCESSOR;
    private static final @NotNull MethodAccessor CODEC_ACCESSOR;

    static {
        SIMPLE_UNBOUND_PROTOCOL_CLASS = MinecraftReflection.getMinecraftClass(
                "network.protocol.SimpleUnboundProtocol" /* 1.21.5+ */,
                "network.protocol.UnboundProtocol" /* 1.21.8+ Alternative */,
                "network.ProtocolInfo$a" /* Spigot Mappings */,
                "network.ProtocolInfo$Unbound" /* Mojang Mappings */);
        BIND_SIMPLE_ACCESSOR = Accessors.getMethodAccessor(FuzzyReflection.fromClass(SIMPLE_UNBOUND_PROTOCOL_CLASS)
                .getMethodByReturnTypeAndParameters("bind", MinecraftReflection.getProtocolInfoClass(), new Class[] { Function.class }));

        UNBOUND_PROTOCOL_CLASS = MinecraftReflection.getNullableNMS("network.protocol.UnboundProtocol");
        if (UNBOUND_PROTOCOL_CLASS != null) {
            BIND_CONTEXT_ACCESSOR = Accessors.getMethodAccessor(FuzzyReflection.fromClass(UNBOUND_PROTOCOL_CLASS)
                    .getMethodByReturnTypeAndParameters("bind", MinecraftReflection.getProtocolInfoClass(), new Class[] { Function.class, Object.class }));
        } else {
            BIND_CONTEXT_ACCESSOR = null;
        }

        FuzzyReflection protocolInfoReflection = FuzzyReflection.fromClass(MinecraftReflection.getProtocolInfoClass());
        ID_ACCESSOR = Accessors.getMethodAccessor(protocolInfoReflection
                .getMethodByReturnTypeAndParameters("id", MinecraftReflection.getEnumProtocolClass(), new Class[0]));
        FLOW_ACCESSOR = Accessors.getMethodAccessor(protocolInfoReflection
                .getMethodByReturnTypeAndParameters("flow", MinecraftReflection.getPacketFlowClass(), new Class[0]));
        CODEC_ACCESSOR = Accessors.getMethodAccessor(protocolInfoReflection
                .getMethodByReturnTypeAndParameters("codec", MinecraftReflection.getStreamCodecClass(), new Class[0]));
    }

    public static boolean isUnboundProtocol(Object protocol) {
        return SIMPLE_UNBOUND_PROTOCOL_CLASS.isInstance(protocol) || (UNBOUND_PROTOCOL_CLASS != null && UNBOUND_PROTOCOL_CLASS.isInstance(protocol));
    }

    public static ProtocolInfoWrapper fromUnbound(Object protocol, Object context) {
        // bind to identity function in-order to map directly to FriendlyByteBuf instead of ByteBuf

        if (SIMPLE_UNBOUND_PROTOCOL_CLASS.isInstance(protocol)) {
            Object protocolInfo = BIND_SIMPLE_ACCESSOR.invoke(protocol, Function.identity());
            return new ProtocolInfoWrapper(protocolInfo);
        }

        if (UNBOUND_PROTOCOL_CLASS != null && UNBOUND_PROTOCOL_CLASS.isInstance(protocol)) {
            Object protocolInfo = BIND_CONTEXT_ACCESSOR.invoke(protocol, Function.identity(), Objects.requireNonNull(context));
            return new ProtocolInfoWrapper(protocolInfo);
        }

        throw new RuntimeException("Unknown protocol implementation: " + protocol);
    }

    private ProtocolInfoWrapper(Object handle) {
        super(MinecraftReflection.getProtocolInfoClass());
        setHandle(handle);
    }

    public PacketType.Protocol id() {
        Object protocol = ID_ACCESSOR.invoke(this.handle);
        return PacketType.Protocol.fromVanilla((Enum<?>) protocol);
    }

    public PacketType.Sender flow() {
        String packetFlow = FLOW_ACCESSOR.invoke(this.handle).toString();

        if (packetFlow.contains("CLIENTBOUND")) {
            return PacketType.Sender.SERVER;
        } else if (packetFlow.contains("SERVERBOUND")) {
            return PacketType.Sender.CLIENT;
        }

        throw new RuntimeException("Unknown packet flow: " + packetFlow);
    }

    public IdCodecWrapper codec() {
        return new IdCodecWrapper(CODEC_ACCESSOR.invoke(this.handle));
    }
}
