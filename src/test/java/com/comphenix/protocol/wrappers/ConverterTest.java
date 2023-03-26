package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Lukas Alt
 * @since 26.03.2023
 */
public class ConverterTest {
    @Test
    public void testIterableConverter() {
        EquivalentConverter<Iterable<String>> converter = Converters.iterable(Converters.passthrough(String.class), ArrayList::new, ArrayList::new);
        List<String> l = Arrays.asList("a", "b", "c");
        Object generic = converter.getGeneric(l);
        Object specific = converter.getSpecific(generic);
        assertEquals(l, specific);
    }
}
