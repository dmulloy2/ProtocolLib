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
 * Wrapper for {@code ClientboundAnimatePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID performing the animation</li>
 *   <li>{@code int animationId} – animation ID:
 *     <ul>
 *       <li>0 = swing main arm</li>
 *       <li>1 = hurt</li>
 *       <li>2 = wake up</li>
 *       <li>3 = swing off hand</li>
 *       <li>4 = critical hit</li>
 *       <li>5 = magic critical hit</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class WrappedClientboundAnimatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ANIMATION;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(int.class);

    public WrappedClientboundAnimatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundAnimatePacket(int entityId, int animationId) {
        this();
        setEntityId(entityId);
        setAnimationId(animationId);
    }

    public WrappedClientboundAnimatePacket(Entity entity, int animationId) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entity, animationId)));
    }

    public WrappedClientboundAnimatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }

    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    public int getAnimationId() {
        return handle.getIntegers().readSafely(1);
    }

    public void setAnimationId(int animationId) {
        handle.getIntegers().writeSafely(1, animationId);
    }
}
