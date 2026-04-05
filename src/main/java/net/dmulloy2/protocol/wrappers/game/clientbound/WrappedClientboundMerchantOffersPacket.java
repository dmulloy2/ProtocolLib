package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.inventory.MerchantRecipe;

/**
 * Wrapper for {@code ClientboundMerchantOffersPacket} (game phase, clientbound).
 */
public class WrappedClientboundMerchantOffersPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.OPEN_WINDOW_MERCHANT;

    public WrappedClientboundMerchantOffersPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundMerchantOffersPacket(int containerId, List<MerchantRecipe> offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {
        this();
        setContainerId(containerId);
        setOffers(offers);
        setVillagerLevel(villagerLevel);
        setVillagerXp(villagerXp);
        setShowProgress(showProgress);
        setCanRestock(canRestock);
    }

    public WrappedClientboundMerchantOffersPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public List<MerchantRecipe> getOffers() {
        return handle.getMerchantRecipeLists().read(0);
    }

    public void setOffers(List<MerchantRecipe> offers) {
        handle.getMerchantRecipeLists().write(0, offers);
    }

    public int getVillagerLevel() {
        return handle.getIntegers().read(1);
    }

    public void setVillagerLevel(int villagerLevel) {
        handle.getIntegers().write(1, villagerLevel);
    }

    public int getVillagerXp() {
        return handle.getIntegers().read(2);
    }

    public void setVillagerXp(int villagerXp) {
        handle.getIntegers().write(2, villagerXp);
    }

    public boolean isShowProgress() {
        return handle.getBooleans().read(0);
    }

    public void setShowProgress(boolean showProgress) {
        handle.getBooleans().write(0, showProgress);
    }

    public boolean isCanRestock() {
        return handle.getBooleans().read(1);
    }

    public void setCanRestock(boolean canRestock) {
        handle.getBooleans().write(1, canRestock);
    }
}
