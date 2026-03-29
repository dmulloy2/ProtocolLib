package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundSpectateEntityPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSpectateEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundSpectateEntityPacket w = new WrappedServerboundSpectateEntityPacket(42);

        assertEquals(PacketType.Play.Client.SPECTATE_ENTITY, w.getHandle().getType());

        ServerboundSpectateEntityPacket p = (ServerboundSpectateEntityPacket) w.getHandle().getHandle();

        assertEquals(42, p.entityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSpectateEntityPacket w = new WrappedServerboundSpectateEntityPacket();

        assertEquals(PacketType.Play.Client.SPECTATE_ENTITY, w.getHandle().getType());

        ServerboundSpectateEntityPacket p = (ServerboundSpectateEntityPacket) w.getHandle().getHandle();

        assertEquals(0, p.entityId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSpectateEntityPacket nmsPacket = new ServerboundSpectateEntityPacket(100);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSpectateEntityPacket wrapper = new WrappedServerboundSpectateEntityPacket(container);

        assertEquals(100, wrapper.getEntityId());

        wrapper.setEntityId(77);

        assertEquals(77, nmsPacket.entityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSpectateEntityPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
