package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Optional;
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
        WrappedServerboundCookieResponsePacket w = new WrappedServerboundCookieResponsePacket(new MinecraftKey("minecraft", "stone"), Optional.empty());

        assertEquals(PacketType.Login.Client.COOKIE_RESPONSE, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getKey());
        assertEquals(Optional.empty(), w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundCookieResponsePacket w = new WrappedServerboundCookieResponsePacket();

        assertEquals(PacketType.Login.Client.COOKIE_RESPONSE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundCookieResponsePacket source = new WrappedServerboundCookieResponsePacket(new MinecraftKey("minecraft", "stone"), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedServerboundCookieResponsePacket.TYPE, nmsPacket);
        WrappedServerboundCookieResponsePacket wrapper = new WrappedServerboundCookieResponsePacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getKey());
        assertEquals(Optional.empty(), wrapper.getPayload());

        wrapper.setKey(new MinecraftKey("minecraft", "sand"));
        wrapper.setPayload(Optional.empty());

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getKey());
        assertEquals(Optional.empty(), wrapper.getPayload());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getKey());
        assertEquals(Optional.empty(), source.getPayload());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundCookieResponsePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
