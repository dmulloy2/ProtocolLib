package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundGameEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundGameEventPacket w = new WrappedClientboundGameEventPacket(3, 0.5f);

        assertEquals(PacketType.Play.Server.GAME_STATE_CHANGE, w.getHandle().getType());

        ClientboundGameEventPacket p = (ClientboundGameEventPacket) w.getHandle().getHandle();

        // Type.id is private; verify the event via the wrapper getter and param via the NMS accessor
        assertEquals(3, w.getEvent());
        assertEquals(0.5f, p.getParam(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundGameEventPacket w = new WrappedClientboundGameEventPacket();

        assertEquals(PacketType.Play.Server.GAME_STATE_CHANGE, w.getHandle().getType());
        assertEquals(0.0f, w.getValue(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundGameEventPacket nmsPacket = new ClientboundGameEventPacket(
                ClientboundGameEventPacket.CHANGE_GAME_MODE, 1.0f
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundGameEventPacket wrapper = new WrappedClientboundGameEventPacket(container);

        assertEquals(3, wrapper.getEvent());
        assertEquals(1.0f, wrapper.getValue(), 1e-4f);

        wrapper.setEvent(9);
        wrapper.setValue(-3.0f);

        assertEquals(9, wrapper.getEvent());
        assertEquals(-3.0f, wrapper.getValue(), 1e-4f);

        // Verify write-through to the original NMS packet
        assertEquals(-3.0f, nmsPacket.getParam(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundGameEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
