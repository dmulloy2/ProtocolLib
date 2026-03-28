package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundOpenBookTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundOpenBook w = new WrapperGameClientboundOpenBook();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);
        assertEquals(EnumWrappers.Hand.MAIN_HAND, w.getHand());
        assertEquals(PacketType.Play.Server.OPEN_BOOK, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.OPEN_BOOK);
        raw.getModifier().writeDefaults();
        raw.getHands().write(0, EnumWrappers.Hand.OFF_HAND);

        WrapperGameClientboundOpenBook w = new WrapperGameClientboundOpenBook(raw);
        assertEquals(EnumWrappers.Hand.OFF_HAND, w.getHand());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundOpenBook w = new WrapperGameClientboundOpenBook();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);

        new WrapperGameClientboundOpenBook(w.getHandle()).setHand(EnumWrappers.Hand.OFF_HAND);

        assertEquals(EnumWrappers.Hand.OFF_HAND, w.getHand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundOpenBook(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
