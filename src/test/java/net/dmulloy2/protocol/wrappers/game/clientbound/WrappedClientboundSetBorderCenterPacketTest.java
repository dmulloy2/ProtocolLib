package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderCenterPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetBorderCenterPacket w = new WrappedClientboundSetBorderCenterPacket();
        w.setX(100.5);
        w.setZ(-200.5);

        assertEquals(PacketType.Play.Server.SET_BORDER_CENTER, w.getHandle().getType());

        ClientboundSetBorderCenterPacket p = (ClientboundSetBorderCenterPacket) w.getHandle().getHandle();

        assertEquals(100.5, p.getNewCenterX(), 1e-6);
        assertEquals(-200.5, p.getNewCenterZ(), 1e-6);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_CENTER);
        container.getModifier().writeDefaults();
        container.getDoubles().write(0, 500.0);
        container.getDoubles().write(1, -300.0);

        WrappedClientboundSetBorderCenterPacket wrapper = new WrappedClientboundSetBorderCenterPacket(container);

        assertEquals(500.0, wrapper.getX(), 1e-6);
        assertEquals(-300.0, wrapper.getZ(), 1e-6);
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_CENTER);
        container.getModifier().writeDefaults();
        container.getDoubles().write(0, 500.0);
        container.getDoubles().write(1, -300.0);

        WrappedClientboundSetBorderCenterPacket wrapper = new WrappedClientboundSetBorderCenterPacket(container);
        wrapper.setX(600.0);

        assertEquals(600.0, wrapper.getX(), 1e-6);
        assertEquals(-300.0, wrapper.getZ(), 1e-6);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderCenterPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
