package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCustomPayloadPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static CustomPacketPayloadWrapper testPayload() {
        return new CustomPacketPayloadWrapper(new byte[]{1, 2, 3}, new MinecraftKey("test", "payload"));
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundCustomPayloadPacket w = new WrappedClientboundCustomPayloadPacket(testPayload());

        assertEquals(PacketType.Play.Server.CUSTOM_PAYLOAD, w.getHandle().getType());

        assertNotNull(w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCustomPayloadPacket w = new WrappedClientboundCustomPayloadPacket();

        assertEquals(PacketType.Play.Server.CUSTOM_PAYLOAD, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCustomPayloadPacket source = new WrappedClientboundCustomPayloadPacket(testPayload());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundCustomPayloadPacket.TYPE, nmsPacket);
        WrappedClientboundCustomPayloadPacket wrapper = new WrappedClientboundCustomPayloadPacket(container);

        assertNotNull(wrapper.getPayload());

        wrapper.setPayload(new CustomPacketPayloadWrapper(new byte[]{4, 5, 6}, new MinecraftKey("test", "modified")));

        assertNotNull(wrapper.getPayload());
        assertNotNull(source.getPayload());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCustomPayloadPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
