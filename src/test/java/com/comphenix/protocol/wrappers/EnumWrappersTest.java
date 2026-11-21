package com.comphenix.protocol.wrappers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumWrappersTest {

    private static final Set<String> KNOWN_INVALID = Sets.newHashSet(
            "Particle", "WorldBorderAction", "CombatEventType", "TitleAction", "ChatType", "TitleAction",
            "ScoreboardAction", "EntityUseAction"
    );

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
        EnumWrappers.getPlayerInfoActionClass(); // just to initialize the classes and converters
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validateAllEnumFieldsAreWrapped() {
        Map<Class<?>, EquivalentConverter<?>> nativeEnums = EnumWrappers.getFromNativeMap();
        for (Entry<Class<?>, EquivalentConverter<?>> entry : nativeEnums.entrySet()) {
            for (Object nativeConstant : entry.getKey().getEnumConstants()) {
                try {
                    // yay, generics
                    EquivalentConverter<Object> converter = (EquivalentConverter<Object>) entry.getValue();

                    // try to convert the native constant to a wrapper and back
                    Object wrappedValue = converter.getSpecific(nativeConstant);
                    assertNotNull(wrappedValue);

                    Object unwrappedValue = converter.getGeneric(wrappedValue);
                    assertNotNull(unwrappedValue);

                    assertEquals(nativeConstant, unwrappedValue);
                } catch (Exception ex) {
                    fail(ex);
                    // ex.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testValidity() {
        assertEquals(EnumWrappers.INVALID, KNOWN_INVALID);
    }

    @Test
    public void testConvertersAreShared() {
        // a new converter per call discarded its lookup cache for every packet read
        assertSame(EnumWrappers.getPlayerDiggingActionConverter(), EnumWrappers.getPlayerDiggingActionConverter());
        assertSame(EnumWrappers.getEntityActionConverter(), EnumWrappers.getEntityActionConverter());
        assertSame(EnumWrappers.getGameModeConverter(), EnumWrappers.getGameModeConverter());
        assertSame(EnumWrappers.getDifficultyConverter(), EnumWrappers.getDifficultyConverter());
    }

    @Test
    public void testSharedAliasedConverterRoundTrip() {
        EquivalentConverter<EnumWrappers.PlayerDigType> converter = EnumWrappers.getPlayerDiggingActionConverter();
        for (Object nativeConstant : EnumWrappers.getPlayerDigTypeClass().getEnumConstants()) {
            EnumWrappers.PlayerDigType wrapped = converter.getSpecific(nativeConstant);
            assertSame(nativeConstant, converter.getGeneric(wrapped));
            // the second lookup is answered from the shared converter's cache
            assertSame(wrapped, converter.getSpecific(nativeConstant));
        }
    }
}
