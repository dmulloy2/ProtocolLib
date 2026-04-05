package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.boss.CraftBossBar;

public class WrappedClientboundBossEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BOSS;

    private static final MethodAccessor CREATE_ADD_PACKET;
    private static final MethodAccessor CREATE_REMOVE_PACKET;
    private static final MethodAccessor CREATE_UPDATE_PROGRESS_PACKET;
    private static final MethodAccessor CREATE_UPDATE_NAME_PACKET;
    private static final MethodAccessor CREATE_UPDATE_STYLE_PACKET;
    private static final MethodAccessor CREATE_UPDATE_PROPERTIES_PACKET;
    private static final FieldAccessor BOSS_BAR_HANDLE;

    static {
        try {
            Class<?> packetClass = MinecraftReflection.getMinecraftClass(
                    "network.protocol.game.ClientboundBossEventPacket");
            Class<?> bossEventClass = MinecraftReflection.getMinecraftClass("world.BossEvent");

            CREATE_ADD_PACKET = Accessors.getMethodAccessor(packetClass, "createAddPacket", bossEventClass);
            CREATE_REMOVE_PACKET = Accessors.getMethodAccessor(packetClass, "createRemovePacket", UUID.class);
            CREATE_UPDATE_PROGRESS_PACKET = Accessors.getMethodAccessor(packetClass, "createUpdateProgressPacket", bossEventClass);
            CREATE_UPDATE_NAME_PACKET = Accessors.getMethodAccessor(packetClass, "createUpdateNamePacket", bossEventClass);
            CREATE_UPDATE_STYLE_PACKET = Accessors.getMethodAccessor(packetClass, "createUpdateStylePacket", bossEventClass);
            CREATE_UPDATE_PROPERTIES_PACKET = Accessors.getMethodAccessor(packetClass, "createUpdatePropertiesPacket", bossEventClass);

            BOSS_BAR_HANDLE = Accessors.getFieldAccessorOrNull(CraftBossBar.class, "handle", null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public WrappedClientboundBossEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundBossEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public static WrappedClientboundBossEventPacket createAddPacket(BossBar bossBar) {
        Object event = BOSS_BAR_HANDLE.get(bossBar);
        return new WrappedClientboundBossEventPacket(PacketContainer.fromPacket(
                CREATE_ADD_PACKET.invoke(null, event)));
    }

    public static WrappedClientboundBossEventPacket createRemovePacket(UUID id) {
        return new WrappedClientboundBossEventPacket(PacketContainer.fromPacket(
                CREATE_REMOVE_PACKET.invoke(null, id)));
    }

    public static WrappedClientboundBossEventPacket createUpdateProgressPacket(BossBar bossBar) {
        Object event = BOSS_BAR_HANDLE.get(bossBar);
        return new WrappedClientboundBossEventPacket(PacketContainer.fromPacket(
                CREATE_UPDATE_PROGRESS_PACKET.invoke(null, event)));
    }

    public static WrappedClientboundBossEventPacket createUpdateNamePacket(BossBar bossBar) {
        Object event = BOSS_BAR_HANDLE.get(bossBar);
        return new WrappedClientboundBossEventPacket(PacketContainer.fromPacket(
                CREATE_UPDATE_NAME_PACKET.invoke(null, event)));
    }

    public static WrappedClientboundBossEventPacket createUpdateStylePacket(BossBar bossBar) {
        Object event = BOSS_BAR_HANDLE.get(bossBar);
        return new WrappedClientboundBossEventPacket(PacketContainer.fromPacket(
                CREATE_UPDATE_STYLE_PACKET.invoke(null, event)));
    }

    public static WrappedClientboundBossEventPacket createUpdatePropertiesPacket(BossBar bossBar) {
        Object event = BOSS_BAR_HANDLE.get(bossBar);
        return new WrappedClientboundBossEventPacket(PacketContainer.fromPacket(
                CREATE_UPDATE_PROPERTIES_PACKET.invoke(null, event)));
    }

    public UUID getId() {
        return handle.getUUIDs().read(0);
    }

    public void setId(UUID id) {
        handle.getUUIDs().write(0, id);
    }

    public Object getOperation() {
        return handle.getModifier().read(1);
    }

    public void setOperation(Object operation) {
        handle.getModifier().write(1, operation);
    }
}
