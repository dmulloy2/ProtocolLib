package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

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

    public WrappedServerboundPlayerCommandPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerCommandPacket(int entityId, EnumWrappers.PlayerAction action, int data) {
        this();
        setEntityId(entityId);
        setAction(action);
        setData(data);
    }

    public WrappedServerboundPlayerCommandPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the entity ID of the player issuing the command.
     */
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the entity ID of the player issuing the command.
     */
    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    /**
     * Returns the player action (e.g. start/stop sneaking, sprinting).
     */
    public EnumWrappers.PlayerAction getAction() {
        return handle.getEnumModifier(EnumWrappers.PlayerAction.class, EnumWrappers.getPlayerActionClass()).read(0);
    }

    /**
     * Sets the player action (e.g. start/stop sneaking, sprinting).
     */
    public void setAction(EnumWrappers.PlayerAction action) {
        handle.getEnumModifier(EnumWrappers.PlayerAction.class, EnumWrappers.getPlayerActionClass()).write(0, action);
    }

    /**
     * Returns the auxiliary data (e.g. jump boost level for horse jump).
     */
    public int getData() {
        return handle.getIntegers().read(1);
    }

    /**
     * Sets the auxiliary data (e.g. jump boost level for horse jump).
     */
    public void setData(int data) {
        handle.getIntegers().write(1, data);
    }
}
