package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundHeldItemSlotTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundHeldItemSlot w = new WrapperGameClientboundHeldItemSlot();
        w.setSlot(4);

        assertEquals(4, w.getSlot());
        assertEquals(PacketType.Play.Server.HELD_ITEM_SLOT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.HELD_ITEM_SLOT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);

        WrapperGameClientboundHeldItemSlot w = new WrapperGameClientboundHeldItemSlot(raw);
        assertEquals(7, w.getSlot());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundHeldItemSlot w = new WrapperGameClientboundHeldItemSlot();
        w.setSlot(0);

        new WrapperGameClientboundHeldItemSlot(w.getHandle()).setSlot(8);

        assertEquals(8, w.getSlot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundHeldItemSlot(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
