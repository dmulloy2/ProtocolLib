package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundAttackPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundAttackPacket w = new WrappedServerboundAttackPacket();
        w.setEntityId(123);

        assertEquals(PacketType.Play.Client.ATTACK, w.getHandle().getType());

        ServerboundAttackPacket p = (ServerboundAttackPacket) w.getHandle().getHandle();

        assertEquals(123, p.entityId());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundAttackPacket nmsPacket = new ServerboundAttackPacket(55);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundAttackPacket wrapper = new WrappedServerboundAttackPacket(container);

        assertEquals(55, wrapper.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundAttackPacket nmsPacket = new ServerboundAttackPacket(55);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundAttackPacket wrapper = new WrappedServerboundAttackPacket(container);

        wrapper.setEntityId(200);

        assertEquals(200, wrapper.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundAttackPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
