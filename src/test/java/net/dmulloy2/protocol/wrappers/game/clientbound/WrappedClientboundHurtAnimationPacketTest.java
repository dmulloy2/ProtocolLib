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
    void testCreate() {
        WrappedClientboundHurtAnimationPacket w = new WrappedClientboundHurtAnimationPacket();
        w.setEntityId(42);
        w.setYaw(135.0f);

        assertEquals(PacketType.Play.Server.HURT_ANIMATION, w.getHandle().getType());

        ClientboundHurtAnimationPacket p = (ClientboundHurtAnimationPacket) w.getHandle().getHandle();

        assertEquals(42, p.id());
        assertEquals(135.0f, p.yaw(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundHurtAnimationPacket nmsPacket = new ClientboundHurtAnimationPacket(7, 45.0f);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundHurtAnimationPacket wrapper = new WrappedClientboundHurtAnimationPacket(container);

        assertEquals(7, wrapper.getEntityId());
        assertEquals(45.0f, wrapper.getYaw(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundHurtAnimationPacket nmsPacket = new ClientboundHurtAnimationPacket(7, 45.0f);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundHurtAnimationPacket wrapper = new WrappedClientboundHurtAnimationPacket(container);

        wrapper.setYaw(270.0f);

        assertEquals(7, wrapper.getEntityId());
        assertEquals(270.0f, wrapper.getYaw(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundHurtAnimationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
