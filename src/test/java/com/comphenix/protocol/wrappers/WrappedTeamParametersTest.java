package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrappedTeamParametersTest {
    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testTeamParameters() {
        Component displayName = Component.literal("display name");
        Component prefix = Component.literal("prefix");
        Component suffix = Component.literal("suffix");
        String nametagVisibility = "always";
        String collisionRule = "never";

        WrappedTeamParameters wrapped = WrappedTeamParameters.newBuilder()
                .displayName(WrappedChatComponent.fromHandle(displayName))
                .prefix(WrappedChatComponent.fromHandle(prefix))
                .suffix(WrappedChatComponent.fromHandle(suffix))
                .nametagVisibility(nametagVisibility)
                .collisionRule(collisionRule)
                .color(EnumWrappers.ChatFormatting.RED)
                .options(1)
                .build();

        assertEquals(displayName, wrapped.getDisplayName().getHandle());
        assertEquals(prefix, wrapped.getPrefix().getHandle());
        assertEquals(suffix, wrapped.getSuffix().getHandle());
        assertEquals(nametagVisibility, wrapped.getNametagVisibility());
        assertEquals(collisionRule, wrapped.getCollisionRule());
        assertEquals(EnumWrappers.ChatFormatting.RED, wrapped.getColor());
        assertEquals(1, wrapped.getOptions());

        ClientboundSetPlayerTeamPacket.Parameters handle = (ClientboundSetPlayerTeamPacket.Parameters) wrapped.getHandle();
        assertEquals(handle.getDisplayName(), displayName);
        assertEquals(handle.getPlayerPrefix(), prefix);
        assertEquals(handle.getPlayerSuffix(), suffix);
        assertEquals(handle.getNametagVisibility(), nametagVisibility);
        assertEquals(handle.getCollisionRule(), collisionRule);
        assertEquals(handle.getColor(), ChatFormatting.RED);
        assertEquals(handle.getOptions(), 1);
    }
}
