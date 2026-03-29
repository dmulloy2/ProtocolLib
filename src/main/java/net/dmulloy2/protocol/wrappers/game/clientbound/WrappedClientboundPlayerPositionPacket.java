package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Set;

/**
 * Wrapper for {@code ClientboundPlayerPositionPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – teleport confirmation ID</li>
 *   <li>{@code PositionMoveRotation change} – new position, delta, yaw and pitch</li>
 *   <li>{@code Set<Relative> relatives} – which components are relative rather than absolute</li>
 * </ul>
 */
public class WrappedClientboundPlayerPositionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.POSITION;

    public WrappedClientboundPlayerPositionPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundPlayerPositionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public WrappedPositionMoveRotation getChange() {
        return handle.getPositionMoveRotations().read(0);
    }

    public void setChange(WrappedPositionMoveRotation change) {
        handle.getPositionMoveRotations().write(0, change);
    }

    public Set<EnumWrappers.RelativeArgument> getRelatives() {
        return handle.getSets(EnumWrappers.getRelativeArgumentConverter()).read(0);
    }

    public void setRelatives(Set<EnumWrappers.RelativeArgument> relatives) {
        handle.getSets(EnumWrappers.getRelativeArgumentConverter()).write(0, relatives);
    }
}
