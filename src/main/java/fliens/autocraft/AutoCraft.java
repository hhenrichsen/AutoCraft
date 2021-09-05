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

import fliens.autocraft.listener.*;
import fliens.autocraft.util.InventoryUtil;
import fliens.autocraft.util.ParticleUtil;
import fliens.autocraft.util.RecipeUtil;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class AutoCraft extends JavaPlugin {
    boolean particles;
    String redstoneMode;
    long craftCooldown;

    private RecipeUtil recipeUtil;
    private CrafterStore store;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        craftCooldown = getConfig().getLong("craftCooldown");
        particles = getConfig().getBoolean("particles");
        redstoneMode = getConfig().getString("redstoneMode");
    }

    @Override
    public void onEnable() {
        recipeUtil = new RecipeUtil(this, getServer().recipeIterator());
        store = new CrafterStore();

        getServer().getPluginManager().registerEvents(new CrafterChunkLoadListener(this, store), this);
        getServer().getPluginManager().registerEvents(new CrafterChunkUnloadListener(this, store), this);
        getServer().getPluginManager().registerEvents(new ItemFrameCreateListener(store), this);
        getServer().getPluginManager().registerEvents(new ItemFrameDestroyListener(store), this);
        getServer().getPluginManager().registerEvents(new CancelMoveListener(store), this);

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            Iterable<Autocrafter> autoCrafters = store.getCrafters();

            for (final Autocrafter autocrafter : autoCrafters) {
                if (!redstoneMode.equalsIgnoreCase("disabled")) {
                    if ((redstoneMode.equalsIgnoreCase("indirect") && autocrafter.crafter().isBlockIndirectlyPowered()) || autocrafter.crafter().isBlockPowered()) {
                        continue;
                    }
                }
                autocrafter.handle(recipeUtil, particles);
            }
        }, 0L, craftCooldown);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}