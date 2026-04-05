package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundProjectilePowerPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundProjectilePowerPacket w = new WrappedClientboundProjectilePowerPacket(3, 100.0);

        assertEquals(PacketType.Play.Server.PROJECTILE_POWER, w.getHandle().getType());

        ClientboundProjectilePowerPacket p = (ClientboundProjectilePowerPacket) w.getHandle().getHandle();

        assertEquals(3, p.getId());
        assertEquals(100.0, p.getAccelerationPower(), 1e-9);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundProjectilePowerPacket w = new WrappedClientboundProjectilePowerPacket();

        assertEquals(PacketType.Play.Server.PROJECTILE_POWER, w.getHandle().getType());

        ClientboundProjectilePowerPacket p = (ClientboundProjectilePowerPacket) w.getHandle().getHandle();

        assertEquals(0, p.getId());
        assertEquals(0.0, p.getAccelerationPower(), 1e-9);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundProjectilePowerPacket nmsPacket = new ClientboundProjectilePowerPacket(3, 100.0);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundProjectilePowerPacket wrapper = new WrappedClientboundProjectilePowerPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals(100.0, wrapper.getAccelerationPower(), 1e-9);

        wrapper.setId(9);
        wrapper.setAccelerationPower(-5.0);

        assertEquals(9, nmsPacket.getId());
        assertEquals(-5.0, nmsPacket.getAccelerationPower(), 1e-9);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundProjectilePowerPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
