package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundHurtAnimationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundHurtAnimationPacket w = new WrappedClientboundHurtAnimationPacket(3, 0.5f);

        assertEquals(PacketType.Play.Server.HURT_ANIMATION, w.getHandle().getType());

        ClientboundHurtAnimationPacket p = (ClientboundHurtAnimationPacket) w.getHandle().getHandle();

        assertEquals(3, p.id());
        assertEquals(0.5f, p.yaw(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundHurtAnimationPacket w = new WrappedClientboundHurtAnimationPacket();

        assertEquals(PacketType.Play.Server.HURT_ANIMATION, w.getHandle().getType());

        ClientboundHurtAnimationPacket p = (ClientboundHurtAnimationPacket) w.getHandle().getHandle();

        assertEquals(0, p.id());
        assertEquals(0.0f, p.yaw(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundHurtAnimationPacket nmsPacket = new ClientboundHurtAnimationPacket(3, 0.5f);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundHurtAnimationPacket wrapper = new WrappedClientboundHurtAnimationPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(0.5f, wrapper.getYaw(), 1e-4f);

        wrapper.setEntityId(9);
        wrapper.setYaw(-3.0f);

        assertEquals(9, nmsPacket.id());
        assertEquals(-3.0f, nmsPacket.yaw(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundHurtAnimationPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
