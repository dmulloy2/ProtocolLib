package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChangeDifficultyPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundChangeDifficultyPacket w = new WrappedClientboundChangeDifficultyPacket();
        w.setDifficulty(EnumWrappers.Difficulty.HARD);
        w.setLocked(true);

        assertEquals(PacketType.Play.Server.SERVER_DIFFICULTY, w.getHandle().getType());

        ClientboundChangeDifficultyPacket p = (ClientboundChangeDifficultyPacket) w.getHandle().getHandle();

        assertEquals(net.minecraft.world.Difficulty.HARD, p.difficulty());
        assertTrue(p.locked());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundChangeDifficultyPacket nmsPacket = new ClientboundChangeDifficultyPacket(
                net.minecraft.world.Difficulty.NORMAL, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundChangeDifficultyPacket wrapper = new WrappedClientboundChangeDifficultyPacket(container);

        assertEquals(EnumWrappers.Difficulty.NORMAL, wrapper.getDifficulty());
        assertFalse(wrapper.isLocked());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundChangeDifficultyPacket nmsPacket = new ClientboundChangeDifficultyPacket(
                net.minecraft.world.Difficulty.NORMAL, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundChangeDifficultyPacket wrapper = new WrappedClientboundChangeDifficultyPacket(container);

        wrapper.setDifficulty(EnumWrappers.Difficulty.PEACEFUL);

        assertEquals(EnumWrappers.Difficulty.PEACEFUL, wrapper.getDifficulty());
        assertFalse(wrapper.isLocked());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChangeDifficultyPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
