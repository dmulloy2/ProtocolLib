package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedMinecartStep;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.List;

/**
 * Wrapper for {@code ClientboundMoveMinecartPacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code int entityId} – the entity ID of the minecart</li>
 *   <li>{@code List<MinecartStep> lerpSteps} – interpolation steps (opaque NMS type, no ProtocolLib accessor)</li>
 * </ul>
 */
public class WrappedClientboundMoveMinecartPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MOVE_MINECART;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(List.class, BukkitConverters.getListConverter(WrappedMinecartStep.CONVERTER));

    public WrappedClientboundMoveMinecartPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundMoveMinecartPacket(int entityId, List<WrappedMinecartStep> lerpSteps) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entityId, lerpSteps)));
    }

    public WrappedClientboundMoveMinecartPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public List<WrappedMinecartStep> getLerpSteps() {
        return handle.getLists(WrappedMinecartStep.CONVERTER).read(0);
    }

    public void setLerpSteps(List<WrappedMinecartStep> lerpSteps) {
        handle.getLists(WrappedMinecartStep.CONVERTER).write(0, lerpSteps);
    }
}
