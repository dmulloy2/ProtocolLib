package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTickingStepPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int tickSteps} – number of ticks to step (used in step-tick debug mode)</li>
 * </ul>
 */
public class WrapperGameClientboundTickingStepState extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TICKING_STEP_STATE;

    public WrapperGameClientboundTickingStepState() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundTickingStepState(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTickSteps() {
        return handle.getIntegers().read(0);
    }

    public void setTickSteps(int tickSteps) {
        handle.getIntegers().write(0, tickSteps);
    }
}
