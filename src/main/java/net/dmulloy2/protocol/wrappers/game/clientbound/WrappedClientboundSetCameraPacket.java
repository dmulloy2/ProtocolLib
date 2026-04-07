package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundSetCameraPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int cameraEntityId} – entity ID whose perspective the player views</li>
 * </ul>
 */
public class WrappedClientboundSetCameraPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CAMERA;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance());

    public WrappedClientboundSetCameraPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetCameraPacket(int cameraEntityId) {
        this();
        setCameraEntityId(cameraEntityId);
    }

    public WrappedClientboundSetCameraPacket(Entity camera) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(camera)));
    }

    public WrappedClientboundSetCameraPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Entity getCamera(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setCamera(Entity camera) {
        handle.getEntityModifier(camera.getWorld()).writeSafely(0, camera);
    }

    public int getCameraEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setCameraEntityId(int cameraEntityId) {
        handle.getIntegers().writeSafely(0, cameraEntityId);
    }
}
