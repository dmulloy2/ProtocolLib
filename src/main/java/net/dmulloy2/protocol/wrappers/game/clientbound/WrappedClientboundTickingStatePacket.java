package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTickingStatePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code float tickRate} – current tick rate in ticks per second</li>
 *   <li>{@code boolean isFrozen} – whether the server is in frozen-tick mode</li>
 * </ul>
 */
public class WrappedClientboundTickingStatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TICKING_STATE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(float.class)
            .withParam(boolean.class);

    public WrappedClientboundTickingStatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTickingStatePacket(float tickRate, boolean frozen) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(tickRate, frozen)));
    }

    public WrappedClientboundTickingStatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public float getTickRate() {
        return handle.getFloat().read(0);
    }

    public void setTickRate(float tickRate) {
        handle.getFloat().write(0, tickRate);
    }

    public boolean isFrozen() {
        return handle.getBooleans().read(0);
    }

    public void setFrozen(boolean isFrozen) {
        handle.getBooleans().write(0, isFrozen);
    }
}
