package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundEditBookPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundEditBookPacket w = new WrappedServerboundEditBookPacket(3, List.of("world"), Optional.empty());

        assertEquals(PacketType.Play.Client.B_EDIT, w.getHandle().getType());

        assertEquals(3, w.getSlot());
        assertEquals(List.of("world"), w.getPages());
        assertEquals(Optional.empty(), w.getTitle());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundEditBookPacket w = new WrappedServerboundEditBookPacket();

        assertEquals(PacketType.Play.Client.B_EDIT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundEditBookPacket source = new WrappedServerboundEditBookPacket(3, List.of("world"), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundEditBookPacket wrapper = new WrappedServerboundEditBookPacket(container);

        assertEquals(3, wrapper.getSlot());
        assertEquals(List.of("world"), wrapper.getPages());
        assertEquals(Optional.empty(), wrapper.getTitle());

        wrapper.setSlot(9);
        wrapper.setPages(List.of("modified"));
        wrapper.setTitle(Optional.empty());

        assertEquals(9, wrapper.getSlot());
        assertEquals(List.of("modified"), wrapper.getPages());
        assertEquals(Optional.empty(), wrapper.getTitle());

        assertEquals(9, source.getSlot());
        assertEquals(List.of("modified"), source.getPages());
        assertEquals(Optional.empty(), source.getTitle());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundEditBookPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
