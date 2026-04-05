package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedExplosionParticleInfo;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import com.comphenix.protocol.wrappers.WrappedWeightedList;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ClientboundExplodePacket} (game phase, clientbound).
 * <p>
 * NMS record fields (declaration order):
 * <ol>
 *   <li>{@code Vec3 center}</li>
 *   <li>{@code float radius}</li>
 *   <li>{@code int blockCount}</li>
 *   <li>{@code Optional<Vec3> playerKnockback}</li>
 *   <li>{@code ParticleOptions explosionParticle}</li>
 *   <li>{@code Holder<SoundEvent> explosionSound}</li>
 *   <li>{@code WeightedList<ExplosionParticleInfo> blockParticles}</li>
 * </ol>
 */
public class WrappedClientboundExplodePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.EXPLOSION;

    public WrappedClientboundExplodePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundExplodePacket(Vector center, float radius, int blockCount, Optional<Vector> playerKnockback, WrappedParticle<?> explosionParticle, Sound explosionSound, WrappedWeightedList<WrappedExplosionParticleInfo> blockParticles) {
        this();
        setCenter(center);
        setRadius(radius);
        setBlockCount(blockCount);
        setPlayerKnockback(playerKnockback);
        setExplosionParticle(explosionParticle);
        setExplosionSound(explosionSound);
        setBlockParticles(blockParticles);
    }

    public WrappedClientboundExplodePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // ── center (Vec3) ────────────────────────────────────────────────────

    public Vector getCenter() {
        return handle.getVectors().read(0);
    }

    public void setCenter(Vector center) {
        handle.getVectors().write(0, center);
    }

    // ── radius (float) ──────────────────────────────────────────────────

    public float getRadius() {
        return handle.getFloat().read(0);
    }

    public void setRadius(float radius) {
        handle.getFloat().write(0, radius);
    }

    // ── blockCount (int) ────────────────────────────────────────────────

    public int getBlockCount() {
        return handle.getIntegers().read(0);
    }

    public void setBlockCount(int blockCount) {
        handle.getIntegers().write(0, blockCount);
    }

    // ── playerKnockback (Optional<Vec3>) ────────────────────────────────

    public Optional<Vector> getPlayerKnockback() {
        return handle.getOptionals(BukkitConverters.getVectorConverter()).read(0);
    }

    public void setPlayerKnockback(Optional<Vector> playerKnockback) {
        handle.getOptionals(BukkitConverters.getVectorConverter()).write(0, playerKnockback);
    }

    // ── explosionParticle (ParticleOptions) ─────────────────────────────

    public WrappedParticle<?> getExplosionParticle() {
        return handle.getNewParticles().read(0);
    }

    public void setExplosionParticle(WrappedParticle<?> explosionParticle) {
        handle.getNewParticles().write(0, explosionParticle);
    }

    // ── explosionSound (Holder<SoundEvent>) ─────────────────────────────

    public Sound getExplosionSound() {
        return handle.getSoundEffects().read(0);
    }

    public void setExplosionSound(Sound explosionSound) {
        handle.getSoundEffects().write(0, explosionSound);
    }

    // ── blockParticles (WeightedList<ExplosionParticleInfo>) ────────────

    public WrappedWeightedList<WrappedExplosionParticleInfo> getBlockParticles() {
        return handle.getWeightedLists(WrappedExplosionParticleInfo.getConverter()).read(0);
    }

    public void setBlockParticles(WrappedWeightedList<WrappedExplosionParticleInfo> blockParticles) {
        handle.getWeightedLists(WrappedExplosionParticleInfo.getConverter()).write(0, blockParticles);
    }
}
