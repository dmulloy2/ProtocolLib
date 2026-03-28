package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerAbilitiesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundPlayerAbilitiesPacket w = new WrappedClientboundPlayerAbilitiesPacket();
        w.setInvulnerable(true);
        w.setFlying(false);
        w.setCanFly(true);
        w.setCreativeMode(true);
        w.setFlySpeed(0.1f);
        w.setWalkSpeed(0.2f);

        assertEquals(PacketType.Play.Server.ABILITIES, w.getHandle().getType());

        ClientboundPlayerAbilitiesPacket p = (ClientboundPlayerAbilitiesPacket) w.getHandle().getHandle();

        assertTrue(p.isInvulnerable());
        assertFalse(p.isFlying());
        assertTrue(p.canFly());
        assertTrue(p.canInstabuild());
        assertEquals(0.1f, p.getFlyingSpeed(), 1e-4f);
        assertEquals(0.2f, p.getWalkingSpeed(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ABILITIES);
        container.getModifier().writeDefaults();
        container.getBooleans().write(0, false);
        container.getBooleans().write(1, true);
        container.getBooleans().write(2, true);
        container.getBooleans().write(3, false);
        container.getFloat().write(0, 0.05f);
        container.getFloat().write(1, 0.1f);

        WrappedClientboundPlayerAbilitiesPacket wrapper = new WrappedClientboundPlayerAbilitiesPacket(container);

        assertFalse(wrapper.isInvulnerable());
        assertTrue(wrapper.isFlying());
        assertEquals(0.05f, wrapper.getFlySpeed(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ABILITIES);
        container.getModifier().writeDefaults();
        container.getBooleans().write(0, false);
        container.getBooleans().write(1, false);
        container.getBooleans().write(2, true);
        container.getBooleans().write(3, false);
        container.getFloat().write(0, 0.05f);
        container.getFloat().write(1, 0.1f);

        WrappedClientboundPlayerAbilitiesPacket wrapper = new WrappedClientboundPlayerAbilitiesPacket(container);
        wrapper.setFlying(true);

        assertFalse(wrapper.isInvulnerable());
        assertTrue(wrapper.isFlying());
        assertEquals(0.05f, wrapper.getFlySpeed(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerAbilitiesPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
