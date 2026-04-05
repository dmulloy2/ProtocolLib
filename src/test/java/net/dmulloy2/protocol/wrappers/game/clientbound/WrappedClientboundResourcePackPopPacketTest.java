package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundResourcePackPopPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundResourcePackPopPacket w = new WrappedClientboundResourcePackPopPacket(Optional.empty());

        assertEquals(PacketType.Play.Server.REMOVE_RESOURCE_PACK, w.getHandle().getType());

        assertEquals(Optional.empty(), w.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundResourcePackPopPacket w = new WrappedClientboundResourcePackPopPacket();

        assertEquals(PacketType.Play.Server.REMOVE_RESOURCE_PACK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundResourcePackPopPacket source = new WrappedClientboundResourcePackPopPacket(Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundResourcePackPopPacket.TYPE, nmsPacket);
        WrappedClientboundResourcePackPopPacket wrapper = new WrappedClientboundResourcePackPopPacket(container);

        assertEquals(Optional.empty(), wrapper.getId());

        wrapper.setId(Optional.empty());

        assertEquals(Optional.empty(), wrapper.getId());

        assertEquals(Optional.empty(), source.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundResourcePackPopPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
