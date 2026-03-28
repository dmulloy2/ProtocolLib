package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundViewCentreTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundViewCentre w = new WrapperGameClientboundViewCentre();
        w.setChunkX(10);
        w.setChunkZ(-5);

        assertEquals(10, w.getChunkX());
        assertEquals(-5, w.getChunkZ());
        assertEquals(PacketType.Play.Server.VIEW_CENTRE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.VIEW_CENTRE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 3);
        raw.getIntegers().write(1, 7);

        WrapperGameClientboundViewCentre w = new WrapperGameClientboundViewCentre(raw);
        assertEquals(3, w.getChunkX());
        assertEquals(7, w.getChunkZ());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundViewCentre w = new WrapperGameClientboundViewCentre();
        w.setChunkX(0);
        w.setChunkZ(0);

        new WrapperGameClientboundViewCentre(w.getHandle()).setChunkZ(15);

        assertEquals(15, w.getChunkZ());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundViewCentre(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
