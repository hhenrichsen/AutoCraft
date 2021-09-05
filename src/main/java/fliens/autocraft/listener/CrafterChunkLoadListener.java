package fliens.autocraft.listener;

import fliens.autocraft.Autocrafter;
import fliens.autocraft.CrafterStore;
import fliens.autocraft.util.BlockUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CrafterChunkLoadListener implements Listener {
    private final CrafterStore store;
    private final JavaPlugin plugin;

    public CrafterChunkLoadListener(JavaPlugin plugin, CrafterStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Chunk chunk = event.getChunk().getWorld().getChunkAt(event.getChunk().getBlock(0, 0, 0));
            if (!chunk.isLoaded()) {
                return;
            }
            for (Entity entity : chunk.getEntities()) {
                if (entity.getType().equals(EntityType.ITEM_FRAME) || entity.getType().equals(EntityType.GLOW_ITEM_FRAME)) {
                    ItemFrame frame = (ItemFrame) entity;
                    Autocrafter possibleCrafter = BlockUtil.attemptCreateCrafter(frame);
                    if (possibleCrafter != null) {
                        store.addCrafter(event.getChunk(), possibleCrafter);
                    }
                }
            }
        }, 20L); // Run later to get around Spigot/Paper issue where no entities are returned.
    }
}
