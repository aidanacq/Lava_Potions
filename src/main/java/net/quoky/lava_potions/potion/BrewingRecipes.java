package net.quoky.lava_potions.potion;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.item.crafting.Ingredient;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Registers brewing recipes for custom lava potions
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BrewingRecipes {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerBrewingRecipes();
        });
    }

    /**
     * Register all brewing recipes for custom lava potions
     */
    private static void registerBrewingRecipes() {
        Lava_Potions.LOGGER.info("Registering custom lava potion brewing recipes");
        
        try {
            // Obsidian Skin Potion (Awkward Lava Potion + Obsidian)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.AWKWARD_LAVA.get())),
                Ingredient.of(Items.CRYING_OBSIDIAN),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.OBSIDIAN_SKIN.get())
            );
            
            // Extended Obsidian Skin Potion
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.OBSIDIAN_SKIN.get())),
                Ingredient.of(Items.REDSTONE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.OBSIDIAN_SKIN_LONG.get())
            );
            
            // Netherite Skin Potion (Obsidian Skin Potion + Netherite Scrap)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.OBSIDIAN_SKIN.get())),
                Ingredient.of(Items.NETHERITE_SCRAP),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.NETHERITE_SKIN.get())
            );
            
            // Extended Netherite Skin Potion from Extended Obsidian Skin
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.OBSIDIAN_SKIN_LONG.get())),
                Ingredient.of(Items.NETHERITE_SCRAP),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.NETHERITE_SKIN_LONG.get())
            );
            
            // Glass Skin Potion (Netherite Skin Potion + Fermented Spider Eye)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.NETHERITE_SKIN.get())),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.GLASS_SKIN.get())
            );
            
            // Extended Glass Skin Potion (from extended Netherite Skin)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.NETHERITE_SKIN_LONG.get())),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.GLASS_SKIN_LONG.get())
            );
            
            // Extended Glass Skin Potion (from Glass Skin + Redstone)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.GLASS_SKIN.get())),
                Ingredient.of(Items.REDSTONE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.GLASS_SKIN_LONG.get())
            );
            

            
            // Flame Aura Potion (Awkward Lava Potion + Blaze Powder)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.AWKWARD_LAVA.get())),
                Ingredient.of(Items.BLAZE_POWDER),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAME_AURA.get())
            );
            
            // Extended Flame Aura Potion
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAME_AURA.get())),
                Ingredient.of(Items.REDSTONE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAME_AURA_LONG.get())
            );
            
            // Strong Flame Aura Potion
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAME_AURA.get())),
                Ingredient.of(Items.GLOWSTONE_DUST),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAME_AURA_STRONG.get())
            );
            
            // Base Flammability Potion (Lava Bottle + Fermented Spider Eye)
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.LAVA_BOTTLE.get())),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAMMABILITY.get())
            );
            
            // Extended Flammability Potion
            BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAMMABILITY.get())),
                Ingredient.of(Items.REDSTONE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotionTypes.FLAMMABILITY_LONG.get())
            );
            
            Lava_Potions.LOGGER.info("Custom lava potion brewing recipes registered successfully");
        } catch (Exception e) {
            Lava_Potions.LOGGER.error("Error registering custom lava potion brewing recipes", e);
        }
    }
} 