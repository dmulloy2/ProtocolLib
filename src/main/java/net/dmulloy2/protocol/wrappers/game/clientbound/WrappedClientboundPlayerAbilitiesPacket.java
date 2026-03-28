package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerAbilitiesPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code boolean invulnerable} – whether the player is invulnerable</li>
 *   <li>{@code boolean flying} – whether the player is currently flying</li>
 *   <li>{@code boolean canFly} – whether the player is allowed to fly</li>
 *   <li>{@code boolean creativeMode} – whether the player is in creative mode</li>
 *   <li>{@code float flySpeed} – flying speed multiplier</li>
 *   <li>{@code float walkSpeed} – walking speed multiplier</li>
 * </ul>
 */
public class WrappedClientboundPlayerAbilitiesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ABILITIES;

    public WrappedClientboundPlayerAbilitiesPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundPlayerAbilitiesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isInvulnerable() {
        return handle.getBooleans().read(0);
    }

    public void setInvulnerable(boolean invulnerable) {
        handle.getBooleans().write(0, invulnerable);
    }

    public boolean isFlying() {
        return handle.getBooleans().read(1);
    }

    public void setFlying(boolean flying) {
        handle.getBooleans().write(1, flying);
    }

    public boolean isCanFly() {
        return handle.getBooleans().read(2);
    }

    public void setCanFly(boolean canFly) {
        handle.getBooleans().write(2, canFly);
    }

    public boolean isCreativeMode() {
        return handle.getBooleans().read(3);
    }

    public void setCreativeMode(boolean creativeMode) {
        handle.getBooleans().write(3, creativeMode);
    }

    public float getFlySpeed() {
        return handle.getFloat().read(0);
    }

    public void setFlySpeed(float flySpeed) {
        handle.getFloat().write(0, flySpeed);
    }

    public float getWalkSpeed() {
        return handle.getFloat().read(1);
    }

    public void setWalkSpeed(float walkSpeed) {
        handle.getFloat().write(1, walkSpeed);
    }
}
