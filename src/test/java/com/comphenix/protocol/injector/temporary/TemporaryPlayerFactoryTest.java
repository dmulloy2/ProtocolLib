package com.comphenix.protocol.injector.temporary;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
