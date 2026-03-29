package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundClientCommandPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code ClientCommand$Action action} – the client command action (e.g. perform respawn)</li>
 * </ul>
 */
public class WrappedServerboundClientCommandPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CLIENT_COMMAND;

    public WrappedServerboundClientCommandPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundClientCommandPacket(EnumWrappers.ClientCommand action) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { EnumWrappers.getClientCommandClass() }).createPacket(EnumWrappers.getClientCommandConverter().getGeneric(action)));
    }

    public WrappedServerboundClientCommandPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the client command action.
     */
    public EnumWrappers.ClientCommand getAction() {
        return handle.getEnumModifier(EnumWrappers.ClientCommand.class, EnumWrappers.getClientCommandClass()).read(0);
    }

    /**
     * Sets the client command action.
     */
    public void setAction(EnumWrappers.ClientCommand action) {
        handle.getEnumModifier(EnumWrappers.ClientCommand.class, EnumWrappers.getClientCommandClass()).write(0, action);
    }
}
