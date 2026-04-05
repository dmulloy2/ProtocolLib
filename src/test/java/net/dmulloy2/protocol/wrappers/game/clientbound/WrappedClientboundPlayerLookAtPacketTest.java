package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static net.dmulloy2.protocol.wrappers.game.clientbound.WrappedClientboundPlayerLookAtPacket.Anchor.*;

class WrappedClientboundPlayerLookAtPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerLookAtPacket w = new WrappedClientboundPlayerLookAtPacket(FEET, EYES, -2.5, 3.14, 100.0, true, 3);

        assertEquals(PacketType.Play.Server.LOOK_AT, w.getHandle().getType());

        assertEquals(FEET, w.getFromAnchor());
        assertEquals(EYES, w.getToAnchor());
        assertEquals(-2.5, w.getX(), 1e-9);
        assertEquals(3.14, w.getY(), 1e-9);
        assertEquals(100.0, w.getZ(), 1e-9);
        assertTrue(w.isAtEntity());
        assertEquals(3, w.getEntity());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerLookAtPacket w = new WrappedClientboundPlayerLookAtPacket();

        assertEquals(PacketType.Play.Server.LOOK_AT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerLookAtPacket source = new WrappedClientboundPlayerLookAtPacket(FEET, EYES, -2.5, 3.14, 100.0, true, 3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerLookAtPacket wrapper = new WrappedClientboundPlayerLookAtPacket(container);

        assertEquals(FEET, wrapper.getFromAnchor());
        assertEquals(EYES, wrapper.getToAnchor());

        wrapper.setFromAnchor(EYES);
        wrapper.setToAnchor(FEET);
        wrapper.setX(0.0);
        wrapper.setY(100.0);
        wrapper.setZ(2.71);
        wrapper.setAtEntity(false);
        wrapper.setEntity(0);

        assertEquals(EYES, wrapper.getFromAnchor());
        assertEquals(FEET, wrapper.getToAnchor());
        assertEquals(0.0, wrapper.getX(), 1e-9);
        assertEquals(100.0, wrapper.getY(), 1e-9);
        assertEquals(2.71, wrapper.getZ(), 1e-9);
        assertFalse(wrapper.isAtEntity());
        assertEquals(0, wrapper.getEntity());

        assertEquals(EYES, source.getFromAnchor());
        assertEquals(FEET, source.getToAnchor());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerLookAtPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
