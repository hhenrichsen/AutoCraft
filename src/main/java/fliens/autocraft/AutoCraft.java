/*  AutoCraft plugin
 *
 *  Copyright (C) 2021 Fliens
 *  Copyright (C) 2021 MrTransistor
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fliens.autocraft;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class AutoCraft extends JavaPlugin {

    boolean particles;
    String redstoneMode;
    long craftCooldown;

    private RecipeUtil recipeUtil;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("AutoCraft plugin started");

        saveDefaultConfig();
        craftCooldown = getConfig().getLong("craftCooldown");
        particles = getConfig().getBoolean("particles");
        redstoneMode = getConfig().getString("redstoneMode");

        new EventListener(this);
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            ArrayList<Block> autoCrafters = collectAutoCrafters();

            for (final Block autocrafter : autoCrafters) {
                if (!redstoneMode.equalsIgnoreCase("disabled")) { // redstone powering type check
                    if ((redstoneMode.equalsIgnoreCase("indirect") && autocrafter.isBlockIndirectlyPowered()) || autocrafter.isBlockPowered()) {
                        continue;
                    }
                }
                handleAutoCrafter(autocrafter);
            }
        }, 0L, craftCooldown); // configurable cooldown

        recipeUtil = new RecipeUtil(this, getServer().recipeIterator());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("AutoCraft plugin stopped");
    }

    /*
     * TODO: Switch to Event-based crafter collection during operation
     * Next method is too resource-heavy to execute every craft tick, so it eventually should be converted
     * to an Event-based system. We can check item frame manipulation events and add/remove crafters only when needed
     * thus removing the necessity to update crafter list every craft tick.
     */

    /**
     * This method returns an ArrayList of all autocrafters in the world
     *
     * @return ArrayList of autocrafters
     **/

    public static ArrayList<Block> collectAutoCrafters() {
        ArrayList<Block> autoCrafters = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) { // avixk`s fix
                if (entity.getType().equals(EntityType.ITEM_FRAME) || entity.getType().equals(EntityType.GLOW_ITEM_FRAME)) {
                    if (((ItemFrame) entity).getItem().equals(new ItemStack(Material.CRAFTING_TABLE))) {
                        Block autoCrafter = entity.getLocation().getBlock()
                                .getRelative(((ItemFrame) entity).getAttachedFace());
                        if (!autoCrafters.contains(autoCrafter)
                                && autoCrafter.getType().equals(Material.DISPENSER)) {
                            autoCrafters.add(autoCrafter);
                        }
                    }
                }
            }
        }
        return autoCrafters;
    }

    /**
     * Main method that updates a specified autocrafter
     *
     * @param autocrafter specified autocrafter
     **/

    private void handleAutoCrafter(Block autocrafter) {
        Dispenser dispenser = (Dispenser) autocrafter.getState();
        BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) autocrafter.getBlockData()).getFacing();
        BlockState source = new Location(autocrafter.getWorld(),
                autocrafter.getX() + targetFace.getOppositeFace().getModX(),
                autocrafter.getY() + targetFace.getOppositeFace().getModY(),
                autocrafter.getZ() + targetFace.getOppositeFace().getModZ()).getBlock().getState();
        BlockState destination = new Location(autocrafter.getWorld(), autocrafter.getX() + targetFace.getModX(),
                autocrafter.getY() + targetFace.getModY(), autocrafter.getZ() + targetFace.getModZ()).getBlock()
                .getState();

        if (source instanceof InventoryHolder && destination instanceof InventoryHolder) {
            Inventory sourceInv = ((InventoryHolder) source).getInventory();
            Inventory destinationInv = ((InventoryHolder) destination).getInventory();
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
                if (!sourceInv.containsAtLeast(i, i.getAmount()))
                    return;
            ItemStack result = recipeUtil.getCraftResult(crafterItems);
            if (result == null)
                return;

            ArrayList<ItemStack> output = new ArrayList<>(); // Making a list of items to add to the output container
            output.add(result.clone());
            output.addAll(itemsrem);

            if (!InventoryUtil.addItemsIfCan(destinationInv, output)) // Trying to add
                return;

            for (ItemStack item : itemstmp) {
                for (int i = 0; i < item.getAmount(); i++) {
                    for (ItemStack sourceItem : sourceInv.getContents()) {
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
                for (Location loc : ParticleUtil.getHollowCube(autocrafter.getLocation(), 0.05))
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 2, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.LIME, 0.2F));
        }
    }
}