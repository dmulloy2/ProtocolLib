package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundServerDataPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Component motd} – server MOTD</li>
 *   <li>{@code Optional<byte[]> iconBytes} – optional favicon PNG bytes</li>
 * </ul>
 */
public class WrappedClientboundServerDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SERVER_DATA;

    public WrappedClientboundServerDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundServerDataPacket(WrappedChatComponent motd, Optional<byte[]> iconBytes) {
        this();
        setMotd(motd);
        setIconBytes(iconBytes);
    }

    public WrappedClientboundServerDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getMotd() {
        return handle.getChatComponents().read(0);
    }

    public void setMotd(WrappedChatComponent motd) {
        handle.getChatComponents().write(0, motd);
    }

    public Optional<byte[]> getIconBytes() {
        return handle.getOptionals(Converters.passthrough(byte[].class)).read(0);
    }

    public void setIconBytes(Optional<byte[]> iconBytes) {
        handle.getOptionals(Converters.passthrough(byte[].class)).write(0, iconBytes);
    }
}
