package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Sound;

/**
 * Wrapper for {@code ClientboundSoundEntityPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Sound sound} – sound effect to play</li>
 *   <li>{@code SoundCategory category} – sound category for volume mixing</li>
 *   <li>{@code int entityId} – entity the sound is attached to</li>
 *   <li>{@code float volume} – volume (1.0 = 100%)</li>
 *   <li>{@code float pitch} – pitch (1.0 = normal)</li>
 *   <li>{@code long seed} – random seed for the sound event</li>
 * </ul>
 */
public class WrapperGameClientboundEntitySound extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_SOUND;

    public WrapperGameClientboundEntitySound() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundEntitySound(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Sound getSound() {
        return handle.getSoundEffects().read(0);
    }

    public void setSound(Sound sound) {
        handle.getSoundEffects().write(0, sound);
    }

    public EnumWrappers.SoundCategory getCategory() {
        return handle.getSoundCategories().read(0);
    }

    public void setCategory(EnumWrappers.SoundCategory category) {
        handle.getSoundCategories().write(0, category);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public float getVolume() {
        return handle.getFloat().read(0);
    }

    public void setVolume(float volume) {
        handle.getFloat().write(0, volume);
    }

    public float getPitch() {
        return handle.getFloat().read(1);
    }

    public void setPitch(float pitch) {
        handle.getFloat().write(1, pitch);
    }

    public long getSeed() {
        return handle.getLongs().read(0);
    }

    public void setSeed(long seed) {
        handle.getLongs().write(0, seed);
    }
}
