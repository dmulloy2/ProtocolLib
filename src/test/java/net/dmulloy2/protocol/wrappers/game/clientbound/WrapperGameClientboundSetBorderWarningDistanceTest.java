package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetBorderWarningDistanceTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetBorderWarningDistance w = new WrapperGameClientboundSetBorderWarningDistance();
        w.setWarningDistance(5);
        assertEquals(5, w.getWarningDistance());
        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 20);

        WrapperGameClientboundSetBorderWarningDistance w = new WrapperGameClientboundSetBorderWarningDistance(raw);
        assertEquals(20, w.getWarningDistance());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetBorderWarningDistance w = new WrapperGameClientboundSetBorderWarningDistance();
        w.setWarningDistance(1);

        new WrapperGameClientboundSetBorderWarningDistance(w.getHandle()).setWarningDistance(50);

        assertEquals(50, w.getWarningDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetBorderWarningDistance(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
