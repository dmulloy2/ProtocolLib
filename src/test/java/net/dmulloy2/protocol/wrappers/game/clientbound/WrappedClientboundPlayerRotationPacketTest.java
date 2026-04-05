package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerRotationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerRotationPacket w = new WrappedClientboundPlayerRotationPacket(0.75f, false, -3.0f, true);

        assertEquals(PacketType.Play.Server.PLAYER_ROTATION, w.getHandle().getType());

        ClientboundPlayerRotationPacket p = (ClientboundPlayerRotationPacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.yRot(), 1e-4f);
        assertFalse(p.relativeY());
        assertEquals(-3.0f, p.xRot(), 1e-4f);
        assertTrue(p.relativeX());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerRotationPacket w = new WrappedClientboundPlayerRotationPacket();

        assertEquals(PacketType.Play.Server.PLAYER_ROTATION, w.getHandle().getType());

        ClientboundPlayerRotationPacket p = (ClientboundPlayerRotationPacket) w.getHandle().getHandle();

        assertEquals(0.0f, p.yRot(), 1e-4f);
        assertFalse(p.relativeY());
        assertEquals(0.0f, p.xRot(), 1e-4f);
        assertFalse(p.relativeX());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPlayerRotationPacket nmsPacket = new ClientboundPlayerRotationPacket(0.75f, false, -3.0f, true);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerRotationPacket wrapper = new WrappedClientboundPlayerRotationPacket(container);

        assertEquals(0.75f, wrapper.getYaw(), 1e-4f);
        assertFalse(wrapper.isRelativeY());
        assertEquals(-3.0f, wrapper.getPitch(), 1e-4f);
        assertTrue(wrapper.isRelativeX());

        wrapper.setYaw(0.25f);
        wrapper.setRelativeY(true);
        wrapper.setPitch(1.0f);
        wrapper.setRelativeX(false);

        assertEquals(0.25f, nmsPacket.yRot(), 1e-4f);
        assertTrue(nmsPacket.relativeY());
        assertEquals(1.0f, nmsPacket.xRot(), 1e-4f);
        assertFalse(nmsPacket.relativeX());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerRotationPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
