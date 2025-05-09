package com.comphenix.protocol.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinecraftRegistryAccessTest {

    @Test
    public void testGet() {
        assertNotNull(MinecraftRegistryAccess.get());
    }
}
