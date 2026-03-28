package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundCollectTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundCollect w = new WrapperGameClientboundCollect();
        w.setCollectedEntityId(10);
        w.setCollectorEntityId(20);
        w.setPickupItemCount(5);
        assertEquals(10, w.getCollectedEntityId());
        assertEquals(20, w.getCollectorEntityId());
        assertEquals(5, w.getPickupItemCount());
        assertEquals(PacketType.Play.Server.COLLECT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.COLLECT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 100);
        raw.getIntegers().write(1, 200);
        raw.getIntegers().write(2, 3);

        WrapperGameClientboundCollect w = new WrapperGameClientboundCollect(raw);
        assertEquals(100, w.getCollectedEntityId());
        assertEquals(200, w.getCollectorEntityId());
        assertEquals(3, w.getPickupItemCount());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundCollect w = new WrapperGameClientboundCollect();
        w.setPickupItemCount(1);

        new WrapperGameClientboundCollect(w.getHandle()).setPickupItemCount(64);

        assertEquals(64, w.getPickupItemCount());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundCollect(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
