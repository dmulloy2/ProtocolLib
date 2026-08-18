package com.comphenix.protocol.concurrency;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConcurrentPlayerMapTest {

    @Test
    void testPutIfAbsentWithUnavailableAddress() {
        ConcurrentPlayerMap<Object> map = ConcurrentPlayerMap.usingAddress();
        Player player = mock(Player.class);

        assertThrows(IllegalStateException.class, () -> map.putIfAbsent(player, new Object()));
        assertTrue(map.isEmpty());
    }
}
