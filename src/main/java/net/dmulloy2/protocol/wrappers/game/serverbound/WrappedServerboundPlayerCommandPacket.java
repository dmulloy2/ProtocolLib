package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ServerboundPlayerCommandPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – entity ID of the player issuing the command</li>
 *   <li>{@code Action action} – the player action (e.g. start/stop sneaking, sprinting)</li>
 *   <li>{@code int data} – auxiliary data (e.g. jump boost level for horse jump)</li>
 * </ul>
 */
public class WrappedServerboundPlayerCommandPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ENTITY_ACTION;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(EnumWrappers.getPlayerActionClass(), EnumWrappers.getEntityActionConverter())
            .withParam(int.class);

    public WrappedServerboundPlayerCommandPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerCommandPacket(int entityId, EnumWrappers.PlayerAction action, int data) {
        this();
        setEntityId(entityId);
        setAction(action);
        setData(data);
    }

    public WrappedServerboundPlayerCommandPacket(Entity entity, EnumWrappers.PlayerAction action, int data) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entity, action, data)));
    }

    public WrappedServerboundPlayerCommandPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the entity ID of the player issuing the command.
     */
    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    /**
     * Sets the entity ID of the player issuing the command.
     */
    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }

    /**
     * Returns the player action (e.g. start/stop sneaking, sprinting).
     */
    public EnumWrappers.PlayerAction getAction() {
        return handle.getEnumModifier(EnumWrappers.PlayerAction.class, EnumWrappers.getPlayerActionClass()).readSafely(0);
    }

    /**
     * Sets the player action (e.g. start/stop sneaking, sprinting).
     */
    public void setAction(EnumWrappers.PlayerAction action) {
        handle.getEnumModifier(EnumWrappers.PlayerAction.class, EnumWrappers.getPlayerActionClass()).writeSafely(0, action);
    }

    /**
     * Returns the auxiliary data (e.g. jump boost level for horse jump).
     */
    public int getData() {
        return handle.getIntegers().readSafely(1);
    }

    /**
     * Sets the auxiliary data (e.g. jump boost level for horse jump).
     */
    public void setData(int data) {
        handle.getIntegers().writeSafely(1, data);
    }
}
