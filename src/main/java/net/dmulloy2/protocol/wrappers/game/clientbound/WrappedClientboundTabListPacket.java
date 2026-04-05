package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTabListPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code WrappedChatComponent header} – tab-list header component</li>
 *   <li>{@code WrappedChatComponent footer} – tab-list footer component</li>
 * </ul>
 */
public class WrappedClientboundTabListPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter())
            .withParam(MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());

    public WrappedClientboundTabListPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTabListPacket(WrappedChatComponent header, WrappedChatComponent footer) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(header, footer)));
    }

    public WrappedClientboundTabListPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getHeader() {
        return handle.getChatComponents().read(0);
    }

    public void setHeader(WrappedChatComponent header) {
        handle.getChatComponents().write(0, header);
    }

    public WrappedChatComponent getFooter() {
        return handle.getChatComponents().read(1);
    }

    public void setFooter(WrappedChatComponent footer) {
        handle.getChatComponents().write(1, footer);
    }
}
