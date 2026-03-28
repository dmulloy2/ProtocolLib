package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundOpenSignEditorTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundOpenSignEditor w = new WrapperGameClientboundOpenSignEditor();
        BlockPosition pos = new BlockPosition(3, 70, -3);
        w.setPos(pos);
        w.setFrontText(true);
        assertEquals(pos, w.getPos());
        assertTrue(w.isFrontText());
        assertEquals(PacketType.Play.Server.OPEN_SIGN_EDITOR, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        raw.getModifier().writeDefaults();
        raw.getBlockPositionModifier().write(0, new BlockPosition(10, 64, 10));
        raw.getBooleans().write(0, false);

        WrapperGameClientboundOpenSignEditor w = new WrapperGameClientboundOpenSignEditor(raw);
        assertEquals(new BlockPosition(10, 64, 10), w.getPos());
        assertFalse(w.isFrontText());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundOpenSignEditor w = new WrapperGameClientboundOpenSignEditor();
        w.setFrontText(false);

        new WrapperGameClientboundOpenSignEditor(w.getHandle()).setFrontText(true);

        assertTrue(w.isFrontText());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundOpenSignEditor(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
