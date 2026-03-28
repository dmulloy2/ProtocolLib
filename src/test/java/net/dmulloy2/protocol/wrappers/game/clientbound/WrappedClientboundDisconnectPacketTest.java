package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDisconnectPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundDisconnectPacket w = new WrappedClientboundDisconnectPacket();
        w.setReason(WrappedChatComponent.fromText("You were kicked"));

        assertEquals(PacketType.Play.Server.KICK_DISCONNECT, w.getHandle().getType());

        ClientboundDisconnectPacket p = (ClientboundDisconnectPacket) w.getHandle().getHandle();

        assertNotNull(p.reason());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundDisconnectPacket nmsPacket = new ClientboundDisconnectPacket(
                Component.literal("Banned")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDisconnectPacket wrapper = new WrappedClientboundDisconnectPacket(container);

        assertTrue(wrapper.getReason().getJson().contains("Banned"));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundDisconnectPacket nmsPacket = new ClientboundDisconnectPacket(
                Component.literal("old")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDisconnectPacket wrapper = new WrappedClientboundDisconnectPacket(container);

        wrapper.setReason(WrappedChatComponent.fromText("new reason"));

        assertTrue(wrapper.getReason().getJson().contains("new reason"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDisconnectPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
