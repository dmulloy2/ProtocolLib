package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundUpdateHealthTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundUpdateHealth w = new WrapperGameClientboundUpdateHealth();
        w.setHealth(16.5f);
        w.setFood(18);
        w.setSaturation(3.2f);

        assertEquals(16.5f, w.getHealth(),     1e-4f);
        assertEquals(18,    w.getFood());
        assertEquals(3.2f,  w.getSaturation(), 1e-4f);
        assertEquals(PacketType.Play.Server.UPDATE_HEALTH, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.UPDATE_HEALTH);
        raw.getModifier().writeDefaults();
        raw.getFloat().write(0, 20.0f);
        raw.getIntegers().write(0, 20);
        raw.getFloat().write(1, 5.0f);

        WrapperGameClientboundUpdateHealth w = new WrapperGameClientboundUpdateHealth(raw);
        assertEquals(20.0f, w.getHealth(),     1e-4f);
        assertEquals(20,    w.getFood());
        assertEquals(5.0f,  w.getSaturation(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundUpdateHealth w = new WrapperGameClientboundUpdateHealth();
        w.setHealth(20.0f);

        new WrapperGameClientboundUpdateHealth(w.getHandle()).setHealth(0.0f);

        assertEquals(0.0f, w.getHealth(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundUpdateHealth(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
