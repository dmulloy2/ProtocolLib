package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetCameraPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetCameraPacket w = new WrappedClientboundSetCameraPacket();
        w.setCameraEntityId(100);

        assertEquals(PacketType.Play.Server.CAMERA, w.getHandle().getType());

        ClientboundSetCameraPacket p = (ClientboundSetCameraPacket) w.getHandle().getHandle();

        assertNotNull(p);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.CAMERA);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 42);

        WrappedClientboundSetCameraPacket wrapper = new WrappedClientboundSetCameraPacket(container);

        assertEquals(42, wrapper.getCameraEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.CAMERA);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 42);

        WrappedClientboundSetCameraPacket wrapper = new WrappedClientboundSetCameraPacket(container);
        wrapper.setCameraEntityId(5);

        assertEquals(5, wrapper.getCameraEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetCameraPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
