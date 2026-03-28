package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.potion.PotionEffectType;

/**
 * Wrapper for {@code ClientboundRemoveMobEffectPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID from which the effect is removed</li>
 *   <li>{@code PotionEffectType effectType} – the effect type being removed</li>
 * </ul>
 */
public class WrappedClientboundRemoveMobEffectPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.REMOVE_ENTITY_EFFECT;

    public WrappedClientboundRemoveMobEffectPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundRemoveMobEffectPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public PotionEffectType getEffectType() {
        return handle.getEffectTypes().read(0);
    }

    public void setEffectType(PotionEffectType effectType) {
        handle.getEffectTypes().write(0, effectType);
    }
}
