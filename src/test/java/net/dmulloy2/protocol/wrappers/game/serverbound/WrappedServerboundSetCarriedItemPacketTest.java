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
    void testCreate() {
        WrappedServerboundSetCarriedItemPacket w = new WrappedServerboundSetCarriedItemPacket();
        w.setSlot(4);

        assertEquals(PacketType.Play.Client.HELD_ITEM_SLOT, w.getHandle().getType());

        ServerboundSetCarriedItemPacket p = (ServerboundSetCarriedItemPacket) w.getHandle().getHandle();

        assertEquals(4, p.getSlot());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundSetCarriedItemPacket nmsPacket = new ServerboundSetCarriedItemPacket(2);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCarriedItemPacket wrapper = new WrappedServerboundSetCarriedItemPacket(container);

        assertEquals(2, wrapper.getSlot());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSetCarriedItemPacket nmsPacket = new ServerboundSetCarriedItemPacket(2);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCarriedItemPacket wrapper = new WrappedServerboundSetCarriedItemPacket(container);

        wrapper.setSlot(8);

        assertEquals(8, wrapper.getSlot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCarriedItemPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
