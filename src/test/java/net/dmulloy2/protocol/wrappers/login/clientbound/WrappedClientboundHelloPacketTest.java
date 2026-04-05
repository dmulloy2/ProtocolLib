package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundHelloPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundHelloPacket w = new WrappedClientboundHelloPacket("hello", new byte[] { 4, 5, 6 }, new byte[] { 7, 8, 9 }, true);

        assertEquals(PacketType.Login.Server.ENCRYPTION_BEGIN, w.getHandle().getType());

        assertEquals("hello", w.getServerId());
        assertArrayEquals(new byte[] { 4, 5, 6 }, w.getPublicKey());
        assertArrayEquals(new byte[] { 7, 8, 9 }, w.getChallenge());
        assertTrue(w.isShouldAuthenticate());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundHelloPacket w = new WrappedClientboundHelloPacket();

        assertEquals(PacketType.Login.Server.ENCRYPTION_BEGIN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundHelloPacket source = new WrappedClientboundHelloPacket("hello", new byte[] { 4, 5, 6 }, new byte[] { 7, 8, 9 }, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundHelloPacket wrapper = new WrappedClientboundHelloPacket(container);

        assertEquals("hello", wrapper.getServerId());
        assertArrayEquals(new byte[] { 4, 5, 6 }, wrapper.getPublicKey());
        assertArrayEquals(new byte[] { 7, 8, 9 }, wrapper.getChallenge());
        assertTrue(wrapper.isShouldAuthenticate());

        wrapper.setServerId("modified");
        wrapper.setPublicKey(new byte[] { 10, 20, 30 });
        wrapper.setChallenge(new byte[] { 10, 20, 30 });
        wrapper.setShouldAuthenticate(false);

        assertEquals("modified", wrapper.getServerId());
        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getPublicKey());
        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getChallenge());
        assertFalse(wrapper.isShouldAuthenticate());

        assertEquals("modified", source.getServerId());
        assertArrayEquals(new byte[] { 10, 20, 30 }, source.getPublicKey());
        assertArrayEquals(new byte[] { 10, 20, 30 }, source.getChallenge());
        assertFalse(source.isShouldAuthenticate());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundHelloPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
