package fliens.autocraft.listener;

import fliens.autocraft.CrafterStore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ItemFrameDestroyListener implements Listener {
    private final CrafterStore store;

    public ItemFrameDestroyListener(CrafterStore store) {
        this.store = store;
    }

    @EventHandler
    public void onFrameDestroy(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.ITEM_FRAME && entity.getType() != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        if (!(entity instanceof ItemFrame frame)) {
            return;
        }
        store.removeFrame(frame);
    }
}
