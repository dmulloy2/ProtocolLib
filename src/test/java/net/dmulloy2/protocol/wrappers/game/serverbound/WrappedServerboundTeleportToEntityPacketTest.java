package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundTeleportToEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static final UUID UUID_A = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final UUID UUID_B = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Test
    void testAllArgsCreate() {
        WrappedServerboundTeleportToEntityPacket w = new WrappedServerboundTeleportToEntityPacket(UUID_A);

        assertEquals(PacketType.Play.Client.SPECTATE, w.getHandle().getType());

        assertEquals(UUID_A, w.getUuid());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundTeleportToEntityPacket w = new WrappedServerboundTeleportToEntityPacket();

        assertEquals(PacketType.Play.Client.SPECTATE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundTeleportToEntityPacket source = new WrappedServerboundTeleportToEntityPacket(UUID_A);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundTeleportToEntityPacket wrapper = new WrappedServerboundTeleportToEntityPacket(container);

        assertEquals(UUID_A, wrapper.getUuid());

        wrapper.setUuid(UUID_B);

        assertEquals(UUID_B, wrapper.getUuid());
        assertEquals(UUID_B, source.getUuid());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundTeleportToEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
