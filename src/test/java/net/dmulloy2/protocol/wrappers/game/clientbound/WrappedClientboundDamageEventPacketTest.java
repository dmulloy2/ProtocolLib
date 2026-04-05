package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDamageEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundDamageEventPacket w = new WrappedClientboundDamageEventPacket(3, 7, 5, Optional.empty());

        assertEquals(PacketType.Play.Server.DAMAGE_EVENT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(7, w.getSourceCauseId());
        assertEquals(5, w.getSourceDirectId());
        assertEquals(Optional.empty(), w.getSourcePosition());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDamageEventPacket w = new WrappedClientboundDamageEventPacket();

        assertEquals(PacketType.Play.Server.DAMAGE_EVENT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundDamageEventPacket source = new WrappedClientboundDamageEventPacket(3, 7, 5, Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDamageEventPacket wrapper = new WrappedClientboundDamageEventPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(7, wrapper.getSourceCauseId());
        assertEquals(5, wrapper.getSourceDirectId());
        assertEquals(Optional.empty(), wrapper.getSourcePosition());

        wrapper.setEntityId(9);
        wrapper.setSourceCauseId(-5);
        wrapper.setSourceDirectId(0);
        wrapper.setSourcePosition(Optional.empty());

        assertEquals(9, wrapper.getEntityId());
        assertEquals(-5, wrapper.getSourceCauseId());
        assertEquals(0, wrapper.getSourceDirectId());
        assertEquals(Optional.empty(), wrapper.getSourcePosition());

        assertEquals(9, source.getEntityId());
        assertEquals(-5, source.getSourceCauseId());
        assertEquals(0, source.getSourceDirectId());
        assertEquals(Optional.empty(), source.getSourcePosition());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDamageEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
