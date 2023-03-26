package com.comphenix.protocol.wrappers.ping;

public interface ServerPingImpl {
	Object getMotD();
	void setMotD(Object description);
	int getPlayersMaximum();
	void setPlayersMaximum(int maxPlayers);
	int getPlayersOnline();
	void setPlayersOnline(int onlineCount);
	Object getPlayers();
	void setPlayers(Object playerSample);
	String getVersionName();
	void setVersionName(String versionName);
	int getVersionProtocol();
	void setVersionProtocol(int protocolVersion);
	String getFavicon();
	void setFavicon(String favicon);
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

	Object getHandle();
}