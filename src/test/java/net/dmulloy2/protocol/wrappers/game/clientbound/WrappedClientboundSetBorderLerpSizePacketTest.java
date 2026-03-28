package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderLerpSizePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetBorderLerpSizePacket w = new WrappedClientboundSetBorderLerpSizePacket();
        w.setOldDiameter(1000.0);
        w.setNewDiameter(500.0);
        w.setSpeed(10000L);

        assertEquals(PacketType.Play.Server.SET_BORDER_LERP_SIZE, w.getHandle().getType());

        ClientboundSetBorderLerpSizePacket p = (ClientboundSetBorderLerpSizePacket) w.getHandle().getHandle();

        assertEquals(1000.0, p.getOldSize(), 1e-6);
        assertEquals(500.0, p.getNewSize(), 1e-6);
        assertEquals(10000L, p.getLerpTime());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_LERP_SIZE);
        container.getModifier().writeDefaults();
        container.getDoubles().write(0, 200.0);
        container.getDoubles().write(1, 100.0);
        container.getLongs().write(0, 5000L);

        WrappedClientboundSetBorderLerpSizePacket wrapper = new WrappedClientboundSetBorderLerpSizePacket(container);

        assertEquals(200.0, wrapper.getOldDiameter(), 1e-6);
        assertEquals(100.0, wrapper.getNewDiameter(), 1e-6);
        assertEquals(5000L, wrapper.getSpeed());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_LERP_SIZE);
        container.getModifier().writeDefaults();
        container.getDoubles().write(0, 200.0);
        container.getDoubles().write(1, 100.0);
        container.getLongs().write(0, 5000L);

        WrappedClientboundSetBorderLerpSizePacket wrapper = new WrappedClientboundSetBorderLerpSizePacket(container);
        wrapper.setSpeed(9999L);

        assertEquals(200.0, wrapper.getOldDiameter(), 1e-6);
        assertEquals(100.0, wrapper.getNewDiameter(), 1e-6);
        assertEquals(9999L, wrapper.getSpeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderLerpSizePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
