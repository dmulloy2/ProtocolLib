package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundOpenSignEditorPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundOpenSignEditorPacket w = new WrappedClientboundOpenSignEditorPacket();
        w.setPos(new BlockPosition(3, 70, -3));
        w.setFrontText(true);

        assertEquals(PacketType.Play.Server.OPEN_SIGN_EDITOR, w.getHandle().getType());

        ClientboundOpenSignEditorPacket p = (ClientboundOpenSignEditorPacket) w.getHandle().getHandle();

        assertTrue(p.isFrontText());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundOpenSignEditorPacket nmsPacket = new ClientboundOpenSignEditorPacket(
                new net.minecraft.core.BlockPos(10, 64, 10), false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundOpenSignEditorPacket wrapper = new WrappedClientboundOpenSignEditorPacket(container);

        assertEquals(new BlockPosition(10, 64, 10), wrapper.getPos());
        assertFalse(wrapper.isFrontText());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundOpenSignEditorPacket nmsPacket = new ClientboundOpenSignEditorPacket(
                new net.minecraft.core.BlockPos(10, 64, 10), false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundOpenSignEditorPacket wrapper = new WrappedClientboundOpenSignEditorPacket(container);

        wrapper.setFrontText(true);

        assertEquals(new BlockPosition(10, 64, 10), wrapper.getPos());
        assertTrue(wrapper.isFrontText());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundOpenSignEditorPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
