package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetHeldSlotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetHeldSlotPacket w = new WrappedClientboundSetHeldSlotPacket();
        w.setSlot(4);

        assertEquals(4, w.getSlot());
        assertEquals(PacketType.Play.Server.HELD_ITEM_SLOT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.HELD_ITEM_SLOT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);

        WrappedClientboundSetHeldSlotPacket w = new WrappedClientboundSetHeldSlotPacket(raw);
        assertEquals(7, w.getSlot());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetHeldSlotPacket w = new WrappedClientboundSetHeldSlotPacket();
        w.setSlot(0);

        new WrappedClientboundSetHeldSlotPacket(w.getHandle()).setSlot(8);

        assertEquals(8, w.getSlot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetHeldSlotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
