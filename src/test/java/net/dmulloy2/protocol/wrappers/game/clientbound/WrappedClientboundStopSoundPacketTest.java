package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundStopSoundPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundStopSoundPacket w = new WrappedClientboundStopSoundPacket();
        w.setName(new MinecraftKey("minecraft", "block.note_block.harp"));
        w.setSource(EnumWrappers.SoundCategory.RECORDS);

        assertEquals(PacketType.Play.Server.STOP_SOUND, w.getHandle().getType());

        ClientboundStopSoundPacket p = (ClientboundStopSoundPacket) w.getHandle().getHandle();

        assertNotNull(p.getName());
        assertNotNull(p.getSource());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundStopSoundPacket nmsPacket = new ClientboundStopSoundPacket(
                Identifier.fromNamespaceAndPath("minecraft", "block.note_block.bass"),
                SoundSource.MUSIC
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundStopSoundPacket wrapper = new WrappedClientboundStopSoundPacket(container);

        assertNotNull(wrapper.getName());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, wrapper.getSource());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundStopSoundPacket nmsPacket = new ClientboundStopSoundPacket(
                Identifier.fromNamespaceAndPath("minecraft", "entity.player.hurt"),
                SoundSource.PLAYERS
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundStopSoundPacket wrapper = new WrappedClientboundStopSoundPacket(container);

        wrapper.setSource(EnumWrappers.SoundCategory.AMBIENT);

        assertEquals(EnumWrappers.SoundCategory.AMBIENT, wrapper.getSource());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundStopSoundPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
