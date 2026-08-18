package com.comphenix.protocol.async;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.scheduler.ProtocolScheduler;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AsyncFilterManagerTest {

    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testSendingTypesUseServerProcessingQueue() {
        AsyncFilterManager manager = new AsyncFilterManager(
                mock(ErrorReporter.class), mock(ProtocolScheduler.class));
        manager.setManager(mock(ProtocolManager.class));
        manager.registerAsyncHandler(new PacketAdapter(
                mock(Plugin.class), PacketType.Play.Server.SYSTEM_CHAT) { }, false);

        assertTrue(manager.getSendingTypes().contains(PacketType.Play.Server.SYSTEM_CHAT));
        assertFalse(manager.getReceivingTypes().contains(PacketType.Play.Server.SYSTEM_CHAT));
    }
}
