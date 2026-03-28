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
    void testCreate() {
        WrappedClientboundGameEventPacket w = new WrappedClientboundGameEventPacket();
        w.setEvent(WrappedClientboundGameEventPacket.BEGIN_RAINING);
        w.setValue(0.0f);

        assertEquals(PacketType.Play.Server.GAME_STATE_CHANGE, w.getHandle().getType());

        ClientboundGameEventPacket p = (ClientboundGameEventPacket) w.getHandle().getHandle();

        assertNotNull(p.getEvent());
        assertEquals(0.0f, p.getParam(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        container.getModifier().writeDefaults();
        container.getGameStateIDs().write(0, WrappedClientboundGameEventPacket.CHANGE_GAME_MODE);
        container.getFloat().write(0, 1.0f);

        WrappedClientboundGameEventPacket wrapper = new WrappedClientboundGameEventPacket(container);

        assertEquals(WrappedClientboundGameEventPacket.CHANGE_GAME_MODE, wrapper.getEvent());
        assertEquals(1.0f, wrapper.getValue(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        container.getModifier().writeDefaults();
        container.getGameStateIDs().write(0, WrappedClientboundGameEventPacket.CHANGE_GAME_MODE);
        container.getFloat().write(0, 1.0f);

        WrappedClientboundGameEventPacket wrapper = new WrappedClientboundGameEventPacket(container);
        wrapper.setValue(0.5f);

        assertEquals(WrappedClientboundGameEventPacket.CHANGE_GAME_MODE, wrapper.getEvent());
        assertEquals(0.5f, wrapper.getValue(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundGameEventPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
