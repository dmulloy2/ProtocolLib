package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundCookieResponsePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundCookieResponsePacket w = new WrappedServerboundCookieResponsePacket(new MinecraftKey("minecraft", "stone"), null);

        assertEquals(PacketType.Play.Client.COOKIE_RESPONSE, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getKey());
        assertNull(w.getPayload());
    }

    @Test
    void testAllArgsCreateWithPayload() {
        byte[] data = new byte[] { 1, 2, 3 };
        WrappedServerboundCookieResponsePacket w = new WrappedServerboundCookieResponsePacket(new MinecraftKey("minecraft", "stone"), data);

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getKey());
        assertArrayEquals(data, w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundCookieResponsePacket w = new WrappedServerboundCookieResponsePacket();

        assertEquals(PacketType.Play.Client.COOKIE_RESPONSE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundCookieResponsePacket source = new WrappedServerboundCookieResponsePacket(new MinecraftKey("minecraft", "stone"), null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedServerboundCookieResponsePacket.TYPE, nmsPacket);
        WrappedServerboundCookieResponsePacket wrapper = new WrappedServerboundCookieResponsePacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getKey());
        assertNull(wrapper.getPayload());

        wrapper.setKey(new MinecraftKey("minecraft", "sand"));
        wrapper.setPayload(null);

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getKey());
        assertNull(wrapper.getPayload());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getKey());
        assertNull(source.getPayload());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundCookieResponsePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
