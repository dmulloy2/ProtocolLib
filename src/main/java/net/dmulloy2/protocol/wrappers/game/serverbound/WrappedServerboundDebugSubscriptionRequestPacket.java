package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Set;

/**
 * Wrapper for {@code ServerboundDebugSubscriptionRequestPacket} (game phase, serverbound).
 * Requests debug subscriptions.
 */
public class WrappedServerboundDebugSubscriptionRequestPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST;

    private static final EquivalentConverter<MinecraftKey> DEBUG_SUBSCRIPTION_CONVERTER =
            new EquivalentConverter<>() {
                @Override
                public Object getGeneric(MinecraftKey specific) {
                    return getDebugSubscriptionRegistry().valueConverter().getGeneric(specific);
                }

                @Override
                public MinecraftKey getSpecific(Object generic) {
                    return getDebugSubscriptionRegistry().valueConverter().getSpecific(generic);
                }

                @Override
                public Class<MinecraftKey> getSpecificType() {
                    return MinecraftKey.class;
                }
            };

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(Set.class, BukkitConverters.getSetConverter(DEBUG_SUBSCRIPTION_CONVERTER));

    public WrappedServerboundDebugSubscriptionRequestPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundDebugSubscriptionRequestPacket(Set<MinecraftKey> subscriptions) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(subscriptions)));
    }

    public WrappedServerboundDebugSubscriptionRequestPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Set<MinecraftKey> getSubscriptions() {
        return handle.getSets(DEBUG_SUBSCRIPTION_CONVERTER).read(0);
    }

    public void setSubscriptions(Set<MinecraftKey> subscriptions) {
        handle.getSets(DEBUG_SUBSCRIPTION_CONVERTER).write(0, subscriptions);
    }

    private static WrappedRegistry getDebugSubscriptionRegistry() {
        WrappedRegistry registry = WrappedRegistry.getRegistryByNmsKey(
                "core.registries.Registries",
                "DEBUG_SUBSCRIPTION");
        if (registry == null) {
            throw new IllegalStateException("DebugSubscription registry is not available");
        }
        return registry;
    }
}
