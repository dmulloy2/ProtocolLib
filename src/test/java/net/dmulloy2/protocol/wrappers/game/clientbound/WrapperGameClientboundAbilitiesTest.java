package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundAbilitiesTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundAbilities w = new WrapperGameClientboundAbilities();
        w.setInvulnerable(true);
        w.setFlying(false);
        w.setCanFly(true);
        w.setCreativeMode(true);
        w.setFlySpeed(0.1f);
        w.setWalkSpeed(0.2f);
        assertTrue(w.isInvulnerable());
        assertFalse(w.isFlying());
        assertTrue(w.isCanFly());
        assertTrue(w.isCreativeMode());
        assertEquals(0.1f, w.getFlySpeed(), 1e-6f);
        assertEquals(0.2f, w.getWalkSpeed(), 1e-6f);
        assertEquals(PacketType.Play.Server.ABILITIES, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ABILITIES);
        raw.getModifier().writeDefaults();
        raw.getBooleans().write(0, false);
        raw.getBooleans().write(1, true);
        raw.getBooleans().write(2, true);
        raw.getBooleans().write(3, false);
        raw.getFloat().write(0, 0.05f);
        raw.getFloat().write(1, 0.1f);

        WrapperGameClientboundAbilities w = new WrapperGameClientboundAbilities(raw);
        assertFalse(w.isInvulnerable());
        assertTrue(w.isFlying());
        assertEquals(0.05f, w.getFlySpeed(), 1e-6f);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundAbilities w = new WrapperGameClientboundAbilities();
        w.setFlying(false);

        new WrapperGameClientboundAbilities(w.getHandle()).setFlying(true);

        assertTrue(w.isFlying());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundAbilities(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
