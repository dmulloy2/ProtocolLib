package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetTimePacket} (Play phase, clientbound).
 *
 * <p>In Minecraft 1.21.4 (26.1) the packet carries {@code gameTime} (total world
 * age in ticks) and a {@code clockUpdates} map of per-clock states. Only
 * {@code gameTime} is exposed as a simple long; the clock-update map requires
 * custom handling via {@code getHandle()} if needed.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code long gameTime} – total elapsed in-game ticks since world creation</li>
 * </ul>
 */
public class WrappedClientboundSetTimePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UPDATE_TIME;

    public WrappedClientboundSetTimePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetTimePacket(long worldAge) {
        this();
        setWorldAge(worldAge);
    }

    public WrappedClientboundSetTimePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getWorldAge() {
        return handle.getLongs().read(0);
    }

    public void setWorldAge(long worldAge) {
        handle.getLongs().write(0, worldAge);
    }

    // TODO: missing field 'clockUpdates' (NMS type: Map<Holder<WorldClock>, ClockNetworkState>)
    //   Each entry maps a registry-backed WorldClock to a ClockNetworkState (gametime/daytime/tickRate).
    //   No ProtocolLib getMaps() accessor covers Holder<WorldClock> keys.
    //   Use handle.getModifier().read(1) for the raw Map, or add a dedicated converter.
}
