package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

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

    public WrappedClientboundPlayerLookAtPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlayerLookAtPacket(Anchor fromAnchor, Anchor toAnchor, double x, double y, double z, boolean atEntity, int entity) {
        this();
        setFromAnchor(fromAnchor);
        setToAnchor(toAnchor);
        setX(x);
        setY(y);
        setZ(z);
        setAtEntity(atEntity);
        setEntity(entity);
    }

    public WrappedClientboundPlayerLookAtPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // Anchors
    // NMS field layout (non-static, declaration order):
    //   0=x(double), 1=y(double), 2=z(double), 3=toEntity(Entity?), 4=fromAnchor, 5=toAnchor, 6=atEntity(boolean)
    // getEnumModifier(cls, index) uses the *global* field index to look up the NMS enum class,
    // then filters all fields of that type — so index 4 finds both Anchor fields at type-indices 0 and 1.
    // -------------------------------------------------------------------------

    public Anchor getFromAnchor() {
        return handle.getEnumModifier(Anchor.class, 4).read(0);
    }

    public void setFromAnchor(Anchor fromAnchor) {
        handle.getEnumModifier(Anchor.class, 4).write(0, fromAnchor);
    }

    public Anchor getToAnchor() {
        return handle.getEnumModifier(Anchor.class, 4).read(1);
    }

    public void setToAnchor(Anchor toAnchor) {
        handle.getEnumModifier(Anchor.class, 4).write(1, toAnchor);
    }

    // -------------------------------------------------------------------------
    // Target coordinates
    // -------------------------------------------------------------------------

    public double getX() {
        return handle.getDoubles().read(0);
    }

    public void setX(double x) {
        handle.getDoubles().write(0, x);
    }

    public double getY() {
        return handle.getDoubles().read(1);
    }

    public void setY(double y) {
        handle.getDoubles().write(1, y);
    }

    public double getZ() {
        return handle.getDoubles().read(2);
    }

    public void setZ(double z) {
        handle.getDoubles().write(2, z);
    }

    // -------------------------------------------------------------------------
    // Entity target
    // -------------------------------------------------------------------------

    public boolean isAtEntity() {
        return handle.getBooleans().read(0);
    }

    public void setAtEntity(boolean atEntity) {
        handle.getBooleans().write(0, atEntity);
    }

    public int getEntity() {
        return handle.getIntegers().read(0);
    }

    public void setEntity(int entity) {
        handle.getIntegers().write(0, entity);
    }
}
