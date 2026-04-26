package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Either;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.wrappers.configuration.clientbound.WrappedClientboundServerLinksPacket.KnownLinkType;
import net.dmulloy2.protocol.wrappers.configuration.clientbound.WrappedClientboundServerLinksPacket.WrappedUntrustedEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundServerLinksPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundServerLinksPacket w = new WrappedClientboundServerLinksPacket();
        assertEquals(PacketType.Configuration.Server.SERVER_LINKS, w.getHandle().getType());
    }

    @Test
    void testGetLinksEmptyByDefault() {
        WrappedClientboundServerLinksPacket w = new WrappedClientboundServerLinksPacket();
        assertNotNull(w.getLinks());
    }

    @Test
    void testSetAndGetLinksKnownType() {
        WrappedClientboundServerLinksPacket w = new WrappedClientboundServerLinksPacket();
        WrappedUntrustedEntry entry = new WrappedUntrustedEntry(
                Either.left(KnownLinkType.WEBSITE),
                "https://example.com");
        w.setLinks(List.of(entry));

        List<WrappedUntrustedEntry> result = w.getLinks();
        assertEquals(1, result.size());
        WrappedUntrustedEntry roundTripped = result.get(0);
        assertEquals("https://example.com", roundTripped.link);
        assertTrue(roundTripped.type.left().isPresent());
        assertEquals(KnownLinkType.WEBSITE, roundTripped.type.left().get());
    }

    @Test
    void testSetAndGetLinksCustomComponent() {
        WrappedClientboundServerLinksPacket w = new WrappedClientboundServerLinksPacket();
        WrappedChatComponent label = WrappedChatComponent.fromText("Custom");
        WrappedUntrustedEntry entry = new WrappedUntrustedEntry(
                Either.right(label),
                "https://example.com/custom");
        w.setLinks(List.of(entry));

        List<WrappedUntrustedEntry> result = w.getLinks();
        assertEquals(1, result.size());
        WrappedUntrustedEntry roundTripped = result.get(0);
        assertEquals("https://example.com/custom", roundTripped.link);
        assertTrue(roundTripped.type.right().isPresent());
        assertEquals(label, roundTripped.type.right().get());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundServerLinksPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
