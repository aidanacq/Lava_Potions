package net.quoky.lava_potions.potion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.item.crafting.Ingredient;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.ModItems;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Centralized brewing recipe management for custom lava potions
 * Handles both recipe registration and utility methods for potion creation
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BrewingRecipes {

    // Brewing recipe constants
    public static final int STANDARD_POTION_AMOUNT = 250;
    public static final String LAVA_POTIONS_DATA_TAG = "lava_potions_data";
    public static final String LAVA_POTION_DATA_TAG = "LavaPotionData";

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BrewingRecipes::registerBrewingRecipes);
    }

    /**
     * Helper method to create a potion ItemStack
     */
    public static ItemStack createPotion(Potion potion) {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    /**
     * Creates a vanilla potion item with the specified lava potion type
     * with fixed durations that don't count down
     */
    public static ItemStack createVanillaPotionWithLavaType(Potion potionType) {
        ItemStack vanillaPotion = new ItemStack(Items.POTION);
        PotionUtils.setPotion(vanillaPotion, potionType);

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

        for (MobEffectInstance effect : PotionUtils.getCustomEffects(potionStack)) {
            customTag.putInt("original_duration_" + effect.getEffect().getDescriptionId(), effect.getDuration());
        }

        tag.put(LAVA_POTIONS_DATA_TAG, customTag);
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
     * Register all brewing recipes for custom lava potions
     */
    private static void registerBrewingRecipes() {
        Lava_Potions.LOGGER.info("Registering custom lava potion brewing recipes");

        try {
            // Base lava bottle to awkward lava recipe
            registerRecipe(
                    createPotion(ModPotionTypes.LAVA_BOTTLE.get()),
                    Items.NETHER_WART,
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()));

            // Obsidian Skin Potion (Awkward Lava Potion + Crying Obsidian)
            registerRecipe(
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()),
                    Items.CRYING_OBSIDIAN,
                    createPotion(ModPotionTypes.OBSIDIAN_SKIN.get()));

            // Extended Obsidian Skin Potion
            registerRecipe(
                    createPotion(ModPotionTypes.OBSIDIAN_SKIN.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.OBSIDIAN_SKIN_LONG.get()));

            // Netherite Skin Potion (Obsidian Skin Potion + Netherite Scrap)
            registerRecipe(
                    createPotion(ModPotionTypes.OBSIDIAN_SKIN.get()),
                    Items.NETHERITE_SCRAP,
                    createPotion(ModPotionTypes.NETHERITE_SKIN.get()));

            // Extended Netherite Skin Potion from Extended Obsidian Skin
            registerRecipe(
                    createPotion(ModPotionTypes.OBSIDIAN_SKIN_LONG.get()),
                    Items.NETHERITE_SCRAP,
                    createPotion(ModPotionTypes.NETHERITE_SKIN_LONG.get()));

            // Extended Netherite Skin Potion from regular Netherite Skin
            registerRecipe(
                    createPotion(ModPotionTypes.NETHERITE_SKIN.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.NETHERITE_SKIN_LONG.get()));

            // Glass Skin Potion (Netherite Skin Potion + Fermented Spider Eye)
            registerRecipe(
                    createPotion(ModPotionTypes.NETHERITE_SKIN.get()),
                    Items.FERMENTED_SPIDER_EYE,
                    createPotion(ModPotionTypes.GLASS_SKIN.get()));

            // Extended Glass Skin Potion (from extended Netherite Skin)
            registerRecipe(
                    createPotion(ModPotionTypes.NETHERITE_SKIN_LONG.get()),
                    Items.FERMENTED_SPIDER_EYE,
                    createPotion(ModPotionTypes.GLASS_SKIN_LONG.get()));

            // Extended Glass Skin Potion (from regular Glass Skin)
            registerRecipe(
                    createPotion(ModPotionTypes.GLASS_SKIN.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.GLASS_SKIN_LONG.get()));

            // Heat Potion (Awkward Lava Potion + Blaze Powder)
            registerRecipe(
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()),
                    Items.BLAZE_POWDER,
                    createPotion(ModPotionTypes.HEAT.get()));

            // Extended Heat Potion
            registerRecipe(
                    createPotion(ModPotionTypes.HEAT.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.HEAT_LONG.get()));

            // Strong Heat Potion
            registerRecipe(
                    createPotion(ModPotionTypes.HEAT.get()),
                    Items.GLOWSTONE_DUST,
                    createPotion(ModPotionTypes.HEAT_STRONG.get()));

            // Base Flammability Potion (Lava Bottle + Fermented Spider Eye)
            registerRecipe(
                    createPotion(ModPotionTypes.LAVA_BOTTLE.get()),
                    Items.FERMENTED_SPIDER_EYE,
                    createPotion(ModPotionTypes.FLAMMABILITY.get()));

            // Extended Flammability Potion
            registerRecipe(
                    createPotion(ModPotionTypes.FLAMMABILITY.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.FLAMMABILITY_LONG.get()));

            // Pyromancy Potion (Awkward Lava Potion + Fire Charge)
            registerRecipe(
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()),
                    Items.FIRE_CHARGE,
                    createPotion(ModPotionTypes.PYROMANCY.get()));

            // Extended Pyromancy Potion
            registerRecipe(
                    createPotion(ModPotionTypes.PYROMANCY.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.PYROMANCY_LONG.get()));

            // Strong Pyromancy Potion
            registerRecipe(
                    createPotion(ModPotionTypes.PYROMANCY.get()),
                    Items.GLOWSTONE_DUST,
                    createPotion(ModPotionTypes.PYROMANCY_STRONG.get()));

            // Magma Walker Potion (Awkward Lava Potion + Magma Cream)
            registerRecipe(
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()),
                    Items.MAGMA_CREAM,
                    createPotion(ModPotionTypes.MAGMA_WALKER.get()));

            // Extended Magma Walker Potion
            registerRecipe(
                    createPotion(ModPotionTypes.MAGMA_WALKER.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.MAGMA_WALKER_LONG.get()));

            // Strong Magma Walker Potion
            registerRecipe(
                    createPotion(ModPotionTypes.MAGMA_WALKER.get()),
                    Items.GLOWSTONE_DUST,
                    createPotion(ModPotionTypes.MAGMA_WALKER_STRONG.get()));

            // Lava Vision Potion (Awkward Lava Potion + Golden Carrot)
            registerRecipe(
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()),
                    Items.GOLDEN_CARROT,
                    createPotion(ModPotionTypes.LAVA_VISION.get()));

            // Extended Lava Vision Potion
            registerRecipe(
                    createPotion(ModPotionTypes.LAVA_VISION.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.LAVA_VISION_LONG.get()));

            // Lava Strider Potion (Awkward Lava Potion + Strider Hide)
            registerRecipe(
                    createPotion(ModPotionTypes.AWKWARD_LAVA.get()),
                    ModItems.STRIDER_HIDE.get(),
                    createPotion(ModPotionTypes.LAVA_STRIDER.get()));

            // Extended Lava Strider Potion
            registerRecipe(
                    createPotion(ModPotionTypes.LAVA_STRIDER.get()),
                    Items.REDSTONE,
                    createPotion(ModPotionTypes.LAVA_STRIDER_LONG.get()));

            // Strong Lava Strider Potion
            registerRecipe(
                    createPotion(ModPotionTypes.LAVA_STRIDER.get()),
                    Items.GLOWSTONE_DUST,
                    createPotion(ModPotionTypes.LAVA_STRIDER_STRONG.get()));

            Lava_Potions.LOGGER.info("Successfully registered custom lava potion brewing recipes");

        } catch (Exception e) {
            Lava_Potions.LOGGER.error("Failed to register custom lava potion brewing recipes", e);
        }
    }

    private static void registerRecipe(ItemStack input, Ingredient ingredient, ItemStack output) {
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.of(input), ingredient, output));
    }

    private static void registerRecipe(ItemStack input, net.minecraft.world.level.ItemLike ingredient,
            ItemStack output) {
        registerRecipe(input, Ingredient.of(ingredient), output);
    }
}
