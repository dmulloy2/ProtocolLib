package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLevelParticlesPacket} (game phase, clientbound).
 */
public class WrappedClientboundLevelParticlesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.WORLD_PARTICLES;

    public WrappedClientboundLevelParticlesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLevelParticlesPacket(boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, float xDist, float yDist, float zDist, float maxSpeed, int count, WrappedParticle<?> particle) {
        this();
        setOverrideLimiter(overrideLimiter);
        setAlwaysShow(alwaysShow);
        setX(x);
        setY(y);
        setZ(z);
        setXDist(xDist);
        setYDist(yDist);
        setZDist(zDist);
        setMaxSpeed(maxSpeed);
        setCount(count);
        setParticle(particle);
    }

    public WrappedClientboundLevelParticlesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isOverrideLimiter() {
        return handle.getBooleans().read(0);
    }

    public void setOverrideLimiter(boolean overrideLimiter) {
        handle.getBooleans().write(0, overrideLimiter);
    }

    public boolean isAlwaysShow() {
        return handle.getBooleans().read(1);
    }

    public void setAlwaysShow(boolean alwaysShow) {
        handle.getBooleans().write(1, alwaysShow);
    }

    public double getX() {
        return handle.getDoubles().read(0);
    }

    public void setX(double x) {
        handle.getDoubles().write(0, x);
    }

    public double getY() {
        return handle.getDoubles().read(1);
    }

    public void setY(double y) {
        handle.getDoubles().write(1, y);
    }

    public double getZ() {
        return handle.getDoubles().read(2);
    }

    public void setZ(double z) {
        handle.getDoubles().write(2, z);
    }

    public float getXDist() {
        return handle.getFloat().read(0);
    }

    public void setXDist(float xDist) {
        handle.getFloat().write(0, xDist);
    }

    public float getYDist() {
        return handle.getFloat().read(1);
    }

    public void setYDist(float yDist) {
        handle.getFloat().write(1, yDist);
    }

    public float getZDist() {
        return handle.getFloat().read(2);
    }

    public void setZDist(float zDist) {
        handle.getFloat().write(2, zDist);
    }

    public float getMaxSpeed() {
        return handle.getFloat().read(3);
    }

    public void setMaxSpeed(float maxSpeed) {
        handle.getFloat().write(3, maxSpeed);
    }

    public int getCount() {
        return handle.getIntegers().read(0);
    }

    public void setCount(int count) {
        handle.getIntegers().write(0, count);
    }

    public WrappedParticle<?> getParticle() {
        return handle.getNewParticles().read(0);
    }

    public void setParticle(WrappedParticle<?> particle) {
        handle.getNewParticles().write(0, particle);
    }
}
