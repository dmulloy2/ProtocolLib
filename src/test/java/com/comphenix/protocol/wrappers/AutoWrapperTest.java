package com.comphenix.protocol.wrappers;

import static com.comphenix.protocol.utility.MinecraftReflection.getMinecraftClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.comphenix.protocol.BukkitInitialization;
import java.util.List;
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
    display.item = new ItemStack(Material.DARK_OAK_SAPLING);
    display.background = new MinecraftKey("test");
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
    assertEquals(nms.d().a(), "test");
    validateRawText(nms.a(), "Test123");
    validateRawText(nms.b(), "Test567");
    assertSame(AdvancementFrameType.b, nms.e());
    assertSame(Items.L, nms.c().c());
    assertEquals(5f, nms.f(), 0f);
    assertEquals(67f, nms.g(), 0f);
  }

  @Test
  public void testFromNms() {
    AdvancementDisplay display = new AdvancementDisplay(
        new net.minecraft.world.item.ItemStack(Items.qA),
        IChatBaseComponent.b("Test123"),
        IChatBaseComponent.b("Test567"),
        new net.minecraft.resources.MinecraftKey("minecraft", "test"),
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
    assertEquals("test", wrapped.background.getKey());
    assertEquals("{\"text\":\"Test123\"}", wrapped.title.getJson());
    assertEquals("{\"text\":\"Test567\"}", wrapped.description.getJson());
    assertSame(WrappedFrameType.CHALLENGE, wrapped.frameType);
    assertSame(Material.ENDER_EYE, wrapped.item.getType());
    assertEquals(5f, wrapped.x, 0f);
    assertEquals(67f, wrapped.y, 0f);
  }

  private AutoWrapper<WrappedAdvancementDisplay> displayWrapper() {
    return AutoWrapper
        .wrap(WrappedAdvancementDisplay.class, "advancements.AdvancementDisplay")
        .field(0, BukkitConverters.getWrappedChatComponentConverter())
        .field(1, BukkitConverters.getWrappedChatComponentConverter())
        .field(2, BukkitConverters.getItemStackConverter())
        .field(3, MinecraftKey.getConverter())
        .field(4, EnumWrappers.getGenericConverter(getMinecraftClass("advancements.AdvancementFrameType"),
            WrappedFrameType.class));
  }

  private void validateRawText(IChatBaseComponent component, String expected) {
    List<IChatBaseComponent> siblings = component.c();
    assertEquals(1, siblings.size());

    IChatBaseComponent sibling = siblings.get(0);
    assertInstanceOf(LiteralContents.class, sibling.b());
    assertEquals(expected, ((LiteralContents) sibling.b()).a());
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
    public MinecraftKey background;
    public WrappedFrameType frameType;
    public boolean showToast;
    public boolean announceChat;
    public boolean hidden;
    public float x;
    public float y;
  }
}
