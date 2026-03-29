package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundOpenScreenPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundOpenScreenPacket w = new WrappedClientboundOpenScreenPacket();
        w.setContainerId(3);
        w.setTitle(WrappedChatComponent.fromText("My Chest"));

        assertEquals(PacketType.Play.Server.OPEN_WINDOW, w.getHandle().getType());

        ClientboundOpenScreenPacket p = (ClientboundOpenScreenPacket) w.getHandle().getHandle();

        assertEquals(3, p.getContainerId());
        assertNotNull(p.getTitle());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedClientboundOpenScreenPacket src = new WrappedClientboundOpenScreenPacket();
        src.setContainerId(7);
        src.setTitle(WrappedChatComponent.fromText("Shop"));

        PacketContainer container = src.getHandle();
        WrappedClientboundOpenScreenPacket wrapper = new WrappedClientboundOpenScreenPacket(container);

        assertEquals(7, wrapper.getContainerId());
        assertNotNull(wrapper.getTitle());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundOpenScreenPacket w = new WrappedClientboundOpenScreenPacket();
        w.setContainerId(1);
        w.setTitle(WrappedChatComponent.fromText("Old Title"));

        w.setContainerId(10);
        w.setTitle(WrappedChatComponent.fromText("New Title"));

        assertEquals(10, w.getContainerId());
        assertNotNull(w.getTitle());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundOpenScreenPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
