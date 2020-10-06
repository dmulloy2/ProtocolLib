package com.comphenix.protocol.injector.server;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class TemporaryPlayerFactoryTest {

    private static final TemporaryPlayerFactory temporaryPlayerFactory = new TemporaryPlayerFactory();

    @Mock
    Server server;
    @Mock
    SocketInjector socketInjector;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUnavailableSocketInjector()
    {
        Player player = temporaryPlayerFactory.createTemporaryPlayer(server);
        assertThrows(IllegalStateException.class, player::getPlayer);
    }

    @Test
    public void createTemporaryPlayer() {

        Player player = temporaryPlayerFactory.createTemporaryPlayer(server, socketInjector);
        assertEquals(server, player.getServer());

        // May seem dumb, but this makes sure that the .equals method is still instact.
        assertEquals(player, player);
    }
}
