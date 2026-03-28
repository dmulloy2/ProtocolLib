package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEntityMotionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetEntityMotionPacket w = new WrappedClientboundSetEntityMotionPacket();
        w.setEntityId(7);
        w.setVelocity(new Vector(1.0, 0.5, -0.5));

        assertEquals(PacketType.Play.Server.ENTITY_VELOCITY, w.getHandle().getType());

        ClientboundSetEntityMotionPacket p = (ClientboundSetEntityMotionPacket) w.getHandle().getHandle();

        assertEquals(7, p.id());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetEntityMotionPacket nmsPacket = new ClientboundSetEntityMotionPacket(3, new Vec3(1.0, 0.0, 0.0));

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetEntityMotionPacket wrapper = new WrappedClientboundSetEntityMotionPacket(container);

        assertEquals(3, wrapper.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetEntityMotionPacket nmsPacket = new ClientboundSetEntityMotionPacket(10, new Vec3(0.0, 0.0, 0.0));

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetEntityMotionPacket wrapper = new WrappedClientboundSetEntityMotionPacket(container);

        wrapper.setEntityId(20);

        assertEquals(20, wrapper.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEntityMotionPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
