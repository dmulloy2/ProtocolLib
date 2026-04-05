package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundEntityPositionSyncPacket} (game phase, clientbound).
 */
public class WrappedClientboundEntityPositionSyncPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_POSITION_SYNC;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(MinecraftReflection.getMinecraftClass("world.entity.PositionMoveRotation"),
                    WrappedPositionMoveRotation.getConverter())
            .withParam(boolean.class);

    public WrappedClientboundEntityPositionSyncPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundEntityPositionSyncPacket(int id, WrappedPositionMoveRotation values, boolean onGround) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id, values, onGround)));
    }

    public WrappedClientboundEntityPositionSyncPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public WrappedPositionMoveRotation getValues() {
        return handle.getPositionMoveRotations().read(0);
    }

    public void setValues(WrappedPositionMoveRotation values) {
        handle.getPositionMoveRotations().write(0, values);
    }

    public boolean isOnGround() {
        return handle.getBooleans().read(0);
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(0, onGround);
    }
}
