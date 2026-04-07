package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundSetPassengersPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int vehicleId} – entity ID of the vehicle</li>
 *   <li>{@code int[] passengerIds} – array of entity IDs of all current passengers</li>
 * </ul>
 */
public class WrappedClientboundSetPassengersPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MOUNT;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance());

    public WrappedClientboundSetPassengersPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetPassengersPacket(int vehicleId, int[] passengerIds) {
        this();
        setVehicleId(vehicleId);
        setPassengerIds(passengerIds);
    }

    /**
     * Constructs the packet from a live {@link Entity}.
     * The passenger list is derived from {@code vehicle.getPassengers()} at construction time.
     */
    public WrappedClientboundSetPassengersPacket(Entity vehicle) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(vehicle)));
    }

    public WrappedClientboundSetPassengersPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Entity getVehicle(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setVehicle(Entity vehicle) {
        handle.getEntityModifier(vehicle.getWorld()).writeSafely(0, vehicle);
    }

    public int getVehicleId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setVehicleId(int vehicleId) {
        handle.getIntegers().writeSafely(0, vehicleId);
    }

    public int[] getPassengerIds() {
        return handle.getIntegerArrays().readSafely(0);
    }

    public void setPassengerIds(int[] passengerIds) {
        handle.getIntegerArrays().writeSafely(0, passengerIds);
    }
}
