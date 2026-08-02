package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLoginFinishedPacketTest {

    private static final UUID SESSION_ID =
            UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210");

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedGameProfile profile = new WrappedGameProfile(
                UUID.fromString("12345678-1234-1234-1234-123456789abc"), "TestPlayer");
        WrappedClientboundLoginFinishedPacket w =
                new WrappedClientboundLoginFinishedPacket(profile, SESSION_ID);
        assertEquals(PacketType.Login.Server.SUCCESS, w.getHandle().getType());
        assertEquals(profile, w.getGameProfile());
        assertEquals(SESSION_ID, w.getSessionId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLoginFinishedPacket w = new WrappedClientboundLoginFinishedPacket();
        assertEquals(PacketType.Login.Server.SUCCESS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedGameProfile profile = new WrappedGameProfile(
                UUID.fromString("12345678-1234-1234-1234-123456789abc"), "TestPlayer");
        WrappedClientboundLoginFinishedPacket src =
                new WrappedClientboundLoginFinishedPacket(profile, SESSION_ID);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundLoginFinishedPacket wrapper = new WrappedClientboundLoginFinishedPacket(container);
        assertEquals(profile, wrapper.getGameProfile());
        WrappedGameProfile profile2 = new WrappedGameProfile(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), "OtherPlayer");
        wrapper.setGameProfile(profile2);
        assertEquals(profile2, wrapper.getGameProfile());
        UUID updatedSessionId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        wrapper.setSessionId(updatedSessionId);
        assertEquals(updatedSessionId, src.getSessionId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLoginFinishedPacket(
                        new PacketContainer(PacketType.Login.Server.ENCRYPTION_BEGIN)));
    }
}
