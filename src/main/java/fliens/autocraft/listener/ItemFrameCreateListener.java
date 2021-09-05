package fliens.autocraft.listener;

import fliens.autocraft.Autocrafter;
import fliens.autocraft.CrafterStore;
import fliens.autocraft.util.BlockUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ItemFrameCreateListener implements Listener {
    private final CrafterStore store;

    public ItemFrameCreateListener(CrafterStore store) {
        this.store = store;
    }

    @EventHandler
    public void onFrameSpawn(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (entity.getType() != EntityType.ITEM_FRAME && entity.getType() != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        if (!(entity instanceof ItemFrame frame)) {
            return;
        }
        Autocrafter possibleCrafter;
        if (frame.getItem().getType() == Material.AIR) {
             possibleCrafter = BlockUtil.attemptCreateCrafter(frame, event.getPlayer().getInventory().getItem(event.getHand()));
        }
        else {
            possibleCrafter = BlockUtil.attemptCreateCrafter(frame);
        }
        if (possibleCrafter == null) {
            return;
        }
        store.addCrafter(BlockUtil.getAttachedBlock(frame).getChunk(), possibleCrafter);
    }
}
