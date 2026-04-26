package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Either;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.wrappers.game.clientbound.WrappedClientboundServerLinksPacket.KnownLinkType;
import net.dmulloy2.protocol.wrappers.game.clientbound.WrappedClientboundServerLinksPacket.WrappedUntrustedEntry;
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
        assertEquals(PacketType.Play.Server.SERVER_LINKS, w.getHandle().getType());
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
                Either.left(KnownLinkType.BUG_REPORT),
                "https://example.com/report");
        w.setLinks(List.of(entry));

        List<WrappedUntrustedEntry> links = w.getLinks();
        assertEquals(1, links.size());
        WrappedUntrustedEntry roundTripped = links.get(0);
        assertEquals("https://example.com/report", roundTripped.link);
        assertTrue(roundTripped.type.left().isPresent());
        assertEquals(KnownLinkType.BUG_REPORT, roundTripped.type.left().get());
        assertFalse(roundTripped.type.right().isPresent());
    }

    @Test
    void testSetAndGetLinksCustomComponent() {
        WrappedClientboundServerLinksPacket w = new WrappedClientboundServerLinksPacket();
        WrappedChatComponent label = WrappedChatComponent.fromText("My Site");
        WrappedUntrustedEntry entry = new WrappedUntrustedEntry(
                Either.right(label),
                "https://example.com");
        w.setLinks(List.of(entry));

        List<WrappedUntrustedEntry> links = w.getLinks();
        assertEquals(1, links.size());
        WrappedUntrustedEntry roundTripped = links.get(0);
        assertEquals("https://example.com", roundTripped.link);
        assertTrue(roundTripped.type.right().isPresent());
        assertEquals(label, roundTripped.type.right().get());
        assertFalse(roundTripped.type.left().isPresent());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundServerLinksPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
