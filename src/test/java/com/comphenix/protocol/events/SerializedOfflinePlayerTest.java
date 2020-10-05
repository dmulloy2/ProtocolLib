package com.comphenix.protocol.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SerializedOfflinePlayerTest {

    @Mock
    static OfflinePlayer offlinePlayer;

    private static final String name = "playerName";
    private static final UUID uuid = UUID.randomUUID();
    private static final long firstPlayed = 1000L;
    private static final long lastPlayed = firstPlayed + 100L;
    private static final boolean isOp = false;
    private static final boolean playedBefore = true;
    private static final boolean whitelisted = true;

    private static SerializedOfflinePlayer serializedOfflinePlayer;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(offlinePlayer.getName()).thenReturn(name);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        when(offlinePlayer.getFirstPlayed()).thenReturn(firstPlayed);
        when(offlinePlayer.getLastPlayed()).thenReturn(lastPlayed);
        when(offlinePlayer.isOp()).thenReturn(isOp);
        when(offlinePlayer.hasPlayedBefore()).thenReturn(playedBefore);
        when(offlinePlayer.isWhitelisted()).thenReturn(whitelisted);

        serializedOfflinePlayer = new SerializedOfflinePlayer(offlinePlayer);
    }

    @Test
    public void getProxyPlayer() {
        Player player = serializedOfflinePlayer.getProxyPlayer();
        Assert.assertNotNull(player);

        // getDisplayName only works for online players.
        assertThrows(UnsupportedOperationException.class, player::getDisplayName);

        assertEquals(uuid, serializedOfflinePlayer.getUniqueId());
        assertEquals(name, serializedOfflinePlayer.getName());
        assertEquals(firstPlayed, serializedOfflinePlayer.getFirstPlayed());
        assertEquals(lastPlayed, serializedOfflinePlayer.getLastPlayed());
        assertEquals(isOp, serializedOfflinePlayer.isOp());
        assertEquals(playedBefore, serializedOfflinePlayer.hasPlayedBefore());
        assertEquals(whitelisted, serializedOfflinePlayer.isWhitelisted());
    }
}
