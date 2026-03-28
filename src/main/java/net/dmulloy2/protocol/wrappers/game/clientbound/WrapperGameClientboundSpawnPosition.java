package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.InternalStructure;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetDefaultSpawnPositionPacket} (Play phase, clientbound).
 *
 * <p>Sets the default spawn position and orientation sent to the client. In
 * Minecraft 1.21.4 (26.1) the payload is a {@code RespawnData} record containing
 * a {@code GlobalPos} plus yaw and pitch.
 *
 * <p>Packet structure (inner {@code RespawnData}):
 * <ul>
 *   <li>{@code GlobalPos globalPos} – dimension key + block position</li>
 *   <li>{@code float yaw} – spawn yaw facing angle (degrees)</li>
 *   <li>{@code float pitch} – spawn pitch facing angle (degrees)</li>
 * </ul>
 */
public class WrapperGameClientboundSpawnPosition extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SPAWN_POSITION;

    public WrapperGameClientboundSpawnPosition() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundSpawnPosition(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the {@code RespawnData} as a raw structure for advanced access. */
    public InternalStructure getRespawnData() {
        return handle.getStructures().read(0);
    }

    /** Returns the spawn yaw angle (degrees) from the inner {@code RespawnData}. */
    public float getYaw() {
        return getRespawnData().getFloat().read(0);
    }

    public void setYaw(float yaw) {
        getRespawnData().getFloat().write(0, yaw);
    }

    /** Returns the spawn pitch angle (degrees) from the inner {@code RespawnData}. */
    public float getPitch() {
        return getRespawnData().getFloat().read(1);
    }

    public void setPitch(float pitch) {
        getRespawnData().getFloat().write(1, pitch);
    }
}
