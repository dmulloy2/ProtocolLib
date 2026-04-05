package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCustomReportDetailsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundCustomReportDetailsPacket w = new WrappedClientboundCustomReportDetailsPacket(Map.of("key", "value"));

        assertEquals(PacketType.Play.Server.REPORT_DETAILS, w.getHandle().getType());

        assertEquals(Map.of("key", "value"), w.getDetails());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCustomReportDetailsPacket w = new WrappedClientboundCustomReportDetailsPacket();

        assertEquals(PacketType.Play.Server.REPORT_DETAILS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCustomReportDetailsPacket source = new WrappedClientboundCustomReportDetailsPacket(Map.of("key", "value"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundCustomReportDetailsPacket.TYPE, nmsPacket);
        WrappedClientboundCustomReportDetailsPacket wrapper = new WrappedClientboundCustomReportDetailsPacket(container);

        assertEquals(Map.of("key", "value"), wrapper.getDetails());
        wrapper.setDetails(Map.of("modified", "entry"));
        assertEquals(Map.of("modified", "entry"), wrapper.getDetails());
        assertEquals(Map.of("modified", "entry"), source.getDetails());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCustomReportDetailsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
