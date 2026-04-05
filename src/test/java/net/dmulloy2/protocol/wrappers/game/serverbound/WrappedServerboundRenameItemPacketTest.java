package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundRenameItemPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundRenameItemPacket w = new WrappedServerboundRenameItemPacket("hello");

        assertEquals(PacketType.Play.Client.ITEM_NAME, w.getHandle().getType());

        ServerboundRenameItemPacket p = (ServerboundRenameItemPacket) w.getHandle().getHandle();

        assertEquals("hello", p.getName());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundRenameItemPacket w = new WrappedServerboundRenameItemPacket();

        assertEquals(PacketType.Play.Client.ITEM_NAME, w.getHandle().getType());

        ServerboundRenameItemPacket p = (ServerboundRenameItemPacket) w.getHandle().getHandle();


    }

    @Test
    void testModifyExistingPacket() {
        ServerboundRenameItemPacket nmsPacket = new ServerboundRenameItemPacket("hello");
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundRenameItemPacket wrapper = new WrappedServerboundRenameItemPacket(container);

        assertEquals("hello", wrapper.getName());

        wrapper.setName("modified");

        assertEquals("modified", nmsPacket.getName());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundRenameItemPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
