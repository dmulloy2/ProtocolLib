package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
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

        ServerboundPlayerAbilitiesPacket p = (ServerboundPlayerAbilitiesPacket) w.getHandle().getHandle();

        assertTrue(p.isFlying());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerAbilitiesPacket w = new WrappedServerboundPlayerAbilitiesPacket();

        assertEquals(PacketType.Play.Client.ABILITIES, w.getHandle().getType());

        ServerboundPlayerAbilitiesPacket p = (ServerboundPlayerAbilitiesPacket) w.getHandle().getHandle();

        assertFalse(p.isFlying());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerAbilitiesPacket src = new WrappedServerboundPlayerAbilitiesPacket(false);
        ServerboundPlayerAbilitiesPacket nmsPacket = (ServerboundPlayerAbilitiesPacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerAbilitiesPacket wrapper = new WrappedServerboundPlayerAbilitiesPacket(container);

        assertFalse(wrapper.isFlying());

        wrapper.setFlying(true);

        assertTrue(nmsPacket.isFlying());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerAbilitiesPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
