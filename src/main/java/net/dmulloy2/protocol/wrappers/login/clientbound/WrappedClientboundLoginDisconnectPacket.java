package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLoginDisconnectPacket} (login phase, clientbound).
 */
public class WrappedClientboundLoginDisconnectPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Server.DISCONNECT;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());

    public WrappedClientboundLoginDisconnectPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLoginDisconnectPacket(WrappedChatComponent reason) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(reason)));
    }

    public WrappedClientboundLoginDisconnectPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getReason() {
        return handle.getChatComponents().read(0);
    }

    public void setReason(WrappedChatComponent reason) {
        handle.getChatComponents().write(0, reason);
    }
}
