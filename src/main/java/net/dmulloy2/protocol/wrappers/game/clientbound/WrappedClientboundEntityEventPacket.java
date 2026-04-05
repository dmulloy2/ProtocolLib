package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundEntityEventPacket} (Play phase, clientbound).
 *
 * <p>Triggers a status/animation on an entity (e.g. hurt flash, death
 * animation, taming success/failure).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – the affected entity's ID</li>
 *   <li>{@code byte status} – event/status code (see Minecraft protocol wiki)</li>
 * </ul>
 */
public class WrappedClientboundEntityEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_STATUS;

    public WrappedClientboundEntityEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundEntityEventPacket(int entityId, byte status) {
        this();
        setEntityId(entityId);
        setStatus(status);
    }

    public WrappedClientboundEntityEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    public byte getStatus() {
        return handle.getBytes().read(0);
    }

    public void setStatus(byte status) {
        handle.getBytes().write(0, status);
    }
}
