package com.comphenix.protocol.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DarkMcVersionParsingTest {
    @Test
    void parsesDarkMcPaperVersionString() {
        assertEquals("26.1.2", MinecraftVersion.extractVersion("26.1.2-DEV-168698e (MC: 26.1.2)"));
        assertEquals(MinecraftVersion.v26_1_2, MinecraftVersion.fromServerVersion("26.1.2-DEV-168698e (MC: 26.1.2)"));
    }
}
