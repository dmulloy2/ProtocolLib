package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTakeItemEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundTakeItemEntityPacket w = new WrappedClientboundTakeItemEntityPacket(3, 7, 5);

        assertEquals(PacketType.Play.Server.COLLECT, w.getHandle().getType());

        ClientboundTakeItemEntityPacket p = (ClientboundTakeItemEntityPacket) w.getHandle().getHandle();

        assertEquals(3, p.getItemId());
        assertEquals(7, p.getPlayerId());
        assertEquals(5, p.getAmount());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTakeItemEntityPacket w = new WrappedClientboundTakeItemEntityPacket();

        assertEquals(PacketType.Play.Server.COLLECT, w.getHandle().getType());

        ClientboundTakeItemEntityPacket p = (ClientboundTakeItemEntityPacket) w.getHandle().getHandle();

        assertEquals(0, p.getItemId());
        assertEquals(0, p.getPlayerId());
        assertEquals(0, p.getAmount());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTakeItemEntityPacket nmsPacket = new ClientboundTakeItemEntityPacket(3, 7, 5);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTakeItemEntityPacket wrapper = new WrappedClientboundTakeItemEntityPacket(container);

        assertEquals(3, wrapper.getCollectedEntityId());
        assertEquals(7, wrapper.getCollectorEntityId());
        assertEquals(5, wrapper.getPickupItemCount());

        wrapper.setCollectedEntityId(9);
        wrapper.setCollectorEntityId(-5);
        wrapper.setPickupItemCount(0);

        assertEquals(9, nmsPacket.getItemId());
        assertEquals(-5, nmsPacket.getPlayerId());
        assertEquals(0, nmsPacket.getAmount());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTakeItemEntityPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
