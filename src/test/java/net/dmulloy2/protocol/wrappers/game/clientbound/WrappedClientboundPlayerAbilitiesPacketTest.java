package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerAbilitiesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerAbilitiesPacket w = new WrappedClientboundPlayerAbilitiesPacket(true, false, true, true, 0.5f, -3.0f);

        assertEquals(PacketType.Play.Server.ABILITIES, w.getHandle().getType());

        assertTrue(w.isInvulnerable());
        assertFalse(w.isFlying());
        assertTrue(w.isCanFly());
        assertTrue(w.isCreativeMode());
        assertEquals(0.5f, w.getFlySpeed(), 1e-4f);
        assertEquals(-3.0f, w.getWalkSpeed(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerAbilitiesPacket w = new WrappedClientboundPlayerAbilitiesPacket();

        assertEquals(PacketType.Play.Server.ABILITIES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerAbilitiesPacket source = new WrappedClientboundPlayerAbilitiesPacket(true, false, true, true, 0.5f, -3.0f);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerAbilitiesPacket wrapper = new WrappedClientboundPlayerAbilitiesPacket(container);

        assertTrue(wrapper.isInvulnerable());
        assertFalse(wrapper.isFlying());
        assertTrue(wrapper.isCanFly());
        assertTrue(wrapper.isCreativeMode());
        assertEquals(0.5f, wrapper.getFlySpeed(), 1e-4f);
        assertEquals(-3.0f, wrapper.getWalkSpeed(), 1e-4f);

        wrapper.setInvulnerable(false);
        wrapper.setFlying(true);
        wrapper.setCanFly(false);
        wrapper.setCreativeMode(false);
        wrapper.setFlySpeed(0.25f);
        wrapper.setWalkSpeed(1.0f);

        assertFalse(wrapper.isInvulnerable());
        assertTrue(wrapper.isFlying());
        assertFalse(wrapper.isCanFly());
        assertFalse(wrapper.isCreativeMode());
        assertEquals(0.25f, wrapper.getFlySpeed(), 1e-4f);
        assertEquals(1.0f, wrapper.getWalkSpeed(), 1e-4f);

        assertFalse(source.isInvulnerable());
        assertTrue(source.isFlying());
        assertFalse(source.isCanFly());
        assertFalse(source.isCreativeMode());
        assertEquals(0.25f, source.getFlySpeed(), 1e-4f);
        assertEquals(1.0f, source.getWalkSpeed(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerAbilitiesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
