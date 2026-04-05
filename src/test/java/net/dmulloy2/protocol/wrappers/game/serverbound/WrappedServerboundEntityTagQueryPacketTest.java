package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundEntityTagQueryPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundEntityTagQueryPacket w = new WrappedServerboundEntityTagQueryPacket(3, 7);

        assertEquals(PacketType.Play.Client.ENTITY_NBT_QUERY, w.getHandle().getType());

        ServerboundEntityTagQueryPacket p = (ServerboundEntityTagQueryPacket) w.getHandle().getHandle();

        assertEquals(3, p.getTransactionId());
        assertEquals(7, p.getEntityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundEntityTagQueryPacket w = new WrappedServerboundEntityTagQueryPacket();

        assertEquals(PacketType.Play.Client.ENTITY_NBT_QUERY, w.getHandle().getType());

        ServerboundEntityTagQueryPacket p = (ServerboundEntityTagQueryPacket) w.getHandle().getHandle();

        assertEquals(0, p.getTransactionId());
        assertEquals(0, p.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundEntityTagQueryPacket nmsPacket = new ServerboundEntityTagQueryPacket(3, 7);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundEntityTagQueryPacket wrapper = new WrappedServerboundEntityTagQueryPacket(container);

        assertEquals(3, wrapper.getTransactionId());
        assertEquals(7, wrapper.getEntityId());

        wrapper.setTransactionId(9);
        wrapper.setEntityId(-5);

        assertEquals(9, nmsPacket.getTransactionId());
        assertEquals(-5, nmsPacket.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundEntityTagQueryPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
