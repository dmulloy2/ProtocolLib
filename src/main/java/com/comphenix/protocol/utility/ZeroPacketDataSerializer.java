package com.comphenix.protocol.utility;

import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketDataSerializer;

/**
 * Tricks NMS into letting us create empty packets.
 * This is currently only used for MAP_CHUNK, but should be replaced with ByteBuddy or similar.
 */
public class ZeroPacketDataSerializer extends PacketDataSerializer {
    public ZeroPacketDataSerializer() {
        super(new ZeroBuffer());
    }

    public NBTTagCompound a(NBTReadLimiter lim) {
        return new NBTTagCompound();
    }
}
