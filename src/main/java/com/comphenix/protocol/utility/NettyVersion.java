package com.comphenix.protocol.utility;

import com.comphenix.protocol.ProtocolLibrary;
import io.netty.util.Version;

import java.util.Map;

public class NettyVersion {
    private final static String NETTY_ARTIFACT_ID = "netty-common";
    private static NettyVersion version;

    public static NettyVersion getVersion() {
        if(version == null) {
            version = detectVersion();
        }
        return version;
    }

    private static NettyVersion detectVersion() {
        Map<String, Version> nettyArtifacts = Version.identify();
        Version version = nettyArtifacts.get(NETTY_ARTIFACT_ID);
        if(version != null) {
            return new NettyVersion(version.artifactVersion());
        }
        return new NettyVersion(null);
    }

    private boolean valid = false;
    private int major, minor, revision;

    public NettyVersion(String s) {
        if(s == null) {
            return;
        }
        String[] split = s.split( "\\.");
        try {
            this.major = Integer.parseInt(split[0]);
            this.minor = Integer.parseInt(split[1]);
            this.revision = Integer.parseInt(split[2]);
            this.valid = true;
        } catch (Throwable t) {
            ProtocolLibrary.getPlugin().getLogger().warning("Could not detect netty version: '" + s + "'");
        }
    }

    @Override
    public String toString() {
        if(!valid) {
            return "(invalid)";
        }
        return major + "." + minor + "." + revision;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NettyVersion)) {
            return false;
        }
        NettyVersion v = (NettyVersion) obj;
        return v.major == major && v.minor == minor && v.revision == revision;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isValid() {
        return this.valid;
    }

    public boolean isGreaterThan(int major, int minor, int rev) {
        return this.major > major || this.minor > minor || this.revision > rev;
    }

}
