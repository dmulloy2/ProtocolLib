package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundKeyPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundKeyPacket w = new WrappedServerboundKeyPacket(new byte[] { 1, 2, 3 }, new byte[] { 4, 5, 6 });

        assertEquals(PacketType.Login.Client.ENCRYPTION_BEGIN, w.getHandle().getType());

        assertArrayEquals(new byte[] { 1, 2, 3 }, w.getKeyBytes());
        assertArrayEquals(new byte[] { 4, 5, 6 }, w.getEncryptedChallenge());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundKeyPacket w = new WrappedServerboundKeyPacket();

        assertEquals(PacketType.Login.Client.ENCRYPTION_BEGIN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundKeyPacket source = new WrappedServerboundKeyPacket(new byte[] { 1, 2, 3 }, new byte[] { 4, 5, 6 });
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundKeyPacket wrapper = new WrappedServerboundKeyPacket(container);

        assertArrayEquals(new byte[] { 1, 2, 3 }, wrapper.getKeyBytes());
        assertArrayEquals(new byte[] { 4, 5, 6 }, wrapper.getEncryptedChallenge());

        wrapper.setKeyBytes(new byte[] { 10, 20, 30 });
        wrapper.setEncryptedChallenge(new byte[] { 10, 20, 30 });

        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getKeyBytes());
        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getEncryptedChallenge());

        assertArrayEquals(new byte[] { 10, 20, 30 }, source.getKeyBytes());
        assertArrayEquals(new byte[] { 10, 20, 30 }, source.getEncryptedChallenge());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundKeyPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
