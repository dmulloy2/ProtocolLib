package com.comphenix.protocol.wrappers;

import static com.comphenix.protocol.utility.MinecraftReflection.getMinecraftClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;

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
        display.background = Optional.of(new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("minecraft", "test")));
        display.frameType = WrappedFrameType.CHALLENGE;
        display.announceChat = false;
        display.showToast = true;
        display.hidden = true;
        display.x = 5f;
        display.y = 67f;

        DisplayInfo nms = (DisplayInfo) displayWrapper().unwrap(display);

        assertTrue(nms.shouldShowToast());
        assertTrue(nms.isHidden());
        assertFalse(nms.shouldAnnounceChat());
        assertTrue(nms.getBackground().isPresent());
        assertEquals("test", nms.getBackground().get().id().getPath());
        validateRawText(nms.getTitle(), "Test123");
        validateRawText(nms.getDescription(), "Test567");
        assertSame(AdvancementType.CHALLENGE, nms.getType());
        assertSame(MinecraftReflection.getBukkitItemStack(nms.getIcon()).getType(), Material.GOLD_INGOT);
        assertEquals(5f, nms.getX(), 0f);
        assertEquals(67f, nms.getY(), 0f);
    }

    @Test
    public void testFromNms() {
        DisplayInfo display = new DisplayInfo(
              (net.minecraft.world.item.ItemStack)MinecraftReflection.getMinecraftItemStack(new ItemStack(Material.ENDER_EYE)),
              Component.literal("Test123"),
              Component.literal("Test567"),
			  Optional.of(new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("minecraft", "test"))),
              AdvancementType.CHALLENGE,
              true,
              false,
              true
        );
        display.setLocation(5f, 67f);

        WrappedAdvancementDisplay wrapped = displayWrapper().wrap(display);

        assertTrue(wrapped.showToast);
        assertTrue(wrapped.hidden);
        assertFalse(wrapped.announceChat);
        assertTrue(wrapped.background.isPresent());
        assertEquals("test", wrapped.background.get().id().getPath());
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
                .field(3, Converters.optional(Converters.passthrough(ClientAsset.class)))
                .field(4, EnumWrappers.getGenericConverter(
                    getMinecraftClass("advancements.AdvancementType", "advancements.AdvancementFrameType", "advancements.FrameType"),
                    WrappedFrameType.class)
                );
    }

    private void validateRawText(Component component, String expected) {
        PlainTextContents.LiteralContents content = assertInstanceOf(PlainTextContents.LiteralContents.class, component.getContents());
        assertEquals(expected, content.text());
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
        public Optional<ClientAsset> background;
        public WrappedFrameType frameType;
        public boolean showToast;
        public boolean announceChat;
        public boolean hidden;
        public float x;
        public float y;
    }
}
