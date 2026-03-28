package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundAttachEntityTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundAttachEntity w = new WrapperGameClientboundAttachEntity();
        w.setAttachedEntityId(5);
        w.setHoldingEntityId(10);
        assertEquals(5, w.getAttachedEntityId());
        assertEquals(10, w.getHoldingEntityId());
        assertEquals(PacketType.Play.Server.ATTACH_ENTITY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 11);
        raw.getIntegers().write(1, 22);

        WrapperGameClientboundAttachEntity w = new WrapperGameClientboundAttachEntity(raw);
        assertEquals(11, w.getAttachedEntityId());
        assertEquals(22, w.getHoldingEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundAttachEntity w = new WrapperGameClientboundAttachEntity();
        w.setAttachedEntityId(1);

        new WrapperGameClientboundAttachEntity(w.getHandle()).setAttachedEntityId(99);

        assertEquals(99, w.getAttachedEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundAttachEntity(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
