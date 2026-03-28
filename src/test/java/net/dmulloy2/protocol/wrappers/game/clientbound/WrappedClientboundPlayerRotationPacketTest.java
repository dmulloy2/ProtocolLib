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
    void testCreate() {
        WrappedClientboundPlayerRotationPacket w = new WrappedClientboundPlayerRotationPacket();
        w.setYaw(90.0f);
        w.setPitch(-30.0f);

        assertEquals(PacketType.Play.Server.PLAYER_ROTATION, w.getHandle().getType());

        ClientboundPlayerRotationPacket p = (ClientboundPlayerRotationPacket) w.getHandle().getHandle();

        assertEquals(90.0f, p.yRot(), 1e-4f);
        assertEquals(-30.0f, p.xRot(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundPlayerRotationPacket nmsPacket = new ClientboundPlayerRotationPacket(45.0f, false, 10.0f, false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerRotationPacket wrapper = new WrappedClientboundPlayerRotationPacket(container);

        assertEquals(45.0f, wrapper.getYaw(), 1e-4f);
        assertEquals(10.0f, wrapper.getPitch(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPlayerRotationPacket nmsPacket = new ClientboundPlayerRotationPacket(45.0f, false, 10.0f, false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerRotationPacket wrapper = new WrappedClientboundPlayerRotationPacket(container);

        wrapper.setYaw(180.0f);

        assertEquals(180.0f, wrapper.getYaw(), 1e-4f);
        assertEquals(10.0f, wrapper.getPitch(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerRotationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
