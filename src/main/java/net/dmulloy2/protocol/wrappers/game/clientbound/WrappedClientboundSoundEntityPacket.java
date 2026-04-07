package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;

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
public class WrappedClientboundSoundEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_SOUND;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getHolderClass(), Converters.holder(BukkitConverters.getSoundConverter(),
                    WrappedRegistry.getRegistry(MinecraftReflection.getSoundEffectClass())))
            .withParam(EnumWrappers.getSoundCategoryClass(), EnumWrappers.getSoundCategoryConverter())
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(float.class)
            .withParam(float.class)
            .withParam(long.class);

    public WrappedClientboundSoundEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    /** Setter-based constructor using an entity ID (useful for testing). */
    public WrappedClientboundSoundEntityPacket(Sound sound, EnumWrappers.SoundCategory category, int entityId, float volume, float pitch, long seed) {
        this();
        setSound(sound);
        setCategory(category);
        setEntityId(entityId);
        setVolume(volume);
        setPitch(pitch);
        setSeed(seed);
    }

    /** EC constructor using a live {@link Entity} (mirrors the NMS constructor). */
    public WrappedClientboundSoundEntityPacket(Sound sound, EnumWrappers.SoundCategory category, Entity entity, float volume, float pitch, long seed) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(sound, category, entity, volume, pitch, seed)));
    }

    public WrappedClientboundSoundEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Sound getSound() {
        return handle.getSoundEffects().readSafely(0);
    }

    public void setSound(Sound sound) {
        handle.getSoundEffects().writeSafely(0, sound);
    }

    public EnumWrappers.SoundCategory getCategory() {
        return handle.getSoundCategories().readSafely(0);
    }

    public void setCategory(EnumWrappers.SoundCategory category) {
        handle.getSoundCategories().writeSafely(0, category);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }

    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    public float getVolume() {
        return handle.getFloat().readSafely(0);
    }

    public void setVolume(float volume) {
        handle.getFloat().writeSafely(0, volume);
    }

    public float getPitch() {
        return handle.getFloat().readSafely(1);
    }

    public void setPitch(float pitch) {
        handle.getFloat().writeSafely(1, pitch);
    }

    public long getSeed() {
        return handle.getLongs().readSafely(0);
    }

    public void setSeed(long seed) {
        handle.getLongs().writeSafely(0, seed);
    }
}
