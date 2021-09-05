package fliens.autocraft.listener;

import fliens.autocraft.CrafterStore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CrafterChunkUnloadListener implements Listener {
    private final CrafterStore store;
    private final JavaPlugin plugin;

    public CrafterChunkUnloadListener(JavaPlugin plugin, CrafterStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        store.removeChunk(event.getChunk());
    }
}
