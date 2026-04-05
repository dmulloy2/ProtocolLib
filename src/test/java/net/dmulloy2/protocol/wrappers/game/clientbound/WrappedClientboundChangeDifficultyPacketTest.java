package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChangeDifficultyPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundChangeDifficultyPacket w = new WrappedClientboundChangeDifficultyPacket(EnumWrappers.Difficulty.EASY, false);

        assertEquals(PacketType.Play.Server.SERVER_DIFFICULTY, w.getHandle().getType());

        ClientboundChangeDifficultyPacket p = (ClientboundChangeDifficultyPacket) w.getHandle().getHandle();

        assertEquals(Difficulty.EASY, p.difficulty());
        assertFalse(p.locked());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundChangeDifficultyPacket w = new WrappedClientboundChangeDifficultyPacket();

        assertEquals(PacketType.Play.Server.SERVER_DIFFICULTY, w.getHandle().getType());

        ClientboundChangeDifficultyPacket p = (ClientboundChangeDifficultyPacket) w.getHandle().getHandle();

        assertFalse(p.locked());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundChangeDifficultyPacket nmsPacket = new ClientboundChangeDifficultyPacket(Difficulty.EASY, false);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundChangeDifficultyPacket wrapper = new WrappedClientboundChangeDifficultyPacket(container);

        assertEquals(EnumWrappers.Difficulty.EASY, wrapper.getDifficulty());
        assertFalse(wrapper.isLocked());

        wrapper.setDifficulty(EnumWrappers.Difficulty.PEACEFUL);
        wrapper.setLocked(true);

        assertEquals(Difficulty.PEACEFUL, nmsPacket.difficulty());
        assertTrue(nmsPacket.locked());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChangeDifficultyPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
