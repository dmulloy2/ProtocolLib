package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedChatTypeBound;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.time.Instant;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerChatPacket.SignedMessageBodyPacked body =
                new WrappedClientboundPlayerChatPacket.SignedMessageBodyPacked(
                        "Hello, world!",
                        Instant.ofEpochSecond(1234),
                        99L,
                        WrappedClientboundPlayerChatPacket.LastSeenMessagesPacked.empty());
        BitSet filterBits = new BitSet();
        filterBits.set(1);
        WrappedClientboundPlayerChatPacket.FilterMask filterMask =
                WrappedClientboundPlayerChatPacket.FilterMask.partiallyFiltered(filterBits);
        WrappedChatTypeBound chatType = chatType("Alex");
        WrappedClientboundPlayerChatPacket w = new WrappedClientboundPlayerChatPacket(
                3,
                7,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                WrappedChatComponent.fromText("Hello, world!"),
                null,
                body,
                filterMask,
                chatType);

        assertEquals(PacketType.Play.Server.CHAT, w.getHandle().getType());

        assertEquals(3, w.getGlobalIndex());
        assertEquals(7, w.getIndex());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), w.getSender());
        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getUnsignedContent());
        assertEquals(null, w.getSignature());
        assertEquals(body, w.getBody());
        assertEquals(filterMask, w.getFilterMask());
        assertEquals(chatType, w.getChatType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerChatPacket w = new WrappedClientboundPlayerChatPacket();

        assertEquals(PacketType.Play.Server.CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerChatPacket source = new WrappedClientboundPlayerChatPacket(3, 7, UUID.fromString("00000000-0000-0000-0000-000000000001"), WrappedChatComponent.fromText("Hello, world!"), null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerChatPacket wrapper = new WrappedClientboundPlayerChatPacket(container);

        assertEquals(3, wrapper.getGlobalIndex());
        assertEquals(7, wrapper.getIndex());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), wrapper.getSender());
        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getUnsignedContent());
        assertEquals(null, wrapper.getSignature());
        assertEquals(new WrappedClientboundPlayerChatPacket.SignedMessageBodyPacked(
                "", Instant.EPOCH, 0L, WrappedClientboundPlayerChatPacket.LastSeenMessagesPacked.empty()), wrapper.getBody());
        assertEquals(WrappedClientboundPlayerChatPacket.FilterMask.passThrough(), wrapper.getFilterMask());
        assertEquals(chatType(""), wrapper.getChatType());

        WrappedClientboundPlayerChatPacket.SignedMessageBodyPacked modifiedBody =
                new WrappedClientboundPlayerChatPacket.SignedMessageBodyPacked(
                        "Modified",
                        Instant.ofEpochSecond(5678),
                        -5L,
                        new WrappedClientboundPlayerChatPacket.LastSeenMessagesPacked(List.of()));
        BitSet modifiedFilterBits = new BitSet();
        modifiedFilterBits.set(0);
        WrappedClientboundPlayerChatPacket.FilterMask modifiedFilterMask =
                WrappedClientboundPlayerChatPacket.FilterMask.partiallyFiltered(modifiedFilterBits);
        WrappedChatTypeBound modifiedChatType = chatType("Modified");
        wrapper.setGlobalIndex(9);
        wrapper.setIndex(-5);
        wrapper.setSender(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        wrapper.setUnsignedContent(WrappedChatComponent.fromText("Modified"));
        wrapper.setSignature(null);
        wrapper.setBody(modifiedBody);
        wrapper.setFilterMask(modifiedFilterMask);
        wrapper.setChatType(modifiedChatType);

        assertEquals(9, wrapper.getGlobalIndex());
        assertEquals(-5, wrapper.getIndex());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), wrapper.getSender());
        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getUnsignedContent());
        assertEquals(null, wrapper.getSignature());
        assertEquals(modifiedBody, wrapper.getBody());
        assertEquals(modifiedFilterMask, wrapper.getFilterMask());
        assertEquals(modifiedChatType, wrapper.getChatType());

        assertEquals(9, source.getGlobalIndex());
        assertEquals(-5, source.getIndex());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), source.getSender());
        assertEquals(WrappedChatComponent.fromText("Modified"), source.getUnsignedContent());
        assertEquals(null, source.getSignature());
        assertEquals(modifiedBody, source.getBody());
        assertEquals(modifiedFilterMask, source.getFilterMask());
        assertEquals(modifiedChatType, source.getChatType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }

    private static WrappedChatTypeBound chatType(String name) {
        return new WrappedChatTypeBound(
                new MinecraftKey("chat"),
                WrappedChatComponent.fromText(name),
                Optional.empty());
    }
}
