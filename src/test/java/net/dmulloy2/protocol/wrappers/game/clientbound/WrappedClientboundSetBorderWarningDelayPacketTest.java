package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderWarningDelayPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetBorderWarningDelayPacket w = new WrappedClientboundSetBorderWarningDelayPacket(3);

        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DELAY, w.getHandle().getType());

        assertEquals(3, w.getWarningDelay());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetBorderWarningDelayPacket w = new WrappedClientboundSetBorderWarningDelayPacket();

        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DELAY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetBorderWarningDelayPacket source = new WrappedClientboundSetBorderWarningDelayPacket(3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetBorderWarningDelayPacket wrapper = new WrappedClientboundSetBorderWarningDelayPacket(container);

        assertEquals(3, wrapper.getWarningDelay());

        wrapper.setWarningDelay(9);

        assertEquals(9, wrapper.getWarningDelay());

        assertEquals(9, source.getWarningDelay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderWarningDelayPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
