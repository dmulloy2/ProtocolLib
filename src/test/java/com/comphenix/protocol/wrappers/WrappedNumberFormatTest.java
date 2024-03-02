package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
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
        assertInstanceOf(WrappedNumberFormat.Blank.class, WrappedNumberFormat.fromHandle(BlankFormat.a));
        assertEquals(BlankFormat.a, WrappedNumberFormat.blank().getHandle());
    }

    @Test
    void testFixedFormat() {
        IChatBaseComponent content = IChatBaseComponent.a("Fixed");
        WrappedNumberFormat wrappedHandle = WrappedNumberFormat.fromHandle(new FixedFormat(content));
        assertInstanceOf(WrappedNumberFormat.Fixed.class, wrappedHandle);
        assertEquals(content, ((WrappedNumberFormat.Fixed) wrappedHandle).getContent().getHandle());

        WrappedNumberFormat.Fixed wrapped = WrappedNumberFormat.fixed(WrappedChatComponent.fromHandle(content));
        assertEquals(content, wrapped.getContent().getHandle());
    }

    @Test
    void testStyledFormat() {
        ChatModifier style = ChatModifier.a.b(EnumChatFormat.g);
        WrappedNumberFormat wrappedHandle = WrappedNumberFormat.fromHandle(new StyledFormat(style));
        assertInstanceOf(WrappedNumberFormat.Styled.class, wrappedHandle);
        assertEquals(style, ((WrappedNumberFormat.Styled) wrappedHandle).getStyle().getHandle());

        WrappedNumberFormat.Styled newWrapper = WrappedNumberFormat.styled(new WrappedComponentStyle(style));
        assertEquals(style, newWrapper.getStyle().getHandle());
    }
}
