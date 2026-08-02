package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import java.util.HashSet;
import org.bukkit.util.Vector;
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
        WrappedClientboundPlayerPositionPacket w =
                new WrappedClientboundPlayerPositionPacket(3, position(4, 5, 6, 90, 45), new HashSet<>());

        assertEquals(PacketType.Play.Server.POSITION, w.getHandle().getType());

        assertEquals(3, w.getId());
        assertEquals(position(4, 5, 6, 90, 45), w.getChange());
        assertEquals(new HashSet<>(), w.getRelatives());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerPositionPacket w = new WrappedClientboundPlayerPositionPacket();

        assertEquals(PacketType.Play.Server.POSITION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerPositionPacket source =
                new WrappedClientboundPlayerPositionPacket(3, position(4, 5, 6, 90, 45), new HashSet<>());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerPositionPacket wrapper = new WrappedClientboundPlayerPositionPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals(position(4, 5, 6, 90, 45), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());

        wrapper.setId(9);
        wrapper.setChange(position(10, 20, 30, 270, -45));
        wrapper.setRelatives(new HashSet<>());

        assertEquals(9, wrapper.getId());
        assertEquals(position(10, 20, 30, 270, -45), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());

        assertEquals(9, source.getId());
        assertEquals(position(10, 20, 30, 270, -45), source.getChange());
        assertEquals(new HashSet<>(), source.getRelatives());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerPositionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }

    private static WrappedPositionMoveRotation position(
            double x, double y, double z, float yaw, float pitch) {
        return WrappedPositionMoveRotation.create(
                new Vector(x, y, z),
                new Vector(),
                yaw,
                pitch);
    }
}
