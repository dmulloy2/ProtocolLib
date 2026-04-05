package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundStopSoundPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundStopSoundPacket w = new WrappedClientboundStopSoundPacket(new MinecraftKey("minecraft", "stone"), EnumWrappers.SoundCategory.MASTER);

        assertEquals(PacketType.Play.Server.STOP_SOUND, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getName());
        assertEquals(EnumWrappers.SoundCategory.MASTER, w.getSource());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundStopSoundPacket w = new WrappedClientboundStopSoundPacket();

        assertEquals(PacketType.Play.Server.STOP_SOUND, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundStopSoundPacket source = new WrappedClientboundStopSoundPacket(new MinecraftKey("minecraft", "stone"), EnumWrappers.SoundCategory.MASTER);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundStopSoundPacket wrapper = new WrappedClientboundStopSoundPacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getName());
        assertEquals(EnumWrappers.SoundCategory.MASTER, wrapper.getSource());

        wrapper.setName(new MinecraftKey("minecraft", "sand"));
        wrapper.setSource(EnumWrappers.SoundCategory.MUSIC);

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getName());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, wrapper.getSource());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getName());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, source.getSource());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundStopSoundPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
