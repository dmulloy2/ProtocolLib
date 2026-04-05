package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChangeDifficultyPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundChangeDifficultyPacket w = new WrappedServerboundChangeDifficultyPacket(EnumWrappers.Difficulty.EASY);

        assertEquals(PacketType.Play.Client.DIFFICULTY_CHANGE, w.getHandle().getType());

        ServerboundChangeDifficultyPacket p = (ServerboundChangeDifficultyPacket) w.getHandle().getHandle();

        assertEquals(Difficulty.EASY, p.difficulty());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChangeDifficultyPacket w = new WrappedServerboundChangeDifficultyPacket();

        assertEquals(PacketType.Play.Client.DIFFICULTY_CHANGE, w.getHandle().getType());

        ServerboundChangeDifficultyPacket p = (ServerboundChangeDifficultyPacket) w.getHandle().getHandle();


    }

    @Test
    void testModifyExistingPacket() {
        ServerboundChangeDifficultyPacket nmsPacket = new ServerboundChangeDifficultyPacket(Difficulty.EASY);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChangeDifficultyPacket wrapper = new WrappedServerboundChangeDifficultyPacket(container);

        assertEquals(EnumWrappers.Difficulty.EASY, wrapper.getDifficulty());

        wrapper.setDifficulty(EnumWrappers.Difficulty.PEACEFUL);

        assertEquals(Difficulty.PEACEFUL, nmsPacket.difficulty());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChangeDifficultyPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
