package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTagQueryPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundTagQueryPacket w = new WrappedClientboundTagQueryPacket(3, null);

        assertEquals(PacketType.Play.Server.NBT_QUERY, w.getHandle().getType());

        assertEquals(3, w.getTransactionId());
        assertEquals(null, w.getTag());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTagQueryPacket w = new WrappedClientboundTagQueryPacket();

        assertEquals(PacketType.Play.Server.NBT_QUERY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTagQueryPacket source = new WrappedClientboundTagQueryPacket(3, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTagQueryPacket wrapper = new WrappedClientboundTagQueryPacket(container);

        assertEquals(3, wrapper.getTransactionId());
        assertEquals(null, wrapper.getTag());

        wrapper.setTransactionId(9);
        wrapper.setTag(null);

        assertEquals(9, wrapper.getTransactionId());
        assertEquals(null, wrapper.getTag());

        assertEquals(9, source.getTransactionId());
        assertEquals(null, source.getTag());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTagQueryPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
