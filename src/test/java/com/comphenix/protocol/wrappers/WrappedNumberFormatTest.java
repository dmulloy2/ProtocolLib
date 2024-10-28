package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class WrappedNumberFormatTest {

    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testBlankFormat() {
        assertInstanceOf(WrappedNumberFormat.Blank.class, WrappedNumberFormat.fromHandle(BlankFormat.INSTANCE));
        assertEquals(BlankFormat.INSTANCE, WrappedNumberFormat.blank().getHandle());
    }

    @Test
    void testFixedFormat() {
        Component content = Component.literal("Fixed");
        WrappedNumberFormat wrappedHandle = WrappedNumberFormat.fromHandle(new FixedFormat(content));
        assertInstanceOf(WrappedNumberFormat.Fixed.class, wrappedHandle);
        assertEquals(content, ((WrappedNumberFormat.Fixed) wrappedHandle).getContent().getHandle());

        WrappedNumberFormat.Fixed wrapped = WrappedNumberFormat.fixed(WrappedChatComponent.fromHandle(content));
        assertEquals(content, wrapped.getContent().getHandle());
    }

    @Test
    void testStyledFormat() {
        Style style = Style.EMPTY.applyFormat(ChatFormatting.AQUA);
        WrappedNumberFormat wrappedHandle = WrappedNumberFormat.fromHandle(new StyledFormat(style));
        assertInstanceOf(WrappedNumberFormat.Styled.class, wrappedHandle);
        assertEquals(style, ((WrappedNumberFormat.Styled) wrappedHandle).getStyle().getHandle());

        WrappedNumberFormat.Styled newWrapper = WrappedNumberFormat.styled(new WrappedComponentStyle(style));
        assertEquals(style, newWrapper.getStyle().getHandle());
    }
}
