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
    void testCreate() {
        WrappedClientboundTakeItemEntityPacket w = new WrappedClientboundTakeItemEntityPacket();
        w.setCollectedEntityId(10);
        w.setCollectorEntityId(20);
        w.setPickupItemCount(5);

        assertEquals(PacketType.Play.Server.COLLECT, w.getHandle().getType());

        ClientboundTakeItemEntityPacket p = (ClientboundTakeItemEntityPacket) w.getHandle().getHandle();

        assertEquals(10, p.getItemId());
        assertEquals(20, p.getPlayerId());
        assertEquals(5, p.getAmount());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundTakeItemEntityPacket nmsPacket = new ClientboundTakeItemEntityPacket(100, 200, 3);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTakeItemEntityPacket wrapper = new WrappedClientboundTakeItemEntityPacket(container);

        assertEquals(100, wrapper.getCollectedEntityId());
        assertEquals(200, wrapper.getCollectorEntityId());
        assertEquals(3, wrapper.getPickupItemCount());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTakeItemEntityPacket nmsPacket = new ClientboundTakeItemEntityPacket(10, 20, 1);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTakeItemEntityPacket wrapper = new WrappedClientboundTakeItemEntityPacket(container);

        wrapper.setPickupItemCount(64);

        assertEquals(10, wrapper.getCollectedEntityId());
        assertEquals(20, wrapper.getCollectorEntityId());
        assertEquals(64, wrapper.getPickupItemCount());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTakeItemEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
