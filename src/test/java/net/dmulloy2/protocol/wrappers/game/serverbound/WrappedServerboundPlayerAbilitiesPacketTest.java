package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlayerAbilitiesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPlayerAbilitiesPacket w = new WrappedServerboundPlayerAbilitiesPacket(true);

        assertEquals(PacketType.Play.Client.ABILITIES, w.getHandle().getType());

        assertTrue(w.isFlying());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerAbilitiesPacket w = new WrappedServerboundPlayerAbilitiesPacket();

        assertEquals(PacketType.Play.Client.ABILITIES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerAbilitiesPacket source = new WrappedServerboundPlayerAbilitiesPacket(true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerAbilitiesPacket wrapper = new WrappedServerboundPlayerAbilitiesPacket(container);

        assertTrue(wrapper.isFlying());

        wrapper.setFlying(false);

        assertFalse(wrapper.isFlying());

        assertFalse(source.isFlying());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerAbilitiesPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
