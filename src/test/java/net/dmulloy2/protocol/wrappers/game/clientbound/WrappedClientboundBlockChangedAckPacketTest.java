package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBlockChangedAckPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundBlockChangedAckPacket w = new WrappedClientboundBlockChangedAckPacket();
        w.setSequence(17);

        assertEquals(PacketType.Play.Server.BLOCK_CHANGED_ACK, w.getHandle().getType());

        ClientboundBlockChangedAckPacket p = (ClientboundBlockChangedAckPacket) w.getHandle().getHandle();

        assertEquals(17, p.sequence());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundBlockChangedAckPacket nmsPacket = new ClientboundBlockChangedAckPacket(33);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockChangedAckPacket wrapper = new WrappedClientboundBlockChangedAckPacket(container);

        assertEquals(33, wrapper.getSequence());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundBlockChangedAckPacket nmsPacket = new ClientboundBlockChangedAckPacket(33);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockChangedAckPacket wrapper = new WrappedClientboundBlockChangedAckPacket(container);

        wrapper.setSequence(99);

        assertEquals(99, wrapper.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockChangedAckPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
