package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundAnimatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundAnimatePacket w = new WrappedClientboundAnimatePacket();
        w.setEntityId(7);
        w.setAnimationId(3);

        assertEquals(PacketType.Play.Server.ANIMATION, w.getHandle().getType());

        ClientboundAnimatePacket p = (ClientboundAnimatePacket) w.getHandle().getHandle();

        assertEquals(7, p.getId());
        assertEquals(3, p.getAction());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ANIMATION);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 5);
        container.getIntegers().write(1, 1);

        WrappedClientboundAnimatePacket wrapper = new WrappedClientboundAnimatePacket(container);

        assertEquals(5, wrapper.getEntityId());
        assertEquals(1, wrapper.getAnimationId());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ANIMATION);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 5);
        container.getIntegers().write(1, 1);

        WrappedClientboundAnimatePacket wrapper = new WrappedClientboundAnimatePacket(container);
        wrapper.setAnimationId(4);

        assertEquals(5, wrapper.getEntityId());
        assertEquals(4, wrapper.getAnimationId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAnimatePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
