package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundStoreCookiePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundStoreCookiePacket w = new WrappedClientboundStoreCookiePacket(new MinecraftKey("minecraft", "stone"), new byte[] { 4, 5, 6 });

        assertEquals(PacketType.Play.Server.STORE_COOKIE, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getKey());
        assertArrayEquals(new byte[] { 4, 5, 6 }, w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundStoreCookiePacket w = new WrappedClientboundStoreCookiePacket();

        assertEquals(PacketType.Play.Server.STORE_COOKIE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundStoreCookiePacket source = new WrappedClientboundStoreCookiePacket(new MinecraftKey("minecraft", "stone"), new byte[] { 4, 5, 6 });
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundStoreCookiePacket.TYPE, nmsPacket);
        WrappedClientboundStoreCookiePacket wrapper = new WrappedClientboundStoreCookiePacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getKey());
        assertArrayEquals(new byte[] { 4, 5, 6 }, wrapper.getPayload());

        wrapper.setKey(new MinecraftKey("minecraft", "sand"));
        wrapper.setPayload(new byte[] { 10, 20, 30 });

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getKey());
        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getPayload());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getKey());
        assertArrayEquals(new byte[] { 10, 20, 30 }, source.getPayload());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundStoreCookiePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
