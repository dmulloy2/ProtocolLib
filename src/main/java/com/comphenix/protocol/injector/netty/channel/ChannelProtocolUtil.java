package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
final class ChannelProtocolUtil {

    public static final BiFunction<Channel, PacketType.Sender, PacketType.Protocol> PROTOCOL_RESOLVER;

    static {
        Class<?> networkManagerClass = MinecraftReflection.getNetworkManagerClass();
        List<Field> attributeKeys = FuzzyReflection.fromClass(networkManagerClass, true).getFieldList(FuzzyFieldContract.newBuilder()
                .typeExact(AttributeKey.class)
                .requireModifier(Modifier.STATIC)
                .declaringClassExactType(networkManagerClass)
                .build());

        BiFunction<Channel, PacketType.Sender, Object> baseResolver = null;
        if (attributeKeys.size() == 1) {
            // if there is only one attribute key we can assume it's the correct one (1.8 - 1.20.1)
            Object protocolKey = Accessors.getFieldAccessor(attributeKeys.get(0)).get(null);
            baseResolver = new Pre1_20_2DirectResolver((AttributeKey<Object>) protocolKey);
        } else if (attributeKeys.size() > 1) {
            // most likely 1.20.2+: 1 protocol key per protocol direction
            AttributeKey<Object> serverBoundKey = null;
            AttributeKey<Object> clientBoundKey = null;

            for (Field keyField : attributeKeys) {
                AttributeKey<Object> key = (AttributeKey<Object>) Accessors.getFieldAccessor(keyField).get(null);
                if (key.name().equals("protocol")) {
                    // legacy (pre 1.20.2 name) - fall back to the old behaviour
                    baseResolver = new Pre1_20_2DirectResolver(key);
                    break;
                }

                if (key.name().contains("protocol")) {
                    // one of the two protocol keys for 1.20.2
                    if (key.name().contains("server")) {
                        serverBoundKey = key;
                    } else {
                        clientBoundKey = key;
                    }
                }
            }

            if (baseResolver == null) {
                if ((serverBoundKey == null || clientBoundKey == null)) {
                    // neither pre 1.20.2 key nor 1.20.2+ keys are available
                    throw new ExceptionInInitializerError("Unable to resolve protocol state attribute keys");
                } else {
                    baseResolver = new Post1_20_2WrappedResolver(serverBoundKey, clientBoundKey);
                }
            }
        } else {
            throw new ExceptionInInitializerError("Unable to resolve protocol state attribute key(s)");
        }

        // decorate the base resolver by wrapping its return value into our packet type value
        PROTOCOL_RESOLVER = baseResolver.andThen(nmsProtocol -> PacketType.Protocol.fromVanilla((Enum<?>) nmsProtocol));
    }

    private static final class Pre1_20_2DirectResolver implements BiFunction<Channel, PacketType.Sender, Object> {

        private final AttributeKey<Object> attributeKey;

        public Pre1_20_2DirectResolver(AttributeKey<Object> attributeKey) {
            this.attributeKey = attributeKey;
        }

        @Override
        public Object apply(Channel channel, PacketType.Sender sender) {
            return channel.attr(this.attributeKey).get();
        }
    }

    private static final class Post1_20_2WrappedResolver implements BiFunction<Channel, PacketType.Sender, Object> {

        private final AttributeKey<Object> serverBoundKey;
        private final AttributeKey<Object> clientBoundKey;

        // lazy initialized when needed
        private FieldAccessor protocolAccessor;

        public Post1_20_2WrappedResolver(AttributeKey<Object> serverBoundKey, AttributeKey<Object> clientBoundKey) {
            this.serverBoundKey = serverBoundKey;
            this.clientBoundKey = clientBoundKey;
        }

        @Override
        public Object apply(Channel channel, PacketType.Sender sender) {
            AttributeKey<Object> key = this.getKeyForSender(sender);
            Object codecData = channel.attr(key).get();
            if (codecData == null) {
                return null;
            }

            FieldAccessor protocolAccessor = this.getProtocolAccessor(codecData.getClass());
            return protocolAccessor.get(codecData);
        }

        private AttributeKey<Object> getKeyForSender(PacketType.Sender sender) {
            switch (sender) {
                case SERVER:
                    return this.clientBoundKey;
                case CLIENT:
                    return this.serverBoundKey;
                default:
                    throw new IllegalArgumentException("Illegal packet sender " + sender.name());
            }
        }

        private FieldAccessor getProtocolAccessor(Class<?> codecClass) {
            if (this.protocolAccessor == null) {
                Class<?> enumProtocolClass = MinecraftReflection.getEnumProtocolClass();
                this.protocolAccessor = Accessors.getFieldAccessor(codecClass, enumProtocolClass, true);
            }

            return this.protocolAccessor;
        }
    }
}
