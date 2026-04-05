package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundTestInstanceBlockActionPacket} (game phase, serverbound).
 * Test instance block action. Complex fields with no ProtocolLib accessor.
 */
public class WrappedServerboundTestInstanceBlockActionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION;

    public WrappedServerboundTestInstanceBlockActionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundTestInstanceBlockActionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'pos' (NMS type: BlockPos) — use getBlockPositionModifier().read(0) / write(0, value)
    // TODO: missing field 'action' (NMS type: ServerboundTestInstanceBlockActionPacket.Action enum)
    //   — declare a local Action enum matching NMS constants and use getEnumModifier(Action.class, globalIndex)
    // TODO: missing field 'data' (NMS type: TestInstanceBlockEntity.Data — record with Optional<ResourceKey<GameTestInstance>>, Vec3i, Rotation, boolean, Status, Optional<Component>)
    //   — complex record; use AutoWrapper or a dedicated WrappedTestInstanceData class
}
