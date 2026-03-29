package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
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

    public WrappedServerboundAcceptTeleportationPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundAcceptTeleportationPacket(int id) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { int.class }).createPacket(id));
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
