package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundClientCommandPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundClientCommandPacket w = new WrappedServerboundClientCommandPacket(EnumWrappers.ClientCommand.PERFORM_RESPAWN);

        assertEquals(PacketType.Play.Client.CLIENT_COMMAND, w.getHandle().getType());

        assertEquals(EnumWrappers.ClientCommand.PERFORM_RESPAWN, w.getAction());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundClientCommandPacket w = new WrappedServerboundClientCommandPacket();

        assertEquals(PacketType.Play.Client.CLIENT_COMMAND, w.getHandle().getType());

        assertNotNull(w.getAction());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundClientCommandPacket src = new WrappedServerboundClientCommandPacket(EnumWrappers.ClientCommand.PERFORM_RESPAWN);
        ServerboundClientCommandPacket nmsPacket = (ServerboundClientCommandPacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundClientCommandPacket wrapper = new WrappedServerboundClientCommandPacket(container);

        assertEquals(EnumWrappers.ClientCommand.PERFORM_RESPAWN, wrapper.getAction());

        wrapper.setAction(EnumWrappers.ClientCommand.REQUEST_GAMERULE_VALUES);

        assertEquals(EnumWrappers.ClientCommand.REQUEST_GAMERULE_VALUES, wrapper.getAction());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientCommandPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
