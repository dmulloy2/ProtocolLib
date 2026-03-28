package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetHealthPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetHealthPacket w = new WrappedClientboundSetHealthPacket();
        w.setHealth(16.5f);
        w.setFood(18);
        w.setSaturation(3.2f);

        assertEquals(PacketType.Play.Server.UPDATE_HEALTH, w.getHandle().getType());

        ClientboundSetHealthPacket p = (ClientboundSetHealthPacket) w.getHandle().getHandle();

        assertEquals(16.5f, p.getHealth(), 1e-4f);
        assertEquals(18,    p.getFood());
        assertEquals(3.2f,  p.getSaturation(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetHealthPacket nmsPacket = new ClientboundSetHealthPacket(20.0f, 20, 5.0f);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetHealthPacket wrapper = new WrappedClientboundSetHealthPacket(container);

        assertEquals(20.0f, wrapper.getHealth(),     1e-4f);
        assertEquals(20,    wrapper.getFood());
        assertEquals(5.0f,  wrapper.getSaturation(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetHealthPacket nmsPacket = new ClientboundSetHealthPacket(20.0f, 20, 5.0f);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetHealthPacket wrapper = new WrappedClientboundSetHealthPacket(container);

        wrapper.setHealth(0.0f);

        assertEquals(0.0f, wrapper.getHealth(),     1e-4f);
        assertEquals(20,   wrapper.getFood());
        assertEquals(5.0f, wrapper.getSaturation(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetHealthPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
