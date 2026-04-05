package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.boss.CraftBossBar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBossEventPacketTest {

    private static final UUID BOSS_ID = UUID.randomUUID();
    private static BossBar bossBar;

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
        ServerBossEvent event = new ServerBossEvent(BOSS_ID, Component.literal("Test Boss"),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        event.setProgress(0.75f);
        bossBar = new CraftBossBar(event);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBossEventPacket w = new WrappedClientboundBossEventPacket();
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBossEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }

    @Test
    void testCreateAddPacket() {
        WrappedClientboundBossEventPacket w = WrappedClientboundBossEventPacket.createAddPacket(bossBar);
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
        assertEquals(BOSS_ID, w.getId());

        ClientboundBossEventPacket nms = (ClientboundBossEventPacket) w.getHandle().getHandle();
        boolean[] called = {false};
        nms.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void add(UUID id, Component name, float progress, BossEvent.BossBarColor color,
                    BossEvent.BossBarOverlay overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                called[0] = true;
                assertEquals(BOSS_ID, id);
                assertEquals("Test Boss", name.getString());
                assertEquals(0.75f, progress);
                assertEquals(BossEvent.BossBarColor.RED, color);
                assertEquals(BossEvent.BossBarOverlay.PROGRESS, overlay);
                assertFalse(darkenScreen);
                assertFalse(playMusic);
                assertFalse(createWorldFog);
            }
        });
        assertTrue(called[0]);
    }

    @Test
    void testCreateRemovePacket() {
        WrappedClientboundBossEventPacket w = WrappedClientboundBossEventPacket.createRemovePacket(BOSS_ID);
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
        assertEquals(BOSS_ID, w.getId());

        ClientboundBossEventPacket nms = (ClientboundBossEventPacket) w.getHandle().getHandle();
        boolean[] called = {false};
        nms.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void remove(UUID id) {
                called[0] = true;
                assertEquals(BOSS_ID, id);
            }
        });
        assertTrue(called[0]);
    }

    @Test
    void testCreateUpdateProgressPacket() {
        WrappedClientboundBossEventPacket w = WrappedClientboundBossEventPacket.createUpdateProgressPacket(bossBar);
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
        assertEquals(BOSS_ID, w.getId());

        ClientboundBossEventPacket nms = (ClientboundBossEventPacket) w.getHandle().getHandle();
        boolean[] called = {false};
        nms.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void updateProgress(UUID id, float progress) {
                called[0] = true;
                assertEquals(BOSS_ID, id);
                assertEquals(0.75f, progress);
            }
        });
        assertTrue(called[0]);
    }

    @Test
    void testCreateUpdateNamePacket() {
        WrappedClientboundBossEventPacket w = WrappedClientboundBossEventPacket.createUpdateNamePacket(bossBar);
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
        assertEquals(BOSS_ID, w.getId());

        ClientboundBossEventPacket nms = (ClientboundBossEventPacket) w.getHandle().getHandle();
        boolean[] called = {false};
        nms.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void updateName(UUID id, Component name) {
                called[0] = true;
                assertEquals(BOSS_ID, id);
                assertEquals("Test Boss", name.getString());
            }
        });
        assertTrue(called[0]);
    }

    @Test
    void testCreateUpdateStylePacket() {
        WrappedClientboundBossEventPacket w = WrappedClientboundBossEventPacket.createUpdateStylePacket(bossBar);
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
        assertEquals(BOSS_ID, w.getId());

        ClientboundBossEventPacket nms = (ClientboundBossEventPacket) w.getHandle().getHandle();
        boolean[] called = {false};
        nms.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void updateStyle(UUID id, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
                called[0] = true;
                assertEquals(BOSS_ID, id);
                assertEquals(BossEvent.BossBarColor.RED, color);
                assertEquals(BossEvent.BossBarOverlay.PROGRESS, overlay);
            }
        });
        assertTrue(called[0]);
    }

    @Test
    void testCreateUpdatePropertiesPacket() {
        ServerBossEvent flagEvent = new ServerBossEvent(BOSS_ID, Component.literal("Test Boss"),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        flagEvent.setDarkenScreen(true);
        flagEvent.setPlayBossMusic(true);
        BossBar flagBar = new CraftBossBar(flagEvent);

        WrappedClientboundBossEventPacket w = WrappedClientboundBossEventPacket.createUpdatePropertiesPacket(flagBar);
        assertEquals(PacketType.Play.Server.BOSS, w.getHandle().getType());
        assertEquals(BOSS_ID, w.getId());

        ClientboundBossEventPacket nms = (ClientboundBossEventPacket) w.getHandle().getHandle();
        boolean[] called = {false};
        nms.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void updateProperties(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                called[0] = true;
                assertEquals(BOSS_ID, id);
                assertTrue(darkenScreen);
                assertTrue(playMusic);
                assertFalse(createWorldFog);
            }
        });
        assertTrue(called[0]);
    }
}
