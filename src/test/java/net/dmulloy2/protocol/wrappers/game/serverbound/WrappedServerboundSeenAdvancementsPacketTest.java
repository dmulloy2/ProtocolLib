package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundSeenAdvancementsPacket.Action.*;

class WrappedServerboundSeenAdvancementsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        MinecraftKey tab = new MinecraftKey("minecraft", "story/root");
        WrappedServerboundSeenAdvancementsPacket w = new WrappedServerboundSeenAdvancementsPacket(OPENED_TAB, tab);

        assertEquals(PacketType.Play.Client.ADVANCEMENTS, w.getHandle().getType());

        assertEquals(OPENED_TAB, w.getAction());
        assertEquals(tab, w.getTab());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSeenAdvancementsPacket w = new WrappedServerboundSeenAdvancementsPacket();

        assertEquals(PacketType.Play.Client.ADVANCEMENTS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        MinecraftKey tab = new MinecraftKey("minecraft", "story/root");
        WrappedServerboundSeenAdvancementsPacket source = new WrappedServerboundSeenAdvancementsPacket(OPENED_TAB, tab);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSeenAdvancementsPacket wrapper = new WrappedServerboundSeenAdvancementsPacket(container);

        assertEquals(OPENED_TAB, wrapper.getAction());
        assertEquals(tab, wrapper.getTab());

        MinecraftKey tab2 = new MinecraftKey("minecraft", "nether/root");
        wrapper.setAction(CLOSED_SCREEN);
        wrapper.setTab(tab2);

        assertEquals(CLOSED_SCREEN, wrapper.getAction());
        assertEquals(tab2, wrapper.getTab());

        assertEquals(CLOSED_SCREEN, source.getAction());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSeenAdvancementsPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
