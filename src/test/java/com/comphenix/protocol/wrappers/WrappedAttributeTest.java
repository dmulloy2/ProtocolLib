package com.comphenix.protocol.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttributeModifier.Operation;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes.AttributeSnapshot;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WrappedAttributeTest {

    private WrappedAttributeModifier doubleModifier;
    private WrappedAttributeModifier constantModifier;
    private WrappedAttribute attribute;

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @BeforeEach
    public void setUp() {
        // Create a couple of modifiers
        this.doubleModifier =
                WrappedAttributeModifier.newBuilder().
                        name("Double Damage").
                        amount(1).
                        operation(Operation.ADD_PERCENTAGE).
                        build();
        this.constantModifier =
                WrappedAttributeModifier.newBuilder().
                        name("Damage Bonus").
                        amount(5).
                        operation(Operation.ADD_NUMBER).
                        build();

        // Create attribute
        this.attribute = WrappedAttribute.newBuilder().
                attributeKey("generic.attackDamage").
                baseValue(2).
                packet(new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES)).
                modifiers(Lists.newArrayList(this.constantModifier, this.doubleModifier)).
                build();
    }

    @Test
    public void testEquality() {
        // Check wrapped equality
        assertEquals(this.doubleModifier, this.doubleModifier);
        assertNotSame(this.constantModifier, this.doubleModifier);

        assertEquals(this.doubleModifier.getHandle(), this.getModifierCopy(this.doubleModifier));
        assertEquals(this.constantModifier.getHandle(), this.getModifierCopy(this.constantModifier));
    }

    @Test
    @Disabled // TODO -- modifiers are missing (or the hasModifier check is wrong)
    public void testAttribute() {
        assertEquals(this.attribute, WrappedAttribute.fromHandle(this.getAttributeCopy(this.attribute)));

        assertTrue(this.attribute.hasModifier(this.doubleModifier.getUUID()));
        assertTrue(this.attribute.hasModifier(this.constantModifier.getUUID()));
    }

    @Test
    public void testFromTemplate() {
        assertEquals(this.attribute, WrappedAttribute.newBuilder(this.attribute).build());
    }

    /**
     * Retrieve the equivalent NMS attribute.
     *
     * @param attribute - the wrapped attribute.
     * @return The equivalent NMS attribute.
     */
    private AttributeSnapshot getAttributeCopy(WrappedAttribute attribute) {
        List<AttributeModifier> modifiers = new ArrayList<>();

        for (WrappedAttributeModifier wrapper : attribute.getModifiers()) {
            modifiers.add((AttributeModifier) wrapper.getHandle());
        }

        IRegistry<AttributeBase> registry = BuiltInRegistries.u;
        String attributeKey = attribute.getAttributeKey();
        MinecraftKey key = MinecraftKey.a(attributeKey);
        AttributeBase base = registry.a(key);
        Holder<AttributeBase> holder = registry.e(base);
        return new AttributeSnapshot(holder, attribute.getBaseValue(), modifiers);
    }

    private AttributeModifier getModifierCopy(WrappedAttributeModifier modifier) {
        AttributeModifier.Operation operation = AttributeModifier.Operation.values()[modifier.getOperation().getId()];
        return new AttributeModifier(modifier.getUUID(), modifier.getName(), modifier.getAmount(), operation);
    }
}
