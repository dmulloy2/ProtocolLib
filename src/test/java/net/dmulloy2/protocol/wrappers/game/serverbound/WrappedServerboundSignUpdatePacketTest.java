package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSignUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundSignUpdatePacket w = new WrappedServerboundSignUpdatePacket();
        w.setPos(new BlockPosition(10, 64, -5));
        w.setFrontText(true);
        w.setLines(new String[]{"Line 1", "Line 2", "Line 3", "Line 4"});

        assertEquals(PacketType.Play.Client.UPDATE_SIGN, w.getHandle().getType());

        ServerboundSignUpdatePacket p = (ServerboundSignUpdatePacket) w.getHandle().getHandle();

        assertNotNull(p.getPos());
        assertTrue(p.isFrontText());
        assertNotNull(p.getLines());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundSignUpdatePacket nmsPacket = new ServerboundSignUpdatePacket(
                new net.minecraft.core.BlockPos(3, 70, 3),
                true,
                "Hello", "World", "", ""
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSignUpdatePacket wrapper = new WrappedServerboundSignUpdatePacket(container);

        assertEquals(new BlockPosition(3, 70, 3), wrapper.getPos());
        assertTrue(wrapper.isFrontText());
        assertNotNull(wrapper.getLines());
        assertEquals(4, wrapper.getLines().length);
        assertEquals("Hello", wrapper.getLines()[0]);
        assertEquals("World", wrapper.getLines()[1]);
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSignUpdatePacket nmsPacket = new ServerboundSignUpdatePacket(
                new net.minecraft.core.BlockPos(0, 64, 0),
                true,
                "A", "B", "C", "D"
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSignUpdatePacket wrapper = new WrappedServerboundSignUpdatePacket(container);

        wrapper.setFrontText(false);

        assertEquals(new BlockPosition(0, 64, 0), wrapper.getPos());
        assertFalse(wrapper.isFrontText());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSignUpdatePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
