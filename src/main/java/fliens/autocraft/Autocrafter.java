package fliens.autocraft;

import fliens.autocraft.util.InventoryUtil;
import fliens.autocraft.util.ParticleUtil;
import fliens.autocraft.util.RecipeUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Autocrafter(Location source, Location destination, Block crafter, ItemFrame frame) {
    public void handle(RecipeUtil recipeUtil, boolean particles) {
        Dispenser dispenser = (Dispenser) this.crafter().getState();
        BlockState source = this.source().getBlock().getState();
        BlockState destination = this.destination().getBlock().getState();

        if (source instanceof InventoryHolder sourceHolder && destination instanceof InventoryHolder destinationHolder) {
            Inventory sourceInventory = sourceHolder.getInventory();
            Inventory destinationInventory = destinationHolder.getInventory();
            List<ItemStack> crafterItems = new ArrayList<>(Arrays.asList(dispenser.getInventory().getContents()));

            if (crafterItems.stream().noneMatch(Objects::nonNull)) // test if crafter is empty
                return;

            List<ItemStack> itemstmp = new ArrayList<>();
            List<ItemStack> itemsrem = new ArrayList<>();
            for (ItemStack item : crafterItems) {
                if (item != null) {
                    if (itemstmp.stream().noneMatch(i -> i.isSimilar(item))) {
                        int count = 0;
                        for (ItemStack jtem : crafterItems) {
                            if (jtem != null) {
                                if (jtem.isSimilar(item)) {
                                    count += 1;
                                }
                            }
                        }
                        ItemStack tmpitem = new ItemStack(item);
                        tmpitem.setAmount(count);
                        itemstmp.add(tmpitem);

                        Material remMat = item.getType().getCraftingRemainingItem(); // fix for lost bottles/buckets by avixk
                        if (remMat != null && !remMat.isAir()) {
                            itemsrem.add(new ItemStack(remMat, count));
                        }
                    }
                }
            }
            for (ItemStack i : itemstmp)
                if (!sourceInventory.containsAtLeast(i, i.getAmount()))
                    return;
            ItemStack result = recipeUtil.getCraftResult(crafterItems);
            if (result == null)
                return;

            ArrayList<ItemStack> output = new ArrayList<>(); // Making a list of items to add to the output container
            output.add(result.clone());
            output.addAll(itemsrem);

            if (!InventoryUtil.addItemsIfCan(destinationInventory, output))
                return;

            for (ItemStack item : itemstmp) {
                for (int i = 0; i < item.getAmount(); i++) {
                    for (ItemStack sourceItem : sourceInventory.getContents()) {
                        if (sourceItem != null) {
                            if (sourceItem.isSimilar(item)) {
                                sourceItem.setAmount(sourceItem.getAmount() - 1);
                                break;
                            }
                        }
                    }
                }
            }
            if (particles)
                for (Location loc : ParticleUtil.getHollowCube(this.crafter().getLocation(), 0.05))
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 2, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.LIME, 0.2F));
        }
    }
}
