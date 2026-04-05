package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEquipmentPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetEquipmentPacket w = new WrappedClientboundSetEquipmentPacket(3);

        assertEquals(PacketType.Play.Server.ENTITY_EQUIPMENT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetEquipmentPacket w = new WrappedClientboundSetEquipmentPacket();

        assertEquals(PacketType.Play.Server.ENTITY_EQUIPMENT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetEquipmentPacket source = new WrappedClientboundSetEquipmentPacket(3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetEquipmentPacket wrapper = new WrappedClientboundSetEquipmentPacket(container);

        assertEquals(3, wrapper.getEntityId());

        wrapper.setEntityId(9);

        assertEquals(9, wrapper.getEntityId());

        assertEquals(9, source.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEquipmentPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
