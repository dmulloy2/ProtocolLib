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
    void testAllArgsCreate() {
        WrappedClientboundBlockChangedAckPacket w = new WrappedClientboundBlockChangedAckPacket(3);

        assertEquals(PacketType.Play.Server.BLOCK_CHANGED_ACK, w.getHandle().getType());

        ClientboundBlockChangedAckPacket p = (ClientboundBlockChangedAckPacket) w.getHandle().getHandle();

        assertEquals(3, p.sequence());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBlockChangedAckPacket w = new WrappedClientboundBlockChangedAckPacket();

        assertEquals(PacketType.Play.Server.BLOCK_CHANGED_ACK, w.getHandle().getType());

        ClientboundBlockChangedAckPacket p = (ClientboundBlockChangedAckPacket) w.getHandle().getHandle();

        assertEquals(0, p.sequence());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundBlockChangedAckPacket nmsPacket = new ClientboundBlockChangedAckPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockChangedAckPacket wrapper = new WrappedClientboundBlockChangedAckPacket(container);

        assertEquals(3, wrapper.getSequence());

        wrapper.setSequence(9);

        assertEquals(9, nmsPacket.sequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockChangedAckPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
