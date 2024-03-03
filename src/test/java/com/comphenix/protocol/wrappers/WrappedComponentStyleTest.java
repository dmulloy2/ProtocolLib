package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatModifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrappedComponentStyleTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testComponentStyle() {
        ChatModifier style = ChatModifier.a.b(EnumChatFormat.m).a(true);
        WrappedComponentStyle wrapped = new WrappedComponentStyle(style);
        JsonElement json = wrapped.getJson();
        assertEquals("{\"color\":\"red\",\"bold\":true}", json.toString());
        assertEquals(style, WrappedComponentStyle.fromJson(json).getHandle());
    }

    @Test
    public void testStyleAdventureConversion() {
        Style adventureStyle = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.changePage(10));

        WrappedComponentStyle wrapped = AdventureComponentConverter.fromStyle(adventureStyle);
        assertEquals(adventureStyle, AdventureComponentConverter.fromWrapper(wrapped));
    }
}
