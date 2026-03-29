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
    void testCreate() {
        WrappedServerboundChatCommandPacket w = new WrappedServerboundChatCommandPacket();
        w.setCommand("tp 0 64 0");

        assertEquals(PacketType.Play.Client.CHAT_COMMAND, w.getHandle().getType());

        ServerboundChatCommandPacket p = (ServerboundChatCommandPacket) w.getHandle().getHandle();

        assertEquals("tp 0 64 0", p.command());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundChatCommandPacket nmsPacket = new ServerboundChatCommandPacket("say hello");

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatCommandPacket wrapper = new WrappedServerboundChatCommandPacket(container);

        assertEquals("say hello", wrapper.getCommand());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundChatCommandPacket nmsPacket = new ServerboundChatCommandPacket("say hello");

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatCommandPacket wrapper = new WrappedServerboundChatCommandPacket(container);

        wrapper.setCommand("gamemode creative");

        assertEquals("gamemode creative", wrapper.getCommand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatCommandPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
