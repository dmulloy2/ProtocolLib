package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
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
        IChatBaseComponent displayName = IChatBaseComponent.b("display name");
        IChatBaseComponent prefix = IChatBaseComponent.b("prefix");
        IChatBaseComponent suffix = IChatBaseComponent.b("suffix");
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

        PacketPlayOutScoreboardTeam.b handle = (PacketPlayOutScoreboardTeam.b) wrapped.getHandle();
        assertEquals(handle.a(), displayName);
        assertEquals(handle.f(), prefix);
        assertEquals(handle.g(), suffix);
        assertEquals(handle.d(), nametagVisibility);
        assertEquals(handle.e(), collisionRule);
        assertEquals(handle.c(), EnumChatFormat.m);
        assertEquals(handle.b(), 1);
    }
}
