package net.quoky.lava_potions.potion;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.LavaPotionItem;
import net.quoky.lava_potions.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers all brewing recipes for lava potions
 */
public class BrewingRecipes {
    private static final Logger LOGGER = LoggerFactory.getLogger("BrewingRecipes");
    
    /**
     * Registers all brewing recipes
     */
    public static void registerBrewingRecipes() {
        LOGGER.info("Registering brewing recipes");
        
        try {
            // Register our custom brewing recipe for lava bottle to awkward lava potion
            LOGGER.info("Registering recipe: Lava Bottle + Nether Wart = Awkward Lava Potion");
            BrewingRecipeRegistry.addRecipe(new LavaBottleBrewingRecipe());
            
            // Register splash potion recipes
            LOGGER.info("Registering recipe: Lava Bottle + Gunpowder = Splash Lava Bottle");
            BrewingRecipeRegistry.addRecipe(new SplashLavaPotionRecipe(
                ModPotionTypes.LAVA_BOTTLE.get(), 
                ModPotionTypes.SPLASH_LAVA_BOTTLE.get()));
                
            LOGGER.info("Registering recipe: Awkward Lava Potion + Gunpowder = Splash Awkward Lava Potion");
            BrewingRecipeRegistry.addRecipe(new SplashLavaPotionRecipe(
                ModPotionTypes.AWKWARD_LAVA.get(), 
                ModPotionTypes.SPLASH_AWKWARD_LAVA.get()));
            
            // Register lingering potion recipes
            LOGGER.info("Registering recipe: Splash Lava Bottle + Dragon's Breath = Lingering Lava Bottle");
            BrewingRecipeRegistry.addRecipe(new LingeringLavaPotionRecipe(
                ModPotionTypes.SPLASH_LAVA_BOTTLE.get(), 
                ModPotionTypes.LINGERING_LAVA_BOTTLE.get()));
                
            LOGGER.info("Registering recipe: Splash Awkward Lava Potion + Dragon's Breath = Lingering Awkward Lava Potion");
            BrewingRecipeRegistry.addRecipe(new LingeringLavaPotionRecipe(
                ModPotionTypes.SPLASH_AWKWARD_LAVA.get(), 
                ModPotionTypes.LINGERING_AWKWARD_LAVA.get()));
            
            LOGGER.info("Brewing recipes registered successfully");
        } catch (Exception e) {
            LOGGER.error("Error registering brewing recipes", e);
        }
    }
    
    /**
     * Custom brewing recipe for lava bottle -> awkward lava potion
     * This prevents the awkward lava potion from brewing with nether wart again
     */
    private static class LavaBottleBrewingRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(ItemStack input) {
            // Only match if the input is a lava bottle
            if (input.getItem() != ModItems.LAVA_POTION.get()) {
                return false;
            }
            
            Potion potion = LavaPotionItem.getLavaPotionType(input);
            return potion == ModPotionTypes.LAVA_BOTTLE.get();
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            // Only match nether wart
            return ingredient.getItem() == Items.NETHER_WART;
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (isInput(input) && isIngredient(ingredient)) {
                return LavaPotionItem.getPotionItemStack(ModPotionTypes.AWKWARD_LAVA.get());
            }
            return ItemStack.EMPTY;
        }
    }
    
    /**
     * Custom brewing recipe for converting lava potions to splash variants
     */
    private static class SplashLavaPotionRecipe implements IBrewingRecipe {
        private final Potion inputPotion;
        private final Potion outputPotion;
        
        public SplashLavaPotionRecipe(Potion inputPotion, Potion outputPotion) {
            this.inputPotion = inputPotion;
            this.outputPotion = outputPotion;
        }
        
        @Override
        public boolean isInput(ItemStack input) {
            // Match if the input is the specified lava potion
            if (input.getItem() != ModItems.LAVA_POTION.get()) {
                return false;
            }
            
            Potion potion = LavaPotionItem.getLavaPotionType(input);
            return potion == inputPotion;
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            // Only match gunpowder
            return ingredient.getItem() == Items.GUNPOWDER;
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (isInput(input) && isIngredient(ingredient)) {
                return LavaPotionItem.getPotionItemStack(outputPotion);
            }
            return ItemStack.EMPTY;
        }
    }
    
    /**
     * Custom brewing recipe for converting splash lava potions to lingering variants
     */
    private static class LingeringLavaPotionRecipe implements IBrewingRecipe {
        private final Potion inputPotion;
        private final Potion outputPotion;
        
        public LingeringLavaPotionRecipe(Potion inputPotion, Potion outputPotion) {
            this.inputPotion = inputPotion;
            this.outputPotion = outputPotion;
        }
        
        @Override
        public boolean isInput(ItemStack input) {
            // Match if the input is the specified splash potion
            if (input.getItem() != ModItems.LAVA_POTION.get()) {
                return false;
            }
            
            Potion potion = LavaPotionItem.getLavaPotionType(input);
            return potion == inputPotion;
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            // Only match dragon's breath
            return ingredient.getItem() == Items.DRAGON_BREATH;
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (isInput(input) && isIngredient(ingredient)) {
                return LavaPotionItem.getPotionItemStack(outputPotion);
            }
            return ItemStack.EMPTY;
        }
    }
} 