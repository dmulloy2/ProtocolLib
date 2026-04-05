package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundProjectilePowerPacket} (game phase, clientbound).
 */
public class WrappedClientboundProjectilePowerPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PROJECTILE_POWER;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(double.class);

    public WrappedClientboundProjectilePowerPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundProjectilePowerPacket(int id, double accelerationPower) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id, accelerationPower)));
    }

    public WrappedClientboundProjectilePowerPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public double getAccelerationPower() {
        return handle.getDoubles().read(0);
    }

    public void setAccelerationPower(double accelerationPower) {
        handle.getDoubles().write(0, accelerationPower);
    }
}
