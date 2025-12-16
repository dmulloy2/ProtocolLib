package com.comphenix.protocol.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttributeModifier.Operation;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
                attributeKey("attack_damage").
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

        Registry<Attribute> registry = BuiltInRegistries.ATTRIBUTE;
        String attributeKey = attribute.getAttributeKey();
        Identifier key = Identifier.parse(attributeKey);
        Attribute base = registry.getValue(key);
        Holder<Attribute> holder = registry.wrapAsHolder(base);
        return new AttributeSnapshot(holder, attribute.getBaseValue(), modifiers);
    }

    private AttributeModifier getModifierCopy(WrappedAttributeModifier modifier) {
        AttributeModifier.Operation operation = AttributeModifier.Operation.values()[modifier.getOperation().getId()];
        return new AttributeModifier((Identifier) com.comphenix.protocol.wrappers.MinecraftKey.getConverter().getGeneric(modifier.getKey()),
            modifier.getAmount(), operation);
    }
}
