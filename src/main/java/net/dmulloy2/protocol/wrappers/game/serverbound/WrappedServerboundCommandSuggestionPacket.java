package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundCommandSuggestionPacket} (game phase, serverbound).
 */
public class WrappedServerboundCommandSuggestionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TAB_COMPLETE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(String.class);

    public WrappedServerboundCommandSuggestionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundCommandSuggestionPacket(int id, String command) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id, command)));
    }

    public WrappedServerboundCommandSuggestionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public String getCommand() {
        return handle.getStrings().read(0);
    }

    public void setCommand(String command) {
        handle.getStrings().write(0, command);
    }
}
