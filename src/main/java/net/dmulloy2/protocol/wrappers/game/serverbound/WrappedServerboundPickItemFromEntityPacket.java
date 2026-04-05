package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPickItemFromEntityPacket} (game phase, serverbound).
 */
public class WrappedServerboundPickItemFromEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.PICK_ITEM;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(boolean.class);

    public WrappedServerboundPickItemFromEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPickItemFromEntityPacket(int id, boolean includeData) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id, includeData)));
    }

    public WrappedServerboundPickItemFromEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public boolean isIncludeData() {
        return handle.getBooleans().read(0);
    }

    public void setIncludeData(boolean includeData) {
        handle.getBooleans().write(0, includeData);
    }
}
