package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCodeOfConductPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundCodeOfConductPacket w = new WrappedClientboundCodeOfConductPacket("hello");
        assertEquals(PacketType.Configuration.Server.CODE_OF_CONDUCT, w.getHandle().getType());
        assertEquals("hello", w.getCodeOfConduct());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCodeOfConductPacket w = new WrappedClientboundCodeOfConductPacket();
        assertEquals(PacketType.Configuration.Server.CODE_OF_CONDUCT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCodeOfConductPacket src = new WrappedClientboundCodeOfConductPacket("hello");
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundCodeOfConductPacket wrapper = new WrappedClientboundCodeOfConductPacket(container);
        assertEquals("hello", wrapper.getCodeOfConduct());
        wrapper.setCodeOfConduct("modified");
        assertEquals("modified", wrapper.getCodeOfConduct());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCodeOfConductPacket(
                        new PacketContainer(PacketType.Configuration.Server.KEEP_ALIVE)));
    }
}
