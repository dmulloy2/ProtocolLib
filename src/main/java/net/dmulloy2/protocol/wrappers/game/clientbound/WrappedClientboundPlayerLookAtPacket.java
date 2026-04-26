package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundPlayerLookAtPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code EntityAnchorArgument.Anchor fromAnchor} – which part of the player to look from</li>
 *   <li>{@code double x, y, z} – target coordinates to look at</li>
 *   <li>{@code boolean atEntity} – whether the look-at target is an entity</li>
 *   <li>{@code int entity} – entity ID to look at (only relevant if atEntity is true)</li>
 *   <li>{@code EntityAnchorArgument.Anchor toAnchor} – which part of the entity to look at</li>
 * </ul>
 */
public class WrappedClientboundPlayerLookAtPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.LOOK_AT;

    /**
     * Which part of the entity to anchor the look-at direction to.
     * Matches {@code EntityAnchorArgument.Anchor}.
     */
    public enum Anchor {
        FEET, EYES
    }

    private static final Class<?> NMS_ANCHOR_CLASS =
            MinecraftReflection.getMinecraftClass("commands.arguments.EntityAnchorArgument$Anchor");
    private static final EquivalentConverter<Anchor> ANCHOR_CONVERTER =
            new EnumWrappers.EnumConverter<>(NMS_ANCHOR_CLASS, Anchor.class);

    /** Position-target constructor: looks at a fixed world coordinate. */
    private static final EquivalentConstructor POSITION_CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(NMS_ANCHOR_CLASS, ANCHOR_CONVERTER)
            .withParam(double.class)
            .withParam(double.class)
            .withParam(double.class);

    /** Entity-target constructor: looks at a specific anchor point on an entity. */
    private static final EquivalentConstructor ENTITY_CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(NMS_ANCHOR_CLASS, ANCHOR_CONVERTER)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(NMS_ANCHOR_CLASS, ANCHOR_CONVERTER);

    public WrappedClientboundPlayerLookAtPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    /**
     * Constructs a packet that makes the player look at a fixed world coordinate.
     * Mirrors the NMS {@code ClientboundPlayerLookAtPacket(Anchor, double, double, double)} constructor:
     * {@code atEntity} is set to {@code false}, {@code entity} to {@code 0}, and {@code toAnchor} to {@code null}.
     */
    public WrappedClientboundPlayerLookAtPacket(Anchor fromAnchor, double x, double y, double z) {
        this(new PacketContainer(TYPE, POSITION_CONSTRUCTOR.create(fromAnchor, x, y, z)));
    }

    /**
     * Constructs a packet that makes the player look at a specific anchor point on an entity.
     * Mirrors the NMS {@code ClientboundPlayerLookAtPacket(Anchor, Entity, Anchor)} constructor,
     * which also computes x/y/z from the entity's current position.
     */
    public WrappedClientboundPlayerLookAtPacket(Anchor fromAnchor, Entity entity, Anchor toAnchor) {
        this(new PacketContainer(TYPE, ENTITY_CONSTRUCTOR.create(fromAnchor, entity, toAnchor)));
    }

    public WrappedClientboundPlayerLookAtPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // Anchors
    // NMS field layout (non-static, declaration order):
    //   0=x(double), 1=y(double), 2=z(double), 3=entity(int), 4=fromAnchor, 5=toAnchor, 6=atEntity(boolean)
    // getEnumModifier(cls, index) uses the *global* field index to look up the NMS enum class,
    // then filters all fields of that type — so index 4 finds both Anchor fields at type-indices 0 and 1.
    // -------------------------------------------------------------------------

    public Anchor getFromAnchor() {
        return handle.getEnumModifier(Anchor.class, 4).readSafely(0);
    }

    public void setFromAnchor(Anchor fromAnchor) {
        handle.getEnumModifier(Anchor.class, 4).writeSafely(0, fromAnchor);
    }

    public Anchor getToAnchor() {
        return handle.getEnumModifier(Anchor.class, 4).readSafely(1);
    }

    public void setToAnchor(Anchor toAnchor) {
        handle.getEnumModifier(Anchor.class, 4).writeSafely(1, toAnchor);
    }

    // -------------------------------------------------------------------------
    // Target coordinates
    // -------------------------------------------------------------------------

    public double getX() {
        return handle.getDoubles().readSafely(0);
    }

    public void setX(double x) {
        handle.getDoubles().writeSafely(0, x);
    }

    public double getY() {
        return handle.getDoubles().readSafely(1);
    }

    public void setY(double y) {
        handle.getDoubles().writeSafely(1, y);
    }

    public double getZ() {
        return handle.getDoubles().readSafely(2);
    }

    public void setZ(double z) {
        handle.getDoubles().writeSafely(2, z);
    }

    // -------------------------------------------------------------------------
    // Entity target
    // -------------------------------------------------------------------------

    public boolean isAtEntity() {
        return handle.getBooleans().readSafely(0);
    }

    public void setAtEntity(boolean atEntity) {
        handle.getBooleans().writeSafely(0, atEntity);
    }

    /** Returns the raw entity ID of the look-at target. */
    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    /** Returns the look-at target entity resolved against the given world. */
    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }
}
