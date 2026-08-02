package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.OptionalInt;
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSpectateEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSpectateEntityPacket w = new WrappedServerboundSpectateEntityPacket(3);

        assertEquals(PacketType.Play.Client.SPECTATE_ENTITY, w.getHandle().getType());

        ServerboundSpectatorActionPacket p =
                (ServerboundSpectatorActionPacket) w.getHandle().getHandle();

        assertEquals(OptionalInt.of(3), p.spectateEntityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSpectateEntityPacket w = new WrappedServerboundSpectateEntityPacket();

        assertEquals(PacketType.Play.Client.SPECTATE_ENTITY, w.getHandle().getType());

        ServerboundSpectatorActionPacket p =
                (ServerboundSpectatorActionPacket) w.getHandle().getHandle();

        assertEquals(OptionalInt.empty(), p.spectateEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSpectatorActionPacket nmsPacket =
                new ServerboundSpectatorActionPacket(OptionalInt.of(3));
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSpectateEntityPacket wrapper = new WrappedServerboundSpectateEntityPacket(container);

        assertEquals(3, wrapper.getEntityId());

        wrapper.setEntityId(9);

        assertEquals(OptionalInt.of(9), nmsPacket.spectateEntityId());

        wrapper.setOptionalEntityId(OptionalInt.empty());
        assertEquals(OptionalInt.empty(), nmsPacket.spectateEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSpectateEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
