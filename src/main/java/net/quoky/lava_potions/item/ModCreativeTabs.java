package net.quoky.lava_potions.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

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
                    .icon(() -> VanillaPotionBrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()))
                    .title(Component.translatable("creativetab.lava_potions"))
                    .displayItems((parameters, output) -> {
                        // Add vanilla potion items with lava types (regular, splash, lingering)
                        for (var potion : ModPotionTypes.POTION_TYPES) {
                            // Regular potion
                            output.accept(VanillaPotionBrewingRecipes.createVanillaPotionWithLavaType(potion));
                            // Splash potion
                            output.accept(VanillaPotionBrewingRecipes.createVanillaSplashPotionWithLavaType(potion));
                            // Lingering potion
                            output.accept(VanillaPotionBrewingRecipes.createVanillaLingeringPotionWithLavaType(potion));
                        }
                    })
                    .build());

    /**
     * Register all creative mode tabs
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}