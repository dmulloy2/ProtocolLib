package com.comphenix.protocol.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
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

    @Test
    public void testGenericConverterDoesNotReplaceAliasedConverter() throws ReflectiveOperationException {
        // simulate a plugin that asks for the plain converter of an aliased enum before the wrappers are initialized
        Field cacheField = EnumWrappers.class.getDeclaredField("CONVERTER_CACHE");
        cacheField.setAccessible(true);
        ((Map<?, ?>) cacheField.get(null)).clear();

        EnumWrappers.getGenericConverter(EnumWrappers.getPlayerDigTypeClass(), EnumWrappers.PlayerDigType.class);
        EnumWrappers.getGenericConverter(EnumWrappers.getPlayerActionClass(), EnumWrappers.PlayerAction.class);

        // SWAP_HELD_ITEMS only exists as SWAP_ITEM_WITH_OFFHAND in the native enum
        assertEquals(1, assertAliasesResolve(EnumWrappers.getPlayerDiggingActionConverter(),
                EnumWrappers.getPlayerDigTypeClass(), EnumWrappers.PlayerDigType.values()));
        assertAliasesResolve(EnumWrappers.getEntityActionConverter(),
                EnumWrappers.getPlayerActionClass(), EnumWrappers.PlayerAction.values());
    }

    // converts every wrapper constant that only exists under an alias in the native enum, returns their number
    private static <T extends Enum<T> & EnumWrappers.AliasedEnum> int assertAliasesResolve(
            EquivalentConverter<T> converter, Class<?> nativeClass, T[] wrapperConstants) {
        Set<String> nativeNames = Sets.newHashSet();
        for (Object constant : nativeClass.getEnumConstants()) {
            nativeNames.add(((Enum<?>) constant).name());
        }

        int aliased = 0;
        for (T constant : wrapperConstants) {
            if (nativeNames.contains(constant.name())) {
                continue;
            }
            for (String alias : constant.getAliases()) {
                if (nativeNames.contains(alias)) {
                    assertEquals(alias, ((Enum<?>) converter.getGeneric(constant)).name());
                    aliased++;
                }
            }
        }
        return aliased;
    }

    @Test
    public void testGenericConverterDoesNotRetainForeignClassLoaders() throws Exception {
        // the enum of another plugin: once that plugin is disabled, its class loader must be collectable
        WeakReference<ClassLoader> loader = requestConverterForForeignEnum();
        for (int i = 0; i < 50 && loader.get() != null; i++) {
            System.gc();
            Thread.sleep(20);
        }
        assertNull(loader.get(), "the class loader of a foreign enum is still reachable");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static WeakReference<ClassLoader> requestConverterForForeignEnum() throws Exception {
        ClassLoader foreignLoader = new ForeignEnumLoader(EnumWrappersTest.class.getClassLoader());
        Class<? extends Enum> foreignEnum = (Class<? extends Enum>) foreignLoader.loadClass(ForeignEnum.class.getName());
        assertNotSame(ForeignEnum.class, foreignEnum);

        EquivalentConverter<?> converter = EnumWrappers.getGenericConverter(EnumWrappers.getPlayerDigTypeClass(), foreignEnum);
        assertNotNull(converter);
        return new WeakReference<>(foreignLoader);
    }

    // defines its own copy of ForeignEnum, like the class loader of another plugin
    private static final class ForeignEnumLoader extends ClassLoader {

        private ForeignEnumLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.equals(ForeignEnum.class.getName())) {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = input.readAllBytes();
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException exception) {
                        throw new ClassNotFoundException(name, exception);
                    }
                }
                return loaded;
            }
        }
    }
}

enum ForeignEnum {
    START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK
}
