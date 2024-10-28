package com.comphenix.protocol.injector.temporary;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TemporaryPlayerFactoryTest {

    @Mock
    Server server;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUnavailableSocketInjector() {
        Player player = TemporaryPlayerFactory.createTemporaryPlayer();
        assertThrows(IllegalStateException.class, player::getPlayer);
    }
}
