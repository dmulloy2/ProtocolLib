package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLevelParticlesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLevelParticlesPacket w = new WrappedClientboundLevelParticlesPacket(true, false, -2.5, 3.14, 100.0, -3.0f, 0.75f, 0.5f, -3.0f, 3, null);

        assertEquals(PacketType.Play.Server.WORLD_PARTICLES, w.getHandle().getType());

        assertTrue(w.isOverrideLimiter());
        assertFalse(w.isAlwaysShow());
        assertEquals(-2.5, w.getX(), 1e-9);
        assertEquals(3.14, w.getY(), 1e-9);
        assertEquals(100.0, w.getZ(), 1e-9);
        assertEquals(-3.0f, w.getXDist(), 1e-4f);
        assertEquals(0.75f, w.getYDist(), 1e-4f);
        assertEquals(0.5f, w.getZDist(), 1e-4f);
        assertEquals(-3.0f, w.getMaxSpeed(), 1e-4f);
        assertEquals(3, w.getCount());
        assertEquals(null, w.getParticle());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLevelParticlesPacket w = new WrappedClientboundLevelParticlesPacket();

        assertEquals(PacketType.Play.Server.WORLD_PARTICLES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundLevelParticlesPacket source = new WrappedClientboundLevelParticlesPacket(true, false, -2.5, 3.14, 100.0, -3.0f, 0.75f, 0.5f, -3.0f, 3, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLevelParticlesPacket wrapper = new WrappedClientboundLevelParticlesPacket(container);

        assertTrue(wrapper.isOverrideLimiter());
        assertFalse(wrapper.isAlwaysShow());
        assertEquals(-2.5, wrapper.getX(), 1e-9);
        assertEquals(3.14, wrapper.getY(), 1e-9);
        assertEquals(100.0, wrapper.getZ(), 1e-9);
        assertEquals(-3.0f, wrapper.getXDist(), 1e-4f);
        assertEquals(0.75f, wrapper.getYDist(), 1e-4f);
        assertEquals(0.5f, wrapper.getZDist(), 1e-4f);
        assertEquals(-3.0f, wrapper.getMaxSpeed(), 1e-4f);
        assertEquals(3, wrapper.getCount());
        assertEquals(null, wrapper.getParticle());

        wrapper.setOverrideLimiter(false);
        wrapper.setAlwaysShow(true);
        wrapper.setX(0.0);
        wrapper.setY(100.0);
        wrapper.setZ(2.71);
        wrapper.setXDist(1.0f);
        wrapper.setYDist(1.0f);
        wrapper.setZDist(10.5f);
        wrapper.setMaxSpeed(0.25f);
        wrapper.setCount(-5);
        wrapper.setParticle(null);

        assertFalse(wrapper.isOverrideLimiter());
        assertTrue(wrapper.isAlwaysShow());
        assertEquals(0.0, wrapper.getX(), 1e-9);
        assertEquals(100.0, wrapper.getY(), 1e-9);
        assertEquals(2.71, wrapper.getZ(), 1e-9);
        assertEquals(1.0f, wrapper.getXDist(), 1e-4f);
        assertEquals(1.0f, wrapper.getYDist(), 1e-4f);
        assertEquals(10.5f, wrapper.getZDist(), 1e-4f);
        assertEquals(0.25f, wrapper.getMaxSpeed(), 1e-4f);
        assertEquals(-5, wrapper.getCount());
        assertEquals(null, wrapper.getParticle());

        assertFalse(source.isOverrideLimiter());
        assertTrue(source.isAlwaysShow());
        assertEquals(0.0, source.getX(), 1e-9);
        assertEquals(100.0, source.getY(), 1e-9);
        assertEquals(2.71, source.getZ(), 1e-9);
        assertEquals(1.0f, source.getXDist(), 1e-4f);
        assertEquals(1.0f, source.getYDist(), 1e-4f);
        assertEquals(10.5f, source.getZDist(), 1e-4f);
        assertEquals(0.25f, source.getMaxSpeed(), 1e-4f);
        assertEquals(-5, source.getCount());
        assertEquals(null, source.getParticle());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLevelParticlesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
