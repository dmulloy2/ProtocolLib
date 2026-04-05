package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMapItemDataPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMapItemDataPacket w = new WrappedClientboundMapItemDataPacket(3, (byte) 1, true, Optional.empty(), Optional.empty());

        assertEquals(PacketType.Play.Server.MAP, w.getHandle().getType());

        assertEquals(3, w.getMapId());
        assertEquals((byte) 1, w.getScale());
        assertTrue(w.isLocked());
        assertEquals(Optional.empty(), w.getDecorations());
        assertEquals(Optional.empty(), w.getColorPatch());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMapItemDataPacket w = new WrappedClientboundMapItemDataPacket();

        assertEquals(PacketType.Play.Server.MAP, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundMapItemDataPacket source = new WrappedClientboundMapItemDataPacket(3, (byte) 1, true, Optional.empty(), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMapItemDataPacket wrapper = new WrappedClientboundMapItemDataPacket(container);

        assertEquals(3, wrapper.getMapId());
        assertEquals((byte) 1, wrapper.getScale());
        assertTrue(wrapper.isLocked());
        assertEquals(Optional.empty(), wrapper.getDecorations());
        assertEquals(Optional.empty(), wrapper.getColorPatch());

        wrapper.setMapId(9);
        wrapper.setScale((byte) -1);
        wrapper.setLocked(false);
        wrapper.setDecorations(Optional.empty());
        wrapper.setColorPatch(Optional.empty());

        assertEquals(9, wrapper.getMapId());
        assertEquals((byte) -1, wrapper.getScale());
        assertFalse(wrapper.isLocked());
        assertEquals(Optional.empty(), wrapper.getDecorations());
        assertEquals(Optional.empty(), wrapper.getColorPatch());

        assertEquals(9, source.getMapId());
        assertEquals((byte) -1, source.getScale());
        assertFalse(source.isLocked());
        assertEquals(Optional.empty(), source.getDecorations());
        assertEquals(Optional.empty(), source.getColorPatch());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMapItemDataPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
