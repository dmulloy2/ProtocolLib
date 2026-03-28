package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.potion.PotionEffectType;

/**
 * Wrapper for {@code ClientboundUpdateMobEffectPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID receiving the effect</li>
 *   <li>{@code PotionEffectType effectType} – the effect type</li>
 *   <li>{@code int amplifier} – effect amplifier (0 = level I)</li>
 *   <li>{@code int duration} – duration in ticks; -1 for infinite</li>
 *   <li>{@code byte flags} – bitmask of effect flags</li>
 * </ul>
 */
public class WrapperGameClientboundEntityEffect extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_EFFECT;

    /** The effect plays without ambience particles. */
    public static final byte FLAG_IS_AMBIENT    = 0x01;
    /** Show particles for the effect. */
    public static final byte FLAG_SHOW_PARTICLES = 0x02;
    /** Show the effect icon in the HUD. */
    public static final byte FLAG_SHOW_ICON      = 0x04;

    public WrapperGameClientboundEntityEffect() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundEntityEffect(PacketContainer packet) {
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

    public int getAmplifier() {
        return handle.getIntegers().read(1);
    }

    public void setAmplifier(int amplifier) {
        handle.getIntegers().write(1, amplifier);
    }

    public int getDuration() {
        return handle.getIntegers().read(2);
    }

    public void setDuration(int duration) {
        handle.getIntegers().write(2, duration);
    }

    public byte getFlags() {
        return handle.getBytes().read(0);
    }

    public void setFlags(byte flags) {
        handle.getBytes().write(0, flags);
    }
}
