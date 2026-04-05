package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCommandSuggestionsPacket} (game phase, clientbound).
 */
public class WrappedClientboundCommandSuggestionsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TAB_COMPLETE;

    public WrappedClientboundCommandSuggestionsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCommandSuggestionsPacket(int id, int start, int length) {
        this();
        setId(id);
        setStart(start);
        setLength(length);
    }

    public WrappedClientboundCommandSuggestionsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public int getStart() {
        return handle.getIntegers().read(1);
    }

    public void setStart(int start) {
        handle.getIntegers().write(1, start);
    }

    public int getLength() {
        return handle.getIntegers().read(2);
    }

    public void setLength(int length) {
        handle.getIntegers().write(2, length);
    }
}
