package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetPassengersPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int vehicleId} – entity ID of the vehicle</li>
 *   <li>{@code int[] passengerIds} – array of entity IDs of all current passengers</li>
 * </ul>
 */
public class WrapperGameClientboundMount extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MOUNT;

    public WrapperGameClientboundMount() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundMount(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getVehicleId() {
        return handle.getIntegers().read(0);
    }

    public void setVehicleId(int vehicleId) {
        handle.getIntegers().write(0, vehicleId);
    }

    public int[] getPassengerIds() {
        return handle.getIntegerArrays().read(0);
    }

    public void setPassengerIds(int[] passengerIds) {
        handle.getIntegerArrays().write(0, passengerIds);
    }
}
