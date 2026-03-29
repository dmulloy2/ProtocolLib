package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChatCommandPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundChatCommandPacket w = new WrappedServerboundChatCommandPacket("say hello");

        assertEquals(PacketType.Play.Client.CHAT_COMMAND, w.getHandle().getType());

        ServerboundChatCommandPacket p = (ServerboundChatCommandPacket) w.getHandle().getHandle();

        assertEquals("say hello", p.command());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatCommandPacket w = new WrappedServerboundChatCommandPacket();

        assertEquals(PacketType.Play.Client.CHAT_COMMAND, w.getHandle().getType());

        assertEquals("", w.getCommand());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundChatCommandPacket nmsPacket = new ServerboundChatCommandPacket("cmd");

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatCommandPacket wrapper = new WrappedServerboundChatCommandPacket(container);

        assertEquals("cmd", wrapper.getCommand());

        wrapper.setCommand("tp 0 64 0");

        assertEquals("tp 0 64 0", nmsPacket.command());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatCommandPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
