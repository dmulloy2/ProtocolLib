package com.comphenix.protocol.wrappers.ping;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.common.collect.ImmutableList;

public interface ServerPingImpl extends Cloneable {
    WrappedChatComponent getMotD();
    void setMotD(WrappedChatComponent description);
    int getPlayersMaximum();
    void setPlayersMaximum(int maxPlayers);
    int getPlayersOnline();
    void setPlayersOnline(int onlineCount);
    ImmutableList<WrappedGameProfile> getPlayers();
    void setPlayers(Iterable<? extends WrappedGameProfile> playerSample);
    String getVersionName();
    void setVersionName(String versionName);
    int getVersionProtocol();
    void setVersionProtocol(int protocolVersion);
    WrappedServerPing.CompressedImage getFavicon();
    void setFavicon(WrappedServerPing.CompressedImage favicon);
    boolean isEnforceSecureChat();
    void setEnforceSecureChat(boolean safeChat);

    void resetPlayers();
    void resetVersion();

    default boolean isChatPreviewEnabled() {
        return false;
    }

    default void setChatPreviewEnabled(boolean enabled) {

    }

    boolean arePlayersVisible();
    void setPlayersVisible(boolean visible);
    String getJson();

    Object getHandle();
}