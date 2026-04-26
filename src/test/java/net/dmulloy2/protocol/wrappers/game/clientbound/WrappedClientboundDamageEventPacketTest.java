package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.damage.DamageType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDamageEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundDamageEventPacket w = new WrappedClientboundDamageEventPacket(
                3, DamageType.FALL, 7, 5, Optional.empty());

        assertEquals(PacketType.Play.Server.DAMAGE_EVENT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(DamageType.FALL, w.getSourceType());
        assertEquals(7, w.getSourceCauseId());
        assertEquals(5, w.getSourceDirectId());
        assertEquals(Optional.empty(), w.getSourcePosition());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDamageEventPacket w = new WrappedClientboundDamageEventPacket();

        assertEquals(PacketType.Play.Server.DAMAGE_EVENT, w.getHandle().getType());
        assertEquals(0, w.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        Vector pos = new Vector(1.0, 64.0, -3.0);
        WrappedClientboundDamageEventPacket source = new WrappedClientboundDamageEventPacket(
                3, DamageType.FALL, 7, 5, Optional.of(pos));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDamageEventPacket wrapper = new WrappedClientboundDamageEventPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(DamageType.FALL, wrapper.getSourceType());
        assertEquals(7, wrapper.getSourceCauseId());
        assertEquals(5, wrapper.getSourceDirectId());
        assertTrue(wrapper.getSourcePosition().isPresent());

        wrapper.setEntityId(9);
        wrapper.setSourceType(DamageType.GENERIC);
        wrapper.setSourceCauseId(-1);
        wrapper.setSourceDirectId(0);
        wrapper.setSourcePosition(Optional.empty());

        assertEquals(9, wrapper.getEntityId());
        assertEquals(DamageType.GENERIC, wrapper.getSourceType());
        assertEquals(-1, wrapper.getSourceCauseId());
        assertEquals(0, wrapper.getSourceDirectId());
        assertEquals(Optional.empty(), wrapper.getSourcePosition());

        // Verify write-through to the original wrapper
        assertEquals(9, source.getEntityId());
        assertEquals(DamageType.GENERIC, source.getSourceType());
        assertEquals(-1, source.getSourceCauseId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDamageEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
