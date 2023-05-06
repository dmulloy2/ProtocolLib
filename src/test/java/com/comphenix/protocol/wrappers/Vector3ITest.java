package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Lukas Alt
 * @since 06.05.2023
 */
class Vector3ITest {

    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void test() {
        Vector3I vector3I = Vector3I.newInstance(1, 2, 3);
        assertEquals(1, vector3I.getX());
        assertEquals(2, vector3I.getY());
        assertEquals(3, vector3I.getZ());
        vector3I.setX(4);
        vector3I.setY(5);
        vector3I.setZ(6);
        assertEquals(4, vector3I.getX());
        assertEquals(5, vector3I.getY());
        assertEquals(6, vector3I.getZ());
        Object generic = Vector3I.getConverter().getGeneric(vector3I);
        assertEquals(vector3I, Vector3I.getConverter().getSpecific(generic));
    }
}