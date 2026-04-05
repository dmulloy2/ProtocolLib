package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundHelloPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundHelloPacket w = new WrappedServerboundHelloPacket("hello", UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"));

        assertEquals(PacketType.Login.Client.START, w.getHandle().getType());

        assertEquals("hello", w.getName());
        assertEquals(UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), w.getProfileId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundHelloPacket w = new WrappedServerboundHelloPacket();

        assertEquals(PacketType.Login.Client.START, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundHelloPacket source = new WrappedServerboundHelloPacket("hello", UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundHelloPacket wrapper = new WrappedServerboundHelloPacket(container);

        assertEquals("hello", wrapper.getName());
        assertEquals(UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), wrapper.getProfileId());

        wrapper.setName("modified");
        wrapper.setProfileId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));

        assertEquals("modified", wrapper.getName());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), wrapper.getProfileId());

        assertEquals("modified", source.getName());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), source.getProfileId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundHelloPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
