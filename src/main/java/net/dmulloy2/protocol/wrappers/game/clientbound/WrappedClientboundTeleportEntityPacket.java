package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import java.util.Set;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundTeleportEntityPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – entity ID</li>
 *   <li>{@code PositionMoveRotation change} – position, velocity delta, yaw, pitch</li>
 *   <li>{@code Set<Relative> relatives} – axes that are relative to current values</li>
 *   <li>{@code boolean onGround} – whether the entity is on the ground</li>
 * </ul>
 */
public class WrappedClientboundTeleportEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_TELEPORT;

    public WrappedClientboundTeleportEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTeleportEntityPacket(int entityId, WrappedPositionMoveRotation change, Set<EnumWrappers.RelativeArgument> relatives, boolean onGround) {
        this();
        setEntityId(entityId);
        setChange(change);
        setRelatives(relatives);
        setOnGround(onGround);
    }

    public WrappedClientboundTeleportEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // Entity ID
    // -------------------------------------------------------------------------

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    /** Convenience method – resolves the entity in the given world. */
    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    // -------------------------------------------------------------------------
    // Position / movement / rotation (PositionMoveRotation)
    // -------------------------------------------------------------------------

    public WrappedPositionMoveRotation getChange() {
        return handle.getPositionMoveRotations().read(0);
    }

    public void setChange(WrappedPositionMoveRotation change) {
        handle.getPositionMoveRotations().write(0, change);
    }

    // -------------------------------------------------------------------------
    // Relative axes
    // -------------------------------------------------------------------------

    public Set<EnumWrappers.RelativeArgument> getRelatives() {
        return handle.getSets(EnumWrappers.getRelativeArgumentConverter()).read(0);
    }

    public void setRelatives(Set<EnumWrappers.RelativeArgument> relatives) {
        handle.getSets(EnumWrappers.getRelativeArgumentConverter()).write(0, relatives);
    }

    // -------------------------------------------------------------------------
    // On-ground flag
    // -------------------------------------------------------------------------

    public boolean isOnGround() {
        return handle.getBooleans().read(0);
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(0, onGround);
    }
}
