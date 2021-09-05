package fliens.autocraft;

import fliens.autocraft.util.BlockUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CrafterStore {
    private final Map<Chunk, Set<Autocrafter>> chunkedCrafters = new HashMap<>();
    private final Map<Location, Autocrafter> locationCrafters = new HashMap<>();
    private final Set<Autocrafter> crafters = new HashSet<>();

    public CrafterStore() {}

    public void addCrafter(Chunk chunk, Autocrafter autocrafter) {
        if (!chunkedCrafters.containsKey(chunk)) {
            chunkedCrafters.put(chunk, new HashSet<>());
        }
        chunkedCrafters.get(chunk).add(autocrafter);
        locationCrafters.put(autocrafter.crafter().getLocation(), autocrafter);
        crafters.add(autocrafter);
    }

    public void removeChunk(Chunk chunk) {
        Set<Autocrafter> autocrafters = chunkedCrafters.get(chunk);
        if (autocrafters == null) {
            return;
        }
        crafters.removeAll(autocrafters);
        chunkedCrafters.remove(chunk);
    }

    public void removeCrafter(Autocrafter autocrafter) {
        crafters.remove(autocrafter);
        Location location = autocrafter.crafter().getLocation();
        locationCrafters.remove(location);
        Set<Autocrafter> chunkCrafters = chunkedCrafters.get(location.getChunk());
        if (chunkCrafters == null) {
            return;
        }
        chunkCrafters.remove(autocrafter);
    }

    public void removeFrame(ItemFrame frame) {
        Block block = BlockUtil.getAttachedBlock(frame);
        Location location = block.getLocation();
        Autocrafter autocrafter = locationCrafters.get(location);
        if (autocrafter == null) {
            return;
        }
        removeCrafter(autocrafter);
    }

    public boolean isCrafterAt(Location location) {
        return locationCrafters.containsKey(location);
    }

    public Iterable<Autocrafter> getCrafters() {
        return crafters;
    }
}
