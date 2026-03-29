package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCooldownPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code MinecraftKey item} – namespaced item key the cooldown applies to</li>
 *   <li>{@code int ticks} – cooldown duration in ticks; 0 to clear the cooldown</li>
 * </ul>
 */
public class WrappedClientboundCooldownPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_COOLDOWN;

    public WrappedClientboundCooldownPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundCooldownPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getItem() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setItem(MinecraftKey item) {
        handle.getMinecraftKeys().write(0, item);
    }

    public int getTicks() {
        return handle.getIntegers().read(0);
    }

    public void setTicks(int ticks) {
        handle.getIntegers().write(0, ticks);
    }
}
