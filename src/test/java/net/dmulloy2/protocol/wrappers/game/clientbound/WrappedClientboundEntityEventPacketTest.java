package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundEntityEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundEntityEventPacket w = new WrappedClientboundEntityEventPacket();
        w.setEntityId(15);
        w.setStatus((byte) 2);

        assertEquals(PacketType.Play.Server.ENTITY_STATUS, w.getHandle().getType());

        ClientboundEntityEventPacket p = (ClientboundEntityEventPacket) w.getHandle().getHandle();

        assertEquals((byte) 2, p.getEventId());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 42);
        container.getBytes().write(0, (byte) 3);

        WrappedClientboundEntityEventPacket wrapper = new WrappedClientboundEntityEventPacket(container);

        assertEquals(42, wrapper.getEntityId());
        assertEquals((byte) 3, wrapper.getStatus());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 42);
        container.getBytes().write(0, (byte) 3);

        WrappedClientboundEntityEventPacket wrapper = new WrappedClientboundEntityEventPacket(container);
        wrapper.setStatus((byte) 7);

        assertEquals(42, wrapper.getEntityId());
        assertEquals((byte) 7, wrapper.getStatus());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundEntityEventPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
