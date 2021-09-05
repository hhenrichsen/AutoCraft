package fliens.autocraft.util;

import fliens.autocraft.Position;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.StreamSupport;

public class RecipeUtil {
    private final Map<List<ItemStack>, ItemStack> cache = new HashMap<>();
    ArrayList<Recipe> recipes = new ArrayList<>();
    private final JavaPlugin plugin;

    public RecipeUtil(JavaPlugin plugin, Iterator<Recipe> recipes) {
        this.plugin = plugin;
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(recipes, Spliterator.ORDERED), true)
            .filter(this::shouldAddRecipe)
            .forEach(this.recipes::add);
        addExtraRecipes();
    }

    private boolean shouldAddRecipe(Recipe recipe) {
        return recipe.getResult().getType() != Material.FIREWORK_ROCKET;
    }

    /**
     * This method returns an ArrayList of extra recipes
     **/

    private void addExtraRecipes() {
        ItemStack rocket_1 = new ItemStack(Material.FIREWORK_ROCKET, 3);
        FireworkMeta rocket_1_meta = (FireworkMeta) rocket_1.getItemMeta();
        rocket_1_meta.setPower(1);
        rocket_1.setItemMeta(rocket_1_meta);
        ShapelessRecipe rocket_1_recipe = new ShapelessRecipe(new NamespacedKey(plugin, "fliens.autocraft.firework_rocket_1"), rocket_1);
        rocket_1_recipe.addIngredient(Material.PAPER);
        rocket_1_recipe.addIngredient(1, Material.GUNPOWDER);

        ItemStack rocket_2 = new ItemStack(Material.FIREWORK_ROCKET, 3);
        FireworkMeta rocket_2_meta = (FireworkMeta) rocket_2.getItemMeta();
        rocket_2_meta.setPower(2);
        rocket_2.setItemMeta(rocket_2_meta);
        ShapelessRecipe rocket_2_recipe = new ShapelessRecipe(new NamespacedKey(plugin, "fliens.autocraft.firework_rocket_2"), rocket_2);
        rocket_2_recipe.addIngredient(Material.PAPER);
        rocket_2_recipe.addIngredient(2, Material.GUNPOWDER);

        ItemStack rocket_3 = new ItemStack(Material.FIREWORK_ROCKET, 3);
        FireworkMeta rocket_3_meta = (FireworkMeta) rocket_3.getItemMeta();
        rocket_3_meta.setPower(3);
        rocket_3.setItemMeta(rocket_3_meta);
        ShapelessRecipe rocket_3_recipe = new ShapelessRecipe(new NamespacedKey(plugin, "fliens.autocraft.firework_rocket_3"), rocket_3);
        rocket_3_recipe.addIngredient(Material.PAPER);
        rocket_3_recipe.addIngredient(3, Material.GUNPOWDER);

        recipes.add(rocket_1_recipe);
        recipes.add(rocket_2_recipe);
        recipes.add(rocket_3_recipe);
    }

    /**
     * This method returns a craft result for a list of input items
     *
     * @param items an ArrayList of input items
     * @return crafting result (ItemStack)
     **/
    public ItemStack getCraftResult(List<ItemStack> items) {
        if (cache.containsKey(items))
            return cache.get(items);

        if (items.size() != 9) { // list correct?
            return null;
        }
        boolean notNull = false;
        for (ItemStack itemstack : items) {
            if (itemstack != null) {
                notNull = true;
                break;
            }
        }
        if (!notNull) {
            return null;
        }

        ItemStack result;
        for (Recipe recipe : recipes) {
            if (recipe instanceof ShapelessRecipe) { // shapeless recipe
                result = matchesShapeless(((ShapelessRecipe) recipe).getChoiceList(), items) ? recipe.getResult()
                        : null;
                if (result != null) {
                    cache.put(items, result);
                    return result;
                }
            } else if (recipe instanceof ShapedRecipe) { // shaped recipe
                result = matchesShaped((ShapedRecipe) recipe, items) ? recipe.getResult() : null;
                if (result != null) {
                    cache.put(items, result);
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * This method checks if a list of items matches a specified shapeless recipe
     *
     * @param choice specified recipe
     * @param items  a list of items to check
     * @return true if recipe matches, false if not
     **/

    private boolean matchesShapeless(List<RecipeChoice> choice, List<ItemStack> items) {
        items = new ArrayList<>(items);
        for (RecipeChoice c : choice) {
            boolean match = false;
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item == null || item.getType() == Material.AIR)
                    continue;
                if (c.test(item)) {
                    match = true;
                    items.remove(item);
                    break;
                }
            }
            if (!match)
                return false;
        }
        items.removeAll(Arrays.asList(null, new ItemStack(Material.AIR)));
        return items.size() == 0;
    }

    /**
     * This method checks if a list of items matches a specified shaped recipe
     *
     * @param recipe specified recipe
     * @param items  a list of items to check
     * @return true if recipe matches, false if not
     **/

    private boolean matchesShaped(ShapedRecipe recipe, List<ItemStack> items) {
        RecipeChoice[][] recipeArray = new RecipeChoice[recipe.getShape().length][recipe.getShape()[0].length()];
        for (int i = 0; i < recipe.getShape().length; i++) {
            for (int j = 0; j < recipe.getShape()[i].length(); j++) {
                recipeArray[i][j] = recipe.getChoiceMap().get(recipe.getShape()[i].toCharArray()[j]);
            }
        }

        int counter = 0;
        ItemStack[][] itemsArray = new ItemStack[3][3];
        for (int i = 0; i < itemsArray.length; i++) {
            for (int j = 0; j < itemsArray[i].length; j++) {
                itemsArray[i][j] = items.get(counter);
                counter++;
            }
        }

        // itemsArray manipulation
        Object[][] tmpArray = reduceArray(itemsArray);
        itemsArray = new ItemStack[tmpArray.length][tmpArray[0].length];
        for (int i = 0; i < tmpArray.length; i++) {
            for (int j = 0; j < tmpArray[i].length; j++) {
                itemsArray[i][j] = (ItemStack) tmpArray[i][j];
            }
        }
        ItemStack[][] itemsArrayMirrored = new ItemStack[itemsArray.length][itemsArray[0].length];
        for (int i = 0; i < itemsArray.length; i++) {
            int jPos = 0;
            for (int j = itemsArray[i].length - 1; j >= 0; j--) {
                itemsArrayMirrored[i][jPos] = itemsArray[i][j];
                jPos++;
            }
        }
        return match(itemsArray, recipeArray) || match(itemsArrayMirrored, recipeArray);
    }

    private boolean match(ItemStack[][] itemsArray, RecipeChoice[][] recipeArray) {
        boolean match = true;
        if (itemsArray.length == recipeArray.length && itemsArray[0].length == recipeArray[0].length) {
            for (int i = 0; i < recipeArray.length; i++) {
                for (int j = 0; j < recipeArray[0].length; j++) {
                    if (recipeArray[i][j] != null && itemsArray[i][j] != null) {
                        if (!recipeArray[i][j].test(itemsArray[i][j])) {
                            match = false;
                            break;
                        }
                    } else if ((recipeArray[i][j] == null && itemsArray[i][j] != null)
                            || (recipeArray[i][j] != null && itemsArray[i][j] == null)) {
                        match = false;
                        break;
                    }
                }
            }
            return match;
        }
        return false;
    }

    private static Object[][] reduceArray(Object[][] array) {
        ArrayList<Position> positions = new ArrayList<>();
        for (int y = 0; y < array.length; y++)
            for (int x = 0; x < array[y].length; x++) {
                if (array[y][x] != null)
                    positions.add(new Position(x, y));
            }

        Position upperLeft = new Position(array.length - 1, array[0].length - 1);
        Position lowerRight = new Position(0, 0);
        for (Position pos : positions) {
            if (pos.y < upperLeft.y)
                upperLeft.y = pos.y;
            if (pos.x < upperLeft.x)
                upperLeft.x = pos.x;
            if (pos.y > lowerRight.y)
                lowerRight.y = pos.y;
            if (pos.x > lowerRight.x)
                lowerRight.x = pos.x;
        }
        Object[][] clean = new Object[(lowerRight.y - upperLeft.y) + 1][(lowerRight.x - upperLeft.x) + 1];
        int cleanyY = 0;
        for (int y = upperLeft.y; y < lowerRight.y + 1; y++) {
            int cleanxX = 0;
            for (int x = upperLeft.x; x < lowerRight.x + 1; x++) {
                clean[cleanyY][cleanxX] = array[y][x];
                cleanxX++;
            }
            cleanyY++;
        }
        return clean;
    }
}
