package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetCarriedItemPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetCarriedItemPacket w = new WrappedServerboundSetCarriedItemPacket(3);

        assertEquals(PacketType.Play.Client.HELD_ITEM_SLOT, w.getHandle().getType());

        ServerboundSetCarriedItemPacket p = (ServerboundSetCarriedItemPacket) w.getHandle().getHandle();

        assertEquals(3, p.getSlot());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetCarriedItemPacket w = new WrappedServerboundSetCarriedItemPacket();

        assertEquals(PacketType.Play.Client.HELD_ITEM_SLOT, w.getHandle().getType());

        ServerboundSetCarriedItemPacket p = (ServerboundSetCarriedItemPacket) w.getHandle().getHandle();

        assertEquals(0, p.getSlot());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSetCarriedItemPacket nmsPacket = new ServerboundSetCarriedItemPacket(3);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCarriedItemPacket wrapper = new WrappedServerboundSetCarriedItemPacket(container);

        assertEquals(3, wrapper.getSlot());

        wrapper.setSlot(7);

        assertEquals(7, nmsPacket.getSlot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCarriedItemPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
