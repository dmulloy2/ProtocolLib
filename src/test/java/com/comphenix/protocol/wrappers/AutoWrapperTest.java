package com.comphenix.protocol.wrappers;

import java.util.Optional;

import static com.comphenix.protocol.utility.MinecraftReflection.getMinecraftClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;

import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.advancements.AdvancementFrameType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.item.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AutoWrapperTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testToNms() {
        WrappedAdvancementDisplay display = new WrappedAdvancementDisplay();
        display.title = WrappedChatComponent.fromText("Test123");
        display.description = WrappedChatComponent.fromText("Test567");
        display.item = new ItemStack(Material.GOLD_INGOT);
        display.background = Optional.of(new MinecraftKey("test"));
        display.frameType = WrappedFrameType.CHALLENGE;
        display.announceChat = false;
        display.showToast = true;
        display.hidden = true;
        display.x = 5f;
        display.y = 67f;

        AdvancementDisplay nms = (AdvancementDisplay) displayWrapper().unwrap(display);

        assertTrue(nms.h());
        assertTrue(nms.j());
        assertFalse(nms.i());
		    assertTrue(nms.d().isPresent());
        assertEquals("test", nms.d().get().a());
        validateRawText(nms.a(), "Test123");
        validateRawText(nms.b(), "Test567");
        assertSame(AdvancementFrameType.b, nms.e());
        assertSame(MinecraftReflection.getBukkitItemStack(nms.c()).getType(), Material.GOLD_INGOT);
        assertEquals(5f, nms.f(), 0f);
        assertEquals(67f, nms.g(), 0f);
    }

    @Test
    public void testFromNms() {
        AdvancementDisplay display = new AdvancementDisplay(
              (net.minecraft.world.item.ItemStack)MinecraftReflection.getMinecraftItemStack(new ItemStack(Material.ENDER_EYE)),
              IChatBaseComponent.b("Test123"),
              IChatBaseComponent.b("Test567"),
			  Optional.of(new net.minecraft.resources.MinecraftKey("minecraft", "test")),
              AdvancementFrameType.b,
              true,
              false,
              true
        );
        display.a(5f, 67f);

        WrappedAdvancementDisplay wrapped = displayWrapper().wrap(display);

        assertTrue(wrapped.showToast);
        assertTrue(wrapped.hidden);
        assertFalse(wrapped.announceChat);
        assertTrue(wrapped.background.isPresent());
        assertEquals("test", wrapped.background.get().getKey());
        assertEquals("\"Test123\"", wrapped.title.getJson());
        assertEquals("\"Test567\"", wrapped.description.getJson());
        assertSame(WrappedFrameType.CHALLENGE, wrapped.frameType);
        assertSame(Material.ENDER_EYE, wrapped.item.getType());
        assertEquals(5f, wrapped.x, 0f);
        assertEquals(67f, wrapped.y, 0f);
    }

    private AutoWrapper<WrappedAdvancementDisplay> displayWrapper() {
        return AutoWrapper
                .wrap(WrappedAdvancementDisplay.class, "advancements.AdvancementDisplay", "advancements.DisplayInfo")
                .field(0, BukkitConverters.getWrappedChatComponentConverter())
                .field(1, BukkitConverters.getWrappedChatComponentConverter())
                .field(2, BukkitConverters.getItemStackConverter())
                .field(3, Converters.optional(MinecraftKey.getConverter()))
                .field(4, EnumWrappers.getGenericConverter(getMinecraftClass("advancements.AdvancementFrameType", "advancements.FrameType"),
                        WrappedFrameType.class));
    }

    private void validateRawText(IChatBaseComponent component, String expected) {
        LiteralContents content = assertInstanceOf(LiteralContents.class, component.b());
        assertEquals(expected, content.b());
    }

    public enum WrappedFrameType {
        TASK,
        CHALLENGE,
        GOAL
    }

    public static final class WrappedAdvancementDisplay {

        public WrappedChatComponent title;
        public WrappedChatComponent description;
        public ItemStack item;
        public Optional<MinecraftKey> background;
        public WrappedFrameType frameType;
        public boolean showToast;
        public boolean announceChat;
        public boolean hidden;
        public float x;
        public float y;
    }
}
