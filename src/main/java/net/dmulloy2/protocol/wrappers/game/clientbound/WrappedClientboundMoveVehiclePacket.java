package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ClientboundMoveVehiclePacket} (game phase, clientbound).
 */
public class WrappedClientboundMoveVehiclePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.VEHICLE_MOVE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter())
            .withParam(float.class)
            .withParam(float.class);

    public WrappedClientboundMoveVehiclePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundMoveVehiclePacket(Vector position, float yRot, float xRot) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(position, yRot, xRot)));
    }

    public WrappedClientboundMoveVehiclePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Vector getPosition() {
        return handle.getVectors().read(0);
    }

    public void setPosition(Vector position) {
        handle.getVectors().write(0, position);
    }

    public float getYRot() {
        return handle.getFloat().read(0);
    }

    public void setYRot(float yRot) {
        handle.getFloat().write(0, yRot);
    }

    public float getXRot() {
        return handle.getFloat().read(1);
    }

    public void setXRot(float xRot) {
        handle.getFloat().write(1, xRot);
    }
}
