package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundAcceptTeleportationPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – teleport confirmation ID</li>
 * </ul>
 */
public class WrappedServerboundAcceptTeleportationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TELEPORT_ACCEPT;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedServerboundAcceptTeleportationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundAcceptTeleportationPacket(int id) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id)));
    }

    public WrappedServerboundAcceptTeleportationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the teleport confirmation ID.
     */
    public int getId() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the teleport confirmation ID.
     */
    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }
}
