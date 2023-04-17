package com.comphenix.protocol.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SerializedOfflinePlayerTest {

	private static final String name = "playerName";
	private static final long firstPlayed = 1000L;
	private static final long lastPlayed = firstPlayed + 100L;
	private static final boolean isOp = false;
	private static final boolean playedBefore = true;
	private static final boolean whitelisted = true;
	@Mock
	static OfflinePlayer offlinePlayer;
	private static UUID uuid;
	private static SerializedOfflinePlayer serializedOfflinePlayer;

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.openMocks(this);

		uuid = UUID.randomUUID();
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
		assertNotNull(player);

		// getDisplayName only works for online players.
		assertThrows(UnsupportedOperationException.class, player::getDisplayName);

		assertEquals(uuid, player.getUniqueId());
		assertEquals(name, player.getName());
		assertEquals(firstPlayed, player.getFirstPlayed());
		assertEquals(lastPlayed, player.getLastPlayed());
		assertEquals(isOp, player.isOp());
		assertEquals(playedBefore, player.hasPlayedBefore());
		assertEquals(whitelisted, player.isWhitelisted());
	}

	@Test
	public void getSecondProxyPlayer() {
		// Make sure that the proxyPlayer generation doesn't work only once.
		Player player = serializedOfflinePlayer.getProxyPlayer();
		assertNotNull(player);

		assertEquals(uuid, player.getUniqueId());
	}
}
