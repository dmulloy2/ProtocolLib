package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTagQueryPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundTagQueryPacket w = new WrappedClientboundTagQueryPacket();
        w.setTransactionId(42);

        assertEquals(PacketType.Play.Server.NBT_QUERY, w.getHandle().getType());

        ClientboundTagQueryPacket p = (ClientboundTagQueryPacket) w.getHandle().getHandle();

        assertEquals(42, p.getTransactionId());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundTagQueryPacket nmsPacket = new ClientboundTagQueryPacket(99, new CompoundTag());

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTagQueryPacket wrapper = new WrappedClientboundTagQueryPacket(container);

        assertEquals(99, wrapper.getTransactionId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTagQueryPacket nmsPacket = new ClientboundTagQueryPacket(1, new CompoundTag());

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTagQueryPacket wrapper = new WrappedClientboundTagQueryPacket(container);

        wrapper.setTransactionId(7);

        assertEquals(7, wrapper.getTransactionId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTagQueryPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
