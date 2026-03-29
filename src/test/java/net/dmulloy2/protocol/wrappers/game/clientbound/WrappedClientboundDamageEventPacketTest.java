package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
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
    void testCreate() {
        WrappedClientboundDamageEventPacket w = new WrappedClientboundDamageEventPacket();
        w.setEntityId(10);
        w.setSourceCauseId(20);
        w.setSourceDirectId(30);
        w.setSourcePosition(Optional.of(new Vector(1.0, 64.0, -3.0)));

        assertEquals(PacketType.Play.Server.DAMAGE_EVENT, w.getHandle().getType());

        ClientboundDamageEventPacket p = (ClientboundDamageEventPacket) w.getHandle().getHandle();

        assertEquals(10, p.entityId());
        assertEquals(20, p.sourceCauseId());
        assertEquals(30, p.sourceDirectId());
        assertTrue(p.sourcePosition().isPresent());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedClientboundDamageEventPacket src = new WrappedClientboundDamageEventPacket();
        src.setEntityId(5);
        src.setSourceCauseId(0);
        src.setSourceDirectId(0);
        src.setSourcePosition(Optional.empty());

        PacketContainer container = src.getHandle();
        WrappedClientboundDamageEventPacket wrapper = new WrappedClientboundDamageEventPacket(container);

        assertEquals(5, wrapper.getEntityId());
        assertEquals(0, wrapper.getSourceCauseId());
        assertEquals(0, wrapper.getSourceDirectId());
        assertFalse(wrapper.getSourcePosition().isPresent());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundDamageEventPacket w = new WrappedClientboundDamageEventPacket();
        w.setEntityId(1);
        w.setSourceCauseId(0);
        w.setSourceDirectId(0);
        w.setSourcePosition(Optional.empty());

        w.setEntityId(42);
        w.setSourcePosition(Optional.of(new Vector(5.0, 70.0, 5.0)));

        assertEquals(42, w.getEntityId());
        assertTrue(w.getSourcePosition().isPresent());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDamageEventPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
