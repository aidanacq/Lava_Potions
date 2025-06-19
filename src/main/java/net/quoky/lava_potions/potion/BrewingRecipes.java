package net.quoky.lava_potions.potion;

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
     * Helper method to create a potion ItemStack
     */
    public static ItemStack createPotion(Potion potion) {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    /**
     * Register all brewing recipes for custom lava potions
     */
    private static void registerBrewingRecipes() {
        Lava_Potions.LOGGER.info("Registering custom lava potion brewing recipes");
        
        try {
            // First register the base lava bottle to awkward lava recipe
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.LAVA_BOTTLE.get())),
                Ingredient.of(Items.NETHER_WART),
                createPotion(ModPotionTypes.AWKWARD_LAVA.get())
            ));
            
            // Register all effect potion recipes using ProperBrewingRecipe for Create compatibility
            // Obsidian Skin Potion (Awkward Lava Potion + Crying Obsidian)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.AWKWARD_LAVA.get())),
                Ingredient.of(Items.CRYING_OBSIDIAN),
                createPotion(ModPotionTypes.OBSIDIAN_SKIN.get())
            ));
            
            // Extended Obsidian Skin Potion
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.OBSIDIAN_SKIN.get())),
                Ingredient.of(Items.REDSTONE),
                createPotion(ModPotionTypes.OBSIDIAN_SKIN_LONG.get())
            ));
            
            // Netherite Skin Potion (Obsidian Skin Potion + Netherite Scrap)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.OBSIDIAN_SKIN.get())),
                Ingredient.of(Items.NETHERITE_SCRAP),
                createPotion(ModPotionTypes.NETHERITE_SKIN.get())
            ));
            
            // Extended Netherite Skin Potion from Extended Obsidian Skin
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.OBSIDIAN_SKIN_LONG.get())),
                Ingredient.of(Items.NETHERITE_SCRAP),
                createPotion(ModPotionTypes.NETHERITE_SKIN_LONG.get())
            ));
            
            // Glass Skin Potion (Netherite Skin Potion + Fermented Spider Eye)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.NETHERITE_SKIN.get())),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                createPotion(ModPotionTypes.GLASS_SKIN.get())
            ));
            
            // Extended Glass Skin Potion (from extended Netherite Skin)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.NETHERITE_SKIN_LONG.get())),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                createPotion(ModPotionTypes.GLASS_SKIN_LONG.get())
            ));
            
            // Extended Glass Skin Potion (from Glass Skin + Redstone)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.GLASS_SKIN.get())),
                Ingredient.of(Items.REDSTONE),
                createPotion(ModPotionTypes.GLASS_SKIN_LONG.get())
            ));
            
            // Flame Aura Potion (Awkward Lava Potion + Blaze Powder)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.AWKWARD_LAVA.get())),
                Ingredient.of(Items.BLAZE_POWDER),
                createPotion(ModPotionTypes.FLAME_AURA.get())
            ));
            
            // Extended Flame Aura Potion
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.FLAME_AURA.get())),
                Ingredient.of(Items.REDSTONE),
                createPotion(ModPotionTypes.FLAME_AURA_LONG.get())
            ));
            
            // Strong Flame Aura Potion
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.FLAME_AURA.get())),
                Ingredient.of(Items.GLOWSTONE_DUST),
                createPotion(ModPotionTypes.FLAME_AURA_STRONG.get())
            ));
            
            // Base Flammability Potion (Lava Bottle + Fermented Spider Eye)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.LAVA_BOTTLE.get())),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                createPotion(ModPotionTypes.FLAMMABILITY.get())
            ));
            
            // Extended Flammability Potion
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.FLAMMABILITY.get())),
                Ingredient.of(Items.REDSTONE),
                createPotion(ModPotionTypes.FLAMMABILITY_LONG.get())
            ));
            
            // Fire Avatar Potion (Awkward Lava Potion + Fire Charge)
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.AWKWARD_LAVA.get())),
                Ingredient.of(Items.FIRE_CHARGE),
                createPotion(ModPotionTypes.FIRE_AVATAR.get())
            ));
            
            // Extended Fire Avatar Potion
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.FIRE_AVATAR.get())),
                Ingredient.of(Items.REDSTONE),
                createPotion(ModPotionTypes.FIRE_AVATAR_LONG.get())
            ));
            
            // Strong Fire Avatar Potion
            BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(
                Ingredient.of(createPotion(ModPotionTypes.FIRE_AVATAR.get())),
                Ingredient.of(Items.GLOWSTONE_DUST),
                createPotion(ModPotionTypes.FIRE_AVATAR_STRONG.get())
            ));
            
            Lava_Potions.LOGGER.info("Custom lava potion brewing recipes registered successfully");
        } catch (Exception e) {
            Lava_Potions.LOGGER.error("Error registering custom lava potion brewing recipes", e);
        }
    }
} 