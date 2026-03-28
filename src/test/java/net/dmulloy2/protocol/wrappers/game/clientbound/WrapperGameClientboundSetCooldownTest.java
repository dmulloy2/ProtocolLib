package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetCooldownTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetCooldown w = new WrapperGameClientboundSetCooldown();
        MinecraftKey key = new MinecraftKey("minecraft", "ender_pearl");
        w.setItem(key);
        w.setTicks(20);
        assertEquals(key, w.getItem());
        assertEquals(20, w.getTicks());
        assertEquals(PacketType.Play.Server.SET_COOLDOWN, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_COOLDOWN);
        raw.getModifier().writeDefaults();
        raw.getMinecraftKeys().write(0, new MinecraftKey("minecraft", "bow"));
        raw.getIntegers().write(0, 5);

        WrapperGameClientboundSetCooldown w = new WrapperGameClientboundSetCooldown(raw);
        assertEquals(new MinecraftKey("minecraft", "bow"), w.getItem());
        assertEquals(5, w.getTicks());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetCooldown w = new WrapperGameClientboundSetCooldown();
        w.setTicks(10);

        new WrapperGameClientboundSetCooldown(w.getHandle()).setTicks(40);

        assertEquals(40, w.getTicks());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetCooldown(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
