package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDisconnectPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundDisconnectPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.DISCONNECT;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());

    public WrappedClientboundDisconnectPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDisconnectPacket(WrappedChatComponent reason) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(reason)));
    }

    public WrappedClientboundDisconnectPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getReason() {
        return handle.getChatComponents().read(0);
    }

    public void setReason(WrappedChatComponent reason) {
        handle.getChatComponents().write(0, reason);
    }
}
