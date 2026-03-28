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
public class WrapperGameClientboundUpdateTime extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UPDATE_TIME;

    public WrapperGameClientboundUpdateTime() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundUpdateTime(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getWorldAge() {
        return handle.getLongs().read(0);
    }

    public void setWorldAge(long worldAge) {
        handle.getLongs().write(0, worldAge);
    }
}
