package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderSizePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetBorderSizePacket w = new WrappedClientboundSetBorderSizePacket();
        w.setDiameter(1024.0);

        assertEquals(PacketType.Play.Server.SET_BORDER_SIZE, w.getHandle().getType());

        ClientboundSetBorderSizePacket p = (ClientboundSetBorderSizePacket) w.getHandle().getHandle();

        assertEquals(1024.0, p.getSize(), 1e-6);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_SIZE);
        container.getModifier().writeDefaults();
        container.getDoubles().write(0, 60000000.0);

        WrappedClientboundSetBorderSizePacket wrapper = new WrappedClientboundSetBorderSizePacket(container);

        assertEquals(60000000.0, wrapper.getDiameter(), 1e-6);
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_SIZE);
        container.getModifier().writeDefaults();
        container.getDoubles().write(0, 100.0);

        WrappedClientboundSetBorderSizePacket wrapper = new WrappedClientboundSetBorderSizePacket(container);
        wrapper.setDiameter(200.0);

        assertEquals(200.0, wrapper.getDiameter(), 1e-6);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderSizePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
