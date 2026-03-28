package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRotateHeadPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundRotateHeadPacket w = new WrappedClientboundRotateHeadPacket();
        w.setEntityId(8);
        w.setYHeadRot(WrappedClientboundAddEntityPacket.angleToByte(90.0f));

        assertEquals(PacketType.Play.Server.ENTITY_HEAD_ROTATION, w.getHandle().getType());

        ClientboundRotateHeadPacket p = (ClientboundRotateHeadPacket) w.getHandle().getHandle();

        assertNotNull(p);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 20);
        container.getBytes().write(0, (byte) 32);

        WrappedClientboundRotateHeadPacket wrapper = new WrappedClientboundRotateHeadPacket(container);

        assertEquals(20, wrapper.getEntityId());
        assertEquals((byte) 32, wrapper.getYHeadRot());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 20);
        container.getBytes().write(0, (byte) 32);

        WrappedClientboundRotateHeadPacket wrapper = new WrappedClientboundRotateHeadPacket(container);
        wrapper.setEntityId(99);

        assertEquals(99, wrapper.getEntityId());
        assertEquals((byte) 32, wrapper.getYHeadRot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRotateHeadPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
