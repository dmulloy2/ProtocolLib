package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundCustomPayloadPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static CustomPacketPayloadWrapper testPayload() {
        return new CustomPacketPayloadWrapper(new byte[]{1, 2, 3}, new MinecraftKey("test", "payload"));
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundCustomPayloadPacket w = new WrappedServerboundCustomPayloadPacket(testPayload());

        assertEquals(PacketType.Play.Client.CUSTOM_PAYLOAD, w.getHandle().getType());

        assertNotNull(w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundCustomPayloadPacket w = new WrappedServerboundCustomPayloadPacket();

        assertEquals(PacketType.Play.Client.CUSTOM_PAYLOAD, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundCustomPayloadPacket source = new WrappedServerboundCustomPayloadPacket(testPayload());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedServerboundCustomPayloadPacket.TYPE, nmsPacket);
        WrappedServerboundCustomPayloadPacket wrapper = new WrappedServerboundCustomPayloadPacket(container);

        assertNotNull(wrapper.getPayload());

        wrapper.setPayload(new CustomPacketPayloadWrapper(new byte[]{4, 5, 6}, new MinecraftKey("test", "modified")));

        assertNotNull(wrapper.getPayload());
        assertNotNull(source.getPayload());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundCustomPayloadPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
