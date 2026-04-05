package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCookieRequestPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundCookieRequestPacket w = new WrappedClientboundCookieRequestPacket(new MinecraftKey("minecraft", "stone"));

        assertEquals(PacketType.Login.Server.COOKIE_REQUEST, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getKey());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCookieRequestPacket w = new WrappedClientboundCookieRequestPacket();

        assertEquals(PacketType.Login.Server.COOKIE_REQUEST, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCookieRequestPacket source = new WrappedClientboundCookieRequestPacket(new MinecraftKey("minecraft", "stone"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundCookieRequestPacket.TYPE, nmsPacket);
        WrappedClientboundCookieRequestPacket wrapper = new WrappedClientboundCookieRequestPacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getKey());

        wrapper.setKey(new MinecraftKey("minecraft", "sand"));

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getKey());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getKey());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCookieRequestPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
