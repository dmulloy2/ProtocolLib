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
        WrappedServerboundRenameItemPacket w = new WrappedServerboundRenameItemPacket("Diamond Blade");

        assertEquals(PacketType.Play.Client.ITEM_NAME, w.getHandle().getType());

        ServerboundRenameItemPacket p = (ServerboundRenameItemPacket) w.getHandle().getHandle();

        assertEquals("Diamond Blade", p.getName());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundRenameItemPacket w = new WrappedServerboundRenameItemPacket();

        assertEquals(PacketType.Play.Client.ITEM_NAME, w.getHandle().getType());

        assertNotNull(w.getName());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundRenameItemPacket nmsPacket = new ServerboundRenameItemPacket("Old Name");

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundRenameItemPacket wrapper = new WrappedServerboundRenameItemPacket(container);

        assertEquals("Old Name", wrapper.getName());

        wrapper.setName("New Name");

        assertEquals("New Name", nmsPacket.getName());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundRenameItemPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
