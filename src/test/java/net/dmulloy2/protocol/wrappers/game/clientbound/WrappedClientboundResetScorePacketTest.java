package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundResetScorePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundResetScorePacket w = new WrappedClientboundResetScorePacket("hello", "world");

        assertEquals(PacketType.Play.Server.RESET_SCORE, w.getHandle().getType());

        ClientboundResetScorePacket p = (ClientboundResetScorePacket) w.getHandle().getHandle();

        assertEquals("hello", p.owner());
        assertEquals("world", p.objectiveName());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundResetScorePacket w = new WrappedClientboundResetScorePacket();

        assertEquals(PacketType.Play.Server.RESET_SCORE, w.getHandle().getType());

        ClientboundResetScorePacket p = (ClientboundResetScorePacket) w.getHandle().getHandle();


    }

    @Test
    void testModifyExistingPacket() {
        ClientboundResetScorePacket nmsPacket = new ClientboundResetScorePacket("hello", "world");
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundResetScorePacket wrapper = new WrappedClientboundResetScorePacket(container);

        assertEquals("hello", wrapper.getOwner());
        assertEquals("world", wrapper.getObjectiveName());

        wrapper.setOwner("modified");
        wrapper.setObjectiveName("hello");

        assertEquals("modified", nmsPacket.owner());
        assertEquals("hello", nmsPacket.objectiveName());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundResetScorePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
