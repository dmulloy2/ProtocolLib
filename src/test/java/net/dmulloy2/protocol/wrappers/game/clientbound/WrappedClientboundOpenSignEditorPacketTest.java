package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundOpenSignEditorPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundOpenSignEditorPacket w = new WrappedClientboundOpenSignEditorPacket(new BlockPosition(1, 2, 3), false);

        assertEquals(PacketType.Play.Server.OPEN_SIGN_EDITOR, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertFalse(w.isFrontText());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundOpenSignEditorPacket w = new WrappedClientboundOpenSignEditorPacket();

        assertEquals(PacketType.Play.Server.OPEN_SIGN_EDITOR, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundOpenSignEditorPacket source = new WrappedClientboundOpenSignEditorPacket(new BlockPosition(1, 2, 3), false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundOpenSignEditorPacket wrapper = new WrappedClientboundOpenSignEditorPacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertFalse(wrapper.isFrontText());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setFrontText(true);

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertTrue(wrapper.isFrontText());

        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertTrue(source.isFrontText());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundOpenSignEditorPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
