package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderWarningDistancePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetBorderWarningDistancePacket w = new WrappedClientboundSetBorderWarningDistancePacket(3);

        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE, w.getHandle().getType());

        assertEquals(3, w.getWarningDistance());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetBorderWarningDistancePacket w = new WrappedClientboundSetBorderWarningDistancePacket();

        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetBorderWarningDistancePacket source = new WrappedClientboundSetBorderWarningDistancePacket(3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetBorderWarningDistancePacket wrapper = new WrappedClientboundSetBorderWarningDistancePacket(container);

        assertEquals(3, wrapper.getWarningDistance());

        wrapper.setWarningDistance(9);

        assertEquals(9, wrapper.getWarningDistance());

        assertEquals(9, source.getWarningDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderWarningDistancePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
