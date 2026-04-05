package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundLockDifficultyPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundLockDifficultyPacket w = new WrappedServerboundLockDifficultyPacket(true);

        assertEquals(PacketType.Play.Client.DIFFICULTY_LOCK, w.getHandle().getType());

        ServerboundLockDifficultyPacket p = (ServerboundLockDifficultyPacket) w.getHandle().getHandle();

        assertTrue(p.isLocked());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundLockDifficultyPacket w = new WrappedServerboundLockDifficultyPacket();

        assertEquals(PacketType.Play.Client.DIFFICULTY_LOCK, w.getHandle().getType());

        ServerboundLockDifficultyPacket p = (ServerboundLockDifficultyPacket) w.getHandle().getHandle();

        assertFalse(p.isLocked());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundLockDifficultyPacket nmsPacket = new ServerboundLockDifficultyPacket(true);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundLockDifficultyPacket wrapper = new WrappedServerboundLockDifficultyPacket(container);

        assertTrue(wrapper.isLocked());

        wrapper.setLocked(false);

        assertFalse(nmsPacket.isLocked());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundLockDifficultyPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
