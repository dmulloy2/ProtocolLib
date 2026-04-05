package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChatCommandPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String command} – the command string (without leading slash)</li>
 * </ul>
 */
public class WrappedServerboundChatCommandPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT_COMMAND;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class);

    public WrappedServerboundChatCommandPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChatCommandPacket(String command) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(command)));
    }

    public WrappedServerboundChatCommandPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the command string (without leading slash).
     */
    public String getCommand() {
        return handle.getStrings().read(0);
    }

    /**
     * Sets the command string (without leading slash).
     */
    public void setCommand(String command) {
        handle.getStrings().write(0, command);
    }
}
