package fliens.autocraft.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryUtil {

    /**
     * This method adds items to an inventory
     *
     * @param inventory inventory to add items to
     * @param items     an ArrayList of items to add
     * @return true if items were added, false if items weren't added because some of them didn't fit
     **/
    public static boolean addItemsIfCan(Inventory inventory, ArrayList<ItemStack> items) { // I think this needs explanation
        ItemStack[] mutableCont = inventory.getContents();
        ItemStack[] contents = getContents(mutableCont);
        boolean willFit = hasSpace(inventory, items);
        if (!willFit) {
            inventory.setStorageContents(contents);
        }
        return willFit;
    }

    /**
     * This method is similar to addItemsifCan() with only one difference - it doesn't add items
     *
     * @param inventory inventory to add items to
     * @param items     an ArrayList of items to add
     * @return true if items can fit into the inventory, false if not
     * @see InventoryUtil#addItemsIfCan(Inventory inventory, ArrayList items)
     **/
    public static boolean testIfCanFit(Inventory inventory, ArrayList<ItemStack> items) {
        ItemStack[] mutableCont = inventory.getContents();
        ItemStack[] contents = getContents(mutableCont);
        boolean willFit = hasSpace(inventory, items);
        inventory.setStorageContents(contents);
        return willFit;
    }

    private static ItemStack[] getContents(ItemStack[] mutableContents) {
        ItemStack[] contents = new ItemStack[mutableContents.length];
        for (int i = 0; i < mutableContents.length; i++) {
            if (mutableContents[i] != null) {
                contents[i] = mutableContents[i];
            }
        }
        return contents;
    }

    private static boolean hasSpace(Inventory inventory, List<ItemStack> items) {
        for (ItemStack item : items) {
            Map<Integer, ItemStack> left = inventory.addItem(item.clone());
            if (!left.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
