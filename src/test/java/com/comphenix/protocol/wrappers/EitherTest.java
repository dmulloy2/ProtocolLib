package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.wrappers.Either.Left;
import com.comphenix.protocol.wrappers.Either.Right;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EitherTest {

    @Test
    void testLeft() {
        Left<String, ?> left = new Left<>("left");

        assertEquals(left.left(), Optional.of("left"));
        assertEquals(left.right(), Optional.empty());

        String map = left.map(l -> l + "left", r -> r + "right");
        assertEquals("leftleft", map);
    }

    @Test
    void testRight() {
        Right<?, String> right = new Right<>("right");

        assertEquals(right.left(), Optional.empty());
        assertEquals(right.right(), Optional.of("right"));

        String map = right.map(l -> l + "left", r -> r + "right");
        assertEquals("rightright", map);
    }
}
