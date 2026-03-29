package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlayerAbilitiesPacket} (Play phase, serverbound).
 *
 * <p>Sent by the client when its flying state changes (e.g. double-jumping in creative mode).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code boolean isFlying} – whether the player is currently flying</li>
 * </ul>
 */
public class WrappedServerboundPlayerAbilitiesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ABILITIES;

    public WrappedServerboundPlayerAbilitiesPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundPlayerAbilitiesPacket(boolean isFlying) {
        this();
        setFlying(isFlying);
    }

    public WrappedServerboundPlayerAbilitiesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isFlying() {
        return handle.getBooleans().read(0);
    }

    public void setFlying(boolean isFlying) {
        handle.getBooleans().write(0, isFlying);
    }
}
