package fliens.autocraft.util;

import fliens.autocraft.Autocrafter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class BlockUtil {
    public static Autocrafter attemptCreateCrafter(ItemFrame itemFrame) {
        if (!(itemFrame).getItem().equals(new ItemStack(Material.CRAFTING_TABLE))) {
            return null;
        }
        return fromFrame(itemFrame);
    }

    public static Autocrafter attemptCreateCrafter(ItemFrame itemFrame, ItemStack checkItem) {
        if (checkItem == null) {
            return null;
        }
        if (itemFrame.getItem().getType() != Material.AIR || checkItem.getType() != Material.CRAFTING_TABLE) {
            return null;
        }
        return fromFrame(itemFrame);
    }

    private static Autocrafter fromFrame(ItemFrame itemFrame) {
        Block attachedBlock = getAttachedBlock(itemFrame);
        if (attachedBlock.getType() == Material.DISPENSER) {
            Dispenser dispenser = (Dispenser) attachedBlock.getBlockData();
            Location targetLocation = attachedBlock.getRelative(dispenser.getFacing()).getLocation();
            Location sourceLocation = attachedBlock.getRelative(dispenser.getFacing().getOppositeFace()).getLocation();
            return new Autocrafter(sourceLocation, targetLocation, attachedBlock, itemFrame);
        }
        return null;
    }

    public static Block getAttachedBlock(ItemFrame itemFrame) {
        return itemFrame.getLocation().getBlock().getRelative(itemFrame.getAttachedFace());
    }
}
