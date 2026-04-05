package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetHealthPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code float health} – current health (0 = dead, max depends on attributes)</li>
 *   <li>{@code int food} – food level (0–20)</li>
 *   <li>{@code float saturation} – food saturation (0.0–5.0)</li>
 * </ul>
 */
public class WrappedClientboundSetHealthPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UPDATE_HEALTH;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(float.class)
            .withParam(int.class)
            .withParam(float.class);

    public WrappedClientboundSetHealthPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetHealthPacket(float health, int food, float saturation) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(health, food, saturation)));
    }

    public WrappedClientboundSetHealthPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // Health
    // -------------------------------------------------------------------------

    public float getHealth() {
        return handle.getFloat().read(0);
    }

    public void setHealth(float health) {
        handle.getFloat().write(0, health);
    }

    // -------------------------------------------------------------------------
    // Food
    // -------------------------------------------------------------------------

    public int getFood() {
        return handle.getIntegers().read(0);
    }

    public void setFood(int food) {
        handle.getIntegers().write(0, food);
    }

    // -------------------------------------------------------------------------
    // Saturation
    // -------------------------------------------------------------------------

    public float getSaturation() {
        return handle.getFloat().read(1);
    }

    public void setSaturation(float saturation) {
        handle.getFloat().write(1, saturation);
    }
}
