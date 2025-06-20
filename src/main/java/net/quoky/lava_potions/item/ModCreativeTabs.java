package net.quoky.lava_potions.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Handles creative mode tabs for the mod
 */
public class ModCreativeTabs {
    // Create a Deferred Register for creative tabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Lava_Potions.MOD_ID);

    // Register a custom tab for lava potions
    public static final RegistryObject<CreativeModeTab> LAVA_POTIONS_TAB = CREATIVE_MODE_TABS.register("lava_potions",
            () -> CreativeModeTab.builder()
                    .icon(() -> {
                        ItemStack iconStack = new ItemStack(Items.POTION);
                        PotionUtils.setPotion(iconStack, ModPotionTypes.LAVA_BOTTLE.get());
                        return iconStack;
                    })
                    .title(Component.translatable("creativetab.lava_potions"))
                    .displayItems((parameters, output) -> {
                        // Add lava bottle
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()));
                        
                        // Add awkward lava
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get()));
                        
                        // Add all effect potions
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.GLASS_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.GLASS_SKIN_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAME_AURA.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAME_AURA_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAME_AURA_STRONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAMMABILITY.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAMMABILITY_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FIRE_AVATAR.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FIRE_AVATAR_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FIRE_AVATAR_STRONG.get()));
                        
                        // Add splash variants
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.GLASS_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.FLAME_AURA.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.FLAMMABILITY.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.FIRE_AVATAR.get()));
                        
                        // Add lingering variants
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.GLASS_SKIN.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.FLAME_AURA.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.FLAMMABILITY.get()));
                        output.accept(BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.FIRE_AVATAR.get()));
                    })
                    .build());

    /**
     * Register all creative mode tabs
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}