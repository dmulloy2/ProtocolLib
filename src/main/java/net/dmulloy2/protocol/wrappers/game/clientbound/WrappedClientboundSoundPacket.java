package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Sound;

/**
 * Wrapper for {@code ClientboundSoundPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Sound sound} – sound effect to play</li>
 *   <li>{@code SoundCategory category} – sound category for volume mixing</li>
 *   <li>{@code int x} – fixed-point X coordinate (actual = x / 8.0)</li>
 *   <li>{@code int y} – fixed-point Y coordinate (actual = y / 8.0)</li>
 *   <li>{@code int z} – fixed-point Z coordinate (actual = z / 8.0)</li>
 *   <li>{@code float volume} – volume (1.0 = 100%)</li>
 *   <li>{@code float pitch} – pitch (1.0 = normal)</li>
 *   <li>{@code long seed} – random seed for the sound event</li>
 * </ul>
 */
public class WrappedClientboundSoundPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.NAMED_SOUND_EFFECT;

    public WrappedClientboundSoundPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundSoundPacket(PacketContainer packet) {
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

    /** @return fixed-point X (actual position = getX() / 8.0) */
    public int getX() {
        return handle.getIntegers().read(0);
    }

    public void setX(int x) {
        handle.getIntegers().write(0, x);
    }

    /** @return actual X position in blocks */
    public double getActualX() {
        return getX() / 8.0;
    }

    /** @return fixed-point Y (actual position = getY() / 8.0) */
    public int getY() {
        return handle.getIntegers().read(1);
    }

    public void setY(int y) {
        handle.getIntegers().write(1, y);
    }

    /** @return actual Y position in blocks */
    public double getActualY() {
        return getY() / 8.0;
    }

    /** @return fixed-point Z (actual position = getZ() / 8.0) */
    public int getZ() {
        return handle.getIntegers().read(2);
    }

    public void setZ(int z) {
        handle.getIntegers().write(2, z);
    }

    /** @return actual Z position in blocks */
    public double getActualZ() {
        return getZ() / 8.0;
    }

    /**
     * Sets the sound position from world-space coordinates.
     * Each coordinate is multiplied by 8 and stored as an integer.
     *
     * @param x world X
     * @param y world Y
     * @param z world Z
     */
    public void setPosition(double x, double y, double z) {
        handle.getIntegers().write(0, (int) (x * 8));
        handle.getIntegers().write(1, (int) (y * 8));
        handle.getIntegers().write(2, (int) (z * 8));
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
