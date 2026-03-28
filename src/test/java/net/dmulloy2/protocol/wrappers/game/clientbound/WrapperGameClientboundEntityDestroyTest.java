package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundEntityDestroyTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundEntityDestroy w = new WrapperGameClientboundEntityDestroy();
        List<Integer> ids = List.of(10, 20, 30);
        w.setEntityIds(ids);
        List<Integer> result = w.getEntityIds();
        assertEquals(3, result.size());
        assertTrue(result.contains(10));
        assertTrue(result.contains(20));
        assertTrue(result.contains(30));
        assertEquals(PacketType.Play.Server.ENTITY_DESTROY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        raw.getModifier().writeDefaults();
        raw.getIntLists().write(0, List.of(1, 2, 3));

        WrapperGameClientboundEntityDestroy w = new WrapperGameClientboundEntityDestroy(raw);
        assertEquals(3, w.getEntityIds().size());
        assertTrue(w.getEntityIds().contains(1));
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundEntityDestroy w = new WrapperGameClientboundEntityDestroy();
        w.setEntityIds(List.of(1));

        new WrapperGameClientboundEntityDestroy(w.getHandle()).setEntityIds(List.of(5, 6, 7, 8));

        assertEquals(4, w.getEntityIds().size());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundEntityDestroy(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
