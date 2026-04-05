package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetSimulationDistancePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int simulationDistance} – entity simulation distance radius in chunks</li>
 * </ul>
 */
public class WrappedClientboundSetSimulationDistancePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedClientboundSetSimulationDistancePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetSimulationDistancePacket(int simulationDistance) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(simulationDistance)));
    }

    public WrappedClientboundSetSimulationDistancePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getSimulationDistance() {
        return handle.getIntegers().read(0);
    }

    public void setSimulationDistance(int simulationDistance) {
        handle.getIntegers().write(0, simulationDistance);
    }
}
