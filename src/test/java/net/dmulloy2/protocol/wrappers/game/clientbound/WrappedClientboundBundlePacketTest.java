package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBundlePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        PacketContainer systemChat = systemChat("Bundled");
        WrappedClientboundBundlePacket wrapper = new WrappedClientboundBundlePacket(List.of(systemChat));

        assertEquals(PacketType.Play.Server.BUNDLE, wrapper.getHandle().getType());
        List<PacketContainer> subPackets = toList(wrapper.getSubPackets());
        assertEquals(1, subPackets.size());
        assertEquals(PacketType.Play.Server.SYSTEM_CHAT, subPackets.get(0).getType());
        assertEquals(WrappedChatComponent.fromText("Bundled"), subPackets.get(0).getChatComponents().read(0));
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBundlePacket wrapper = new WrappedClientboundBundlePacket();

        assertEquals(PacketType.Play.Server.BUNDLE, wrapper.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundBundlePacket source = new WrappedClientboundBundlePacket(List.of(systemChat("Original")));
        PacketContainer container = PacketContainer.fromPacket(source.getHandle().getHandle());
        WrappedClientboundBundlePacket wrapper = new WrappedClientboundBundlePacket(container);

        assertEquals(WrappedChatComponent.fromText("Original"), toList(wrapper.getSubPackets()).get(0).getChatComponents().read(0));

        wrapper.setSubPackets(List.of(systemChat("Modified")));

        List<PacketContainer> subPackets = toList(wrapper.getSubPackets());
        assertEquals(1, subPackets.size());
        assertEquals(WrappedChatComponent.fromText("Modified"), subPackets.get(0).getChatComponents().read(0));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBundlePacket(new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }

    private static PacketContainer systemChat(String message) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
        packet.getBooleans().write(0, false);
        return packet;
    }

    private static List<PacketContainer> toList(Iterable<PacketContainer> packets) {
        List<PacketContainer> result = new ArrayList<>();
        packets.forEach(result::add);
        return result;
    }
}
