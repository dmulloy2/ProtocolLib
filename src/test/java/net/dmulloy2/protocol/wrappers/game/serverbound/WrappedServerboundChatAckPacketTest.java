package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChatAckPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundChatAckPacket w = new WrappedServerboundChatAckPacket(3);

        assertEquals(PacketType.Play.Client.CHAT_ACK, w.getHandle().getType());

        ServerboundChatAckPacket p = (ServerboundChatAckPacket) w.getHandle().getHandle();

        assertEquals(3, p.offset());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatAckPacket w = new WrappedServerboundChatAckPacket();

        assertEquals(PacketType.Play.Client.CHAT_ACK, w.getHandle().getType());

        ServerboundChatAckPacket p = (ServerboundChatAckPacket) w.getHandle().getHandle();

        assertEquals(0, p.offset());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundChatAckPacket nmsPacket = new ServerboundChatAckPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatAckPacket wrapper = new WrappedServerboundChatAckPacket(container);

        assertEquals(3, wrapper.getOffset());

        wrapper.setOffset(9);

        assertEquals(9, nmsPacket.offset());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatAckPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
