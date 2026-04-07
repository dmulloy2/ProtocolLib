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
 * Wrapper for {@code ClientboundSetEntityLinkPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int attachedEntityId} – entity ID of the entity being attached/leashed</li>
 *   <li>{@code int holdingEntityId} – entity ID of the holder (fence post or player); -1 to detach</li>
 * </ul>
 */
public class WrappedClientboundSetEntityLinkPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ATTACH_ENTITY;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance());

    public WrappedClientboundSetEntityLinkPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetEntityLinkPacket(int attachedEntityId, int holdingEntityId) {
        this();
        setAttachedEntityId(attachedEntityId);
        setHoldingEntityId(holdingEntityId);
    }

    /**
     * @param sourceEntity the entity being leashed/attached
     * @param destEntity   the holder entity, or {@code null} to detach (sets holdingEntityId = -1)
     */
    public WrappedClientboundSetEntityLinkPacket(Entity sourceEntity, Entity destEntity) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(sourceEntity, destEntity)));
    }

    public WrappedClientboundSetEntityLinkPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Entity getAttachedEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setAttachedEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }

    public int getAttachedEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setAttachedEntityId(int attachedEntityId) {
        handle.getIntegers().writeSafely(0, attachedEntityId);
    }

    public int getHoldingEntityId() {
        return handle.getIntegers().readSafely(1);
    }

    public void setHoldingEntityId(int holdingEntityId) {
        handle.getIntegers().writeSafely(1, holdingEntityId);
    }
}
