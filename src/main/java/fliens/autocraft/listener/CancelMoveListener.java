package fliens.autocraft.listener;

import fliens.autocraft.CrafterStore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;

public class CancelMoveListener implements Listener {
    private final CrafterStore store;

    public CancelMoveListener(CrafterStore store) {
        this.store = store;
    }

    @EventHandler
    public void onItemMove(BlockDispenseEvent event) {
        if (store.isCrafterAt(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperItemMove(InventoryMoveItemEvent event) {
        if (event.getSource().getType() == InventoryType.HOPPER && event.getDestination().getType() == InventoryType.DISPENSER) {
            if (store.isCrafterAt(event.getDestination().getLocation())) {
                event.setCancelled(true);
            }
        }
        else if (event.getSource().getType() == InventoryType.DISPENSER && event.getDestination().getType() == InventoryType.HOPPER) {
            if (store.isCrafterAt(event.getSource().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
