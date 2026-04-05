package com.comphenix.protocol.wrappers;

import java.util.Objects;

import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Wraps the NMS {@code ExplosionParticleInfo} record, which pairs a
 * {@link WrappedParticle} with scaling and speed float values.
 *
 * <p>NMS record: {@code ExplosionParticleInfo(ParticleOptions particle, float scaling, float speed)}
 */
public class WrappedExplosionParticleInfo {

    private static Class<?> NMS_CLASS;
    private static ConstructorAccessor NMS_CTOR;

    private static synchronized void ensureReflection() {
        if (NMS_CLASS != null) {
            return;
        }
        NMS_CLASS = MinecraftReflection.getMinecraftClass("core.particles.ExplosionParticleInfo");
        NMS_CTOR = Accessors.getConstructorAccessor(
                NMS_CLASS,
                MinecraftReflection.getParticleParam(),
                float.class,
                float.class);
    }

    /**
     * Returns the NMS {@code ExplosionParticleInfo} class.
     */
    public static Class<?> getNmsClass() {
        ensureReflection();
        return NMS_CLASS;
    }

    // ── Instance data ────────────────────────────────────────────────────

    private WrappedParticle<?> particle;
    private float scaling;
    private float speed;

    public WrappedExplosionParticleInfo() {
        this.scaling = 1.0f;
        this.speed = 1.0f;
    }

    public WrappedExplosionParticleInfo(WrappedParticle<?> particle, float scaling, float speed) {
        this.particle = particle;
        this.scaling = scaling;
        this.speed = speed;
    }

    public WrappedParticle<?> getParticle() {
        return particle;
    }

    public void setParticle(WrappedParticle<?> particle) {
        this.particle = particle;
    }

    public float getScaling() {
        return scaling;
    }

    public void setScaling(float scaling) {
        this.scaling = scaling;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // ── Converter ────────────────────────────────────────────────────────

    /**
     * Reads an NMS {@code ExplosionParticleInfo} into this wrapper via
     * {@link InternalStructure} typed getters.
     */
    public static WrappedExplosionParticleInfo fromHandle(InternalStructure handle) {
        WrappedParticle<?> particle = handle.getNewParticles().read(0);
        float scaling = handle.getFloat().read(0);
        float speed = handle.getFloat().read(1);
        return new WrappedExplosionParticleInfo(particle, scaling, speed);
    }

    /**
     * Returns an {@link EquivalentConverter} between {@code WrappedExplosionParticleInfo}
     * and the NMS {@code ExplosionParticleInfo} record.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EquivalentConverter<WrappedExplosionParticleInfo> getConverter() {
        return new EquivalentConverter<>() {

            @Override
            public WrappedExplosionParticleInfo getSpecific(Object generic) {
                InternalStructure structure = InternalStructure.getConverter().getSpecific(generic);
                return fromHandle(structure);
            }

            @Override
            public Object getGeneric(WrappedExplosionParticleInfo specific) {
                ensureReflection();
                EquivalentConverter particleConverter = BukkitConverters.getParticleConverter();
                Object nmsParticle = particleConverter.getGeneric(specific.particle);
                return NMS_CTOR.invoke(nmsParticle, specific.scaling, specific.speed);
            }

            @Override
            public Class<WrappedExplosionParticleInfo> getSpecificType() {
                return WrappedExplosionParticleInfo.class;
            }
        };
    }

    // ── Object methods ───────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WrappedExplosionParticleInfo that)) return false;
        return Float.compare(scaling, that.scaling) == 0
                && Float.compare(speed, that.speed) == 0
                && Objects.equals(particle, that.particle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(particle, scaling, speed);
    }

    @Override
    public String toString() {
        return "WrappedExplosionParticleInfo{particle=" + particle
                + ", scaling=" + scaling + ", speed=" + speed + "}";
    }
}

