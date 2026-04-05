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
    void testAllArgsCreate() {
        WrappedServerboundAttackPacket w = new WrappedServerboundAttackPacket(3);

        assertEquals(PacketType.Play.Client.ATTACK, w.getHandle().getType());

        ServerboundAttackPacket p = (ServerboundAttackPacket) w.getHandle().getHandle();

        assertEquals(3, p.entityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundAttackPacket w = new WrappedServerboundAttackPacket();

        assertEquals(PacketType.Play.Client.ATTACK, w.getHandle().getType());

        ServerboundAttackPacket p = (ServerboundAttackPacket) w.getHandle().getHandle();

        assertEquals(0, p.entityId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundAttackPacket nmsPacket = new ServerboundAttackPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundAttackPacket wrapper = new WrappedServerboundAttackPacket(container);

        assertEquals(3, wrapper.getEntityId());

        wrapper.setEntityId(9);

        assertEquals(9, nmsPacket.entityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundAttackPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
