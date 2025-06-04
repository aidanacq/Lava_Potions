package net.quoky.lava_potions.potion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

/**
 * Registers brewing recipes that create vanilla potion items with lava potion types
 * Now works with only 2 base potion types - splash/lingering handled by vanilla system
 */
public class VanillaPotionBrewingRecipes {
    private static final Logger LOGGER = LoggerFactory.getLogger("VanillaPotionBrewingRecipes");
    
    /**
     * Registers all brewing recipes that output vanilla potion items
     */
    public static void registerVanillaPotionBrewingRecipes() {
        LOGGER.info("Registering vanilla potion brewing recipes for lava potions (2 base types only)");
        
        try {
            // Base brewing recipe: Lava Bottle + Nether Wart = Awkward Lava Potion
            registerLavaBottleToAwkwardRecipe();
            
            LOGGER.info("Vanilla potion brewing recipes registered successfully");
            LOGGER.info("Splash and lingering variants will be handled by vanilla brewing system");
        } catch (Exception e) {
            LOGGER.error("Error registering vanilla potion brewing recipes", e);
        }
    }
    
    /**
     * Register recipe for Lava Bottle + Nether Wart = Vanilla Awkward Lava Potion
     */
    private static void registerLavaBottleToAwkwardRecipe() {
        LOGGER.info("Registering recipe: Lava Bottle + Nether Wart = Vanilla Awkward Lava Potion");
        BrewingRecipeRegistry.addRecipe(new LavaBottleToVanillaPotionRecipe());
    }
    
    /**
     * Creates a vanilla potion item with the specified lava potion type
     */
    public static ItemStack createVanillaPotionWithLavaType(Potion potionType) {
        ItemStack vanillaPotion = new ItemStack(Items.POTION);
        
        // Set the potion type using vanilla's system
        PotionUtils.setPotion(vanillaPotion, potionType);
        
        // Add custom NBT to mark this as a lava potion
        CompoundTag tag = vanillaPotion.getOrCreateTag();
        tag.putBoolean("IsLavaPotion", true);
        
        return vanillaPotion;
    }
    
    /**
     * Creates a vanilla splash potion item with the specified lava potion type
     */
    public static ItemStack createVanillaSplashPotionWithLavaType(Potion potionType) {
        ItemStack vanillaSplashPotion = new ItemStack(Items.SPLASH_POTION);
        
        // Set the potion type using vanilla's system
        PotionUtils.setPotion(vanillaSplashPotion, potionType);
        
        // Add custom NBT to mark this as a lava potion
        CompoundTag tag = vanillaSplashPotion.getOrCreateTag();
        tag.putBoolean("IsLavaPotion", true);
        
        return vanillaSplashPotion;
    }
    
    /**
     * Creates a vanilla lingering potion item with the specified lava potion type
     */
    public static ItemStack createVanillaLingeringPotionWithLavaType(Potion potionType) {
        ItemStack vanillaLingeringPotion = new ItemStack(Items.LINGERING_POTION);
        
        // Set the potion type using vanilla's system
        PotionUtils.setPotion(vanillaLingeringPotion, potionType);
        
        // Add custom NBT to mark this as a lava potion
        CompoundTag tag = vanillaLingeringPotion.getOrCreateTag();
        tag.putBoolean("IsLavaPotion", true);
        
        return vanillaLingeringPotion;
    }
    
    /**
     * Checks if a vanilla potion item contains a lava potion type
     */
    public static boolean isVanillaPotionWithLavaType(ItemStack stack) {
        if (stack.getItem() != Items.POTION && 
            stack.getItem() != Items.SPLASH_POTION && 
            stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }
        
        // Check if it has our custom NBT tag
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean("IsLavaPotion")) {
            return true;
        }
        
        // Also check if the potion type is one of ours
        Potion potion = PotionUtils.getPotion(stack);
        return ModPotionTypes.isLavaPotion(potion);
    }
    
    /**
     * Checks if a vanilla potion item is a splash variant with lava type
     */
    public static boolean isVanillaSplashPotionWithLavaType(ItemStack stack) {
        return stack.getItem() == Items.SPLASH_POTION && isVanillaPotionWithLavaType(stack);
    }
    
    /**
     * Checks if a vanilla potion item is a lingering variant with lava type
     */
    public static boolean isVanillaLingeringPotionWithLavaType(ItemStack stack) {
        return stack.getItem() == Items.LINGERING_POTION && isVanillaPotionWithLavaType(stack);
    }
    
    /**
     * Custom brewing recipe for lava bottle -> vanilla awkward lava potion
     */
    private static class LavaBottleToVanillaPotionRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(ItemStack input) {
            // Only match if the input is a vanilla potion with lava bottle type
            if (!isVanillaPotionWithLavaType(input)) {
                return false;
            }
            
            Potion potion = PotionUtils.getPotion(input);
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
                return createVanillaPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get());
            }
            return ItemStack.EMPTY;
        }
    }
} 