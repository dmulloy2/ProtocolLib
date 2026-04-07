package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
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

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(byte.class);

    public WrappedClientboundEntityEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundEntityEventPacket(int entityId, byte status) {
        this();
        setEntityId(entityId);
        setStatus(status);
    }

    public WrappedClientboundEntityEventPacket(Entity entity, byte status) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entity, status)));
    }

    public WrappedClientboundEntityEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }

    public byte getStatus() {
        return handle.getBytes().readSafely(0);
    }

    public void setStatus(byte status) {
        handle.getBytes().writeSafely(0, status);
    }
}
