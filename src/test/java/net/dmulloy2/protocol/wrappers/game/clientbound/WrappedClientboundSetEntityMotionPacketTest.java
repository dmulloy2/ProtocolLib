package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEntityMotionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetEntityMotionPacket w = new WrappedClientboundSetEntityMotionPacket(3, new Vector(4.0, 5.0, 6.0));

        assertEquals(PacketType.Play.Server.ENTITY_VELOCITY, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(new Vector(4.0, 5.0, 6.0), w.getVelocity());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetEntityMotionPacket w = new WrappedClientboundSetEntityMotionPacket();

        assertEquals(PacketType.Play.Server.ENTITY_VELOCITY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetEntityMotionPacket source = new WrappedClientboundSetEntityMotionPacket(3, new Vector(4.0, 5.0, 6.0));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetEntityMotionPacket wrapper = new WrappedClientboundSetEntityMotionPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(new Vector(4.0, 5.0, 6.0), wrapper.getVelocity());

        wrapper.setEntityId(9);
        wrapper.setVelocity(new Vector(10.0, 20.0, 30.0));

        assertEquals(9, wrapper.getEntityId());
        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getVelocity());

        assertEquals(9, source.getEntityId());
        assertEquals(new Vector(10.0, 20.0, 30.0), source.getVelocity());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEntityMotionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
