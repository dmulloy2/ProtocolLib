package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEntityLinkPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetEntityLinkPacket w = new WrappedClientboundSetEntityLinkPacket();
        w.setAttachedEntityId(5);
        w.setHoldingEntityId(10);

        assertEquals(PacketType.Play.Server.ATTACH_ENTITY, w.getHandle().getType());

        ClientboundSetEntityLinkPacket p = (ClientboundSetEntityLinkPacket) w.getHandle().getHandle();

        assertEquals(5, p.getSourceId());
        assertEquals(10, p.getDestId());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 11);
        container.getIntegers().write(1, 22);

        WrappedClientboundSetEntityLinkPacket wrapper = new WrappedClientboundSetEntityLinkPacket(container);

        assertEquals(11, wrapper.getAttachedEntityId());
        assertEquals(22, wrapper.getHoldingEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 1);
        container.getIntegers().write(1, 2);

        WrappedClientboundSetEntityLinkPacket wrapper = new WrappedClientboundSetEntityLinkPacket(container);
        wrapper.setAttachedEntityId(99);

        assertEquals(99, wrapper.getAttachedEntityId());
        assertEquals(2, wrapper.getHoldingEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEntityLinkPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
