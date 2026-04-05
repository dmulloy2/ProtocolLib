package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlayerInputPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static WrappedServerboundPlayerInputPacket.WrappedInput makeInput(boolean forward, boolean sprint) {
        WrappedServerboundPlayerInputPacket.WrappedInput i = new WrappedServerboundPlayerInputPacket.WrappedInput();
        i.forward = forward;
        i.sprint = sprint;
        return i;
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundPlayerInputPacket w = new WrappedServerboundPlayerInputPacket(makeInput(true, true));

        assertEquals(PacketType.Play.Client.STEER_VEHICLE, w.getHandle().getType());

        assertTrue(w.getInput().forward);
        assertTrue(w.getInput().sprint);
        assertFalse(w.getInput().backward);
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerInputPacket w = new WrappedServerboundPlayerInputPacket();

        assertEquals(PacketType.Play.Client.STEER_VEHICLE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerInputPacket source = new WrappedServerboundPlayerInputPacket(makeInput(true, false));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerInputPacket wrapper = new WrappedServerboundPlayerInputPacket(container);

        assertTrue(wrapper.getInput().forward);
        assertFalse(wrapper.getInput().sprint);

        wrapper.setInput(makeInput(false, true));

        assertFalse(wrapper.getInput().forward);
        assertTrue(wrapper.getInput().sprint);

        assertFalse(source.getInput().forward);
        assertTrue(source.getInput().sprint);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerInputPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
