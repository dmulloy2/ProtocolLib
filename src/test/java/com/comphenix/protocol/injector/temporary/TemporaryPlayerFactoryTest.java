package com.comphenix.protocol.injector.temporary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TemporaryPlayerFactoryTest {

	private static final TemporaryPlayerFactory temporaryPlayerFactory = new TemporaryPlayerFactory();

	@Mock
	Server server;
	@Mock
	MinimalInjector minimalInjector;

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testUnavailableSocketInjector() {
		Player player = temporaryPlayerFactory.createTemporaryPlayer(this.server);
		assertThrows(IllegalStateException.class, player::getPlayer);
	}

	@Test
	public void createTemporaryPlayer() {

		Player player = temporaryPlayerFactory.createTemporaryPlayer(this.server, this.minimalInjector);
		assertEquals(this.server, player.getServer());

		// May seem dumb, but this makes sure that the .equals method is still instact.
		assertEquals(player, player);
	}
}
