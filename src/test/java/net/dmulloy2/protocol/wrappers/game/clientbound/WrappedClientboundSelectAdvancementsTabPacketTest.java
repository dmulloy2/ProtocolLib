package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSelectAdvancementsTabPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSelectAdvancementsTabPacket w = new WrappedClientboundSelectAdvancementsTabPacket(new MinecraftKey("minecraft", "stone"));

        assertEquals(PacketType.Play.Server.SELECT_ADVANCEMENT_TAB, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getTab());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSelectAdvancementsTabPacket w = new WrappedClientboundSelectAdvancementsTabPacket();

        assertEquals(PacketType.Play.Server.SELECT_ADVANCEMENT_TAB, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSelectAdvancementsTabPacket source = new WrappedClientboundSelectAdvancementsTabPacket(new MinecraftKey("minecraft", "stone"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSelectAdvancementsTabPacket wrapper = new WrappedClientboundSelectAdvancementsTabPacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getTab());

        wrapper.setTab(new MinecraftKey("minecraft", "sand"));

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getTab());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getTab());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSelectAdvancementsTabPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
