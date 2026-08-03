package com.comphenix.protocol.injector.temporary;

import java.util.UUID;

import com.comphenix.protocol.injector.netty.Injector;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class TemporaryPlayerFactoryTest {

    @Mock
    Server server;

    @Mock
    Injector injector;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUnavailableSocketInjector() {
        Player player = TemporaryPlayerFactory.createTemporaryPlayer();
        assertThrows(IllegalStateException.class, player::getPlayer);
    }

    @Test
    public void testLoginProfile() {
        Player player = TemporaryPlayerFactory.createTemporaryPlayer();
        UUID uniqueId = UUID.randomUUID();

        TemporaryPlayerFactory.setInjectorForPlayer(player, this.injector);
        when(this.injector.getPlayer()).thenReturn(player);
        when(this.injector.getPlayerName()).thenReturn("dmulloy2");
        when(this.injector.getPlayerUniqueId()).thenReturn(uniqueId);

        assertEquals("dmulloy2", player.getName());
        assertEquals(uniqueId, player.getUniqueId());
    }
}
