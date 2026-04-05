package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerPositionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerPositionPacket w = new WrappedClientboundPlayerPositionPacket(3, new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), new HashSet<>());

        assertEquals(PacketType.Play.Server.POSITION, w.getHandle().getType());

        assertEquals(3, w.getId());
        assertEquals(new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), w.getChange());
        assertEquals(new HashSet<>(), w.getRelatives());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerPositionPacket w = new WrappedClientboundPlayerPositionPacket();

        assertEquals(PacketType.Play.Server.POSITION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerPositionPacket source = new WrappedClientboundPlayerPositionPacket(3, new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), new HashSet<>());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerPositionPacket wrapper = new WrappedClientboundPlayerPositionPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals(new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());

        wrapper.setId(9);
        wrapper.setChange(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f));
        wrapper.setRelatives(new HashSet<>());

        assertEquals(9, wrapper.getId());
        assertEquals(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());

        assertEquals(9, source.getId());
        assertEquals(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f), source.getChange());
        assertEquals(new HashSet<>(), source.getRelatives());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerPositionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
