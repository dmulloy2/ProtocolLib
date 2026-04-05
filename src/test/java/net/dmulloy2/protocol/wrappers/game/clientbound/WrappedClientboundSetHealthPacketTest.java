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
    void testAllArgsCreate() {
        WrappedClientboundSetHealthPacket w = new WrappedClientboundSetHealthPacket(0.75f, 7, -3.0f);

        assertEquals(PacketType.Play.Server.UPDATE_HEALTH, w.getHandle().getType());

        ClientboundSetHealthPacket p = (ClientboundSetHealthPacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.getHealth(), 1e-4f);
        assertEquals(7, p.getFood());
        assertEquals(-3.0f, p.getSaturation(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetHealthPacket w = new WrappedClientboundSetHealthPacket();

        assertEquals(PacketType.Play.Server.UPDATE_HEALTH, w.getHandle().getType());

        ClientboundSetHealthPacket p = (ClientboundSetHealthPacket) w.getHandle().getHandle();

        assertEquals(0.0f, p.getHealth(), 1e-4f);
        assertEquals(0, p.getFood());
        assertEquals(0.0f, p.getSaturation(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetHealthPacket nmsPacket = new ClientboundSetHealthPacket(0.75f, 7, -3.0f);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetHealthPacket wrapper = new WrappedClientboundSetHealthPacket(container);

        assertEquals(0.75f, wrapper.getHealth(), 1e-4f);
        assertEquals(7, wrapper.getFood());
        assertEquals(-3.0f, wrapper.getSaturation(), 1e-4f);

        wrapper.setHealth(0.25f);
        wrapper.setFood(-5);
        wrapper.setSaturation(1.0f);

        assertEquals(0.25f, nmsPacket.getHealth(), 1e-4f);
        assertEquals(-5, nmsPacket.getFood());
        assertEquals(1.0f, nmsPacket.getSaturation(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetHealthPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
