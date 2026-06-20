package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.EnumWrappers.TeamCollisionRule;
import com.comphenix.protocol.wrappers.EnumWrappers.TeamVisibility;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.TeamColor;

public class WrappedTeamParametersTest {
    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testTeamParameters() {
        Component displayName = Component.literal("display name");
        Component prefix = Component.literal("prefix");
        Component suffix = Component.literal("suffix");
        TeamVisibility nametagVisibility = TeamVisibility.ALWAYS;
        TeamCollisionRule collisionRule = TeamCollisionRule.NEVER;

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
        assertEquals(handle.displayName(), displayName);
        assertEquals(handle.playerPrefix(), prefix);
        assertEquals(handle.playerSuffix(), suffix);
        assertEquals(handle.nameTagVisibility(), Team.Visibility.ALWAYS);
        assertEquals(handle.collisionRule(), Team.CollisionRule.NEVER);
        assertEquals(handle.color(), Optional.of(TeamColor.RED));
        assertEquals(handle.options(), 1);
    }
}
