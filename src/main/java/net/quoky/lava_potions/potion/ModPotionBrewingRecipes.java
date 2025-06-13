package net.quoky.lava_potions.potion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Registers brewing recipes that create vanilla potion items with lava potion types
 * Now works with only 2 base potion types - splash/lingering handled by vanilla system
 */
public class ModPotionBrewingRecipes {
    
    /**
     * Registers all brewing recipes that output vanilla potion items
     */
    public static void registerVanillaPotionBrewingRecipes() {
        Lava_Potions.LOGGER.info("Registering lava potion brewing recipes");
        
        try {
            BrewingRecipeRegistry.addRecipe(new LavaBottleToVanillaPotionRecipe());
            Lava_Potions.LOGGER.info("Lava potion brewing recipes registered successfully");
        } catch (Exception e) {
            Lava_Potions.LOGGER.error("Error registering lava potion brewing recipes", e);
        }
    }
    
    /**
     * Creates a vanilla potion item with the specified lava potion type
     * with fixed durations that don't count down
     */
    public static ItemStack createVanillaPotionWithLavaType(Potion potionType) {
        ItemStack vanillaPotion = new ItemStack(Items.POTION);
        PotionUtils.setPotion(vanillaPotion, potionType);
        
        // For effect potions (excluding basic lava potions), store original durations
        if (ModPotionTypes.isEffectLavaPotion(potionType)) {
            preserveEffectDurations(vanillaPotion);
        }
        
        return vanillaPotion;
    }
    
    /**
     * Creates a vanilla splash potion item with the specified lava potion type
     */
    public static ItemStack createVanillaSplashPotionWithLavaType(Potion potionType) {
        ItemStack vanillaSplashPotion = new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setPotion(vanillaSplashPotion, potionType);
        
        // For effect potions (excluding basic lava potions), store original durations
        if (ModPotionTypes.isEffectLavaPotion(potionType)) {
            preserveEffectDurations(vanillaSplashPotion);
        }
        
        return vanillaSplashPotion;
    }
    
    /**
     * Creates a vanilla lingering potion item with the specified lava potion type
     */
    public static ItemStack createVanillaLingeringPotionWithLavaType(Potion potionType) {
        ItemStack vanillaLingeringPotion = new ItemStack(Items.LINGERING_POTION);
        PotionUtils.setPotion(vanillaLingeringPotion, potionType);
        
        // For effect potions (excluding basic lava potions), store original durations
        if (ModPotionTypes.isEffectLavaPotion(potionType)) {
            preserveEffectDurations(vanillaLingeringPotion);
        }
        
        return vanillaLingeringPotion;
    }
    
    /**
     * Helper method to preserve original effect durations in the potion's NBT data
     * This prevents the countdown effect on item tooltips
     */
    private static void preserveEffectDurations(ItemStack potionStack) {
        CompoundTag tag = potionStack.getOrCreateTag();
        CompoundTag customTag = new CompoundTag();
        
        // Get all effects from the potion
        for (MobEffectInstance effect : PotionUtils.getCustomEffects(potionStack)) {
            // Store the original duration for each effect
            customTag.putInt("original_duration_" + effect.getEffect().getDescriptionId(), effect.getDuration());
        }
        
        // Store custom data
        tag.put("lava_potions_data", customTag);
        potionStack.setTag(tag);
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
            if (!isVanillaPotionWithLavaType(input)) {
                return false;
            }
            
            Potion potion = PotionUtils.getPotion(input);
            return potion == ModPotionTypes.LAVA_BOTTLE.get();
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
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