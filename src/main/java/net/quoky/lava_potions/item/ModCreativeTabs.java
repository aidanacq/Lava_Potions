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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, Lava_Potions.MOD_ID);

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
                        // Lava Bottle - all variants
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()));
                        output.accept(
                                BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()));

                        // Awkward Lava - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.AWKWARD_LAVA.get()));

                        // Obsidian Skin - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.OBSIDIAN_SKIN_LONG.get()));

                        // Netherite Skin - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.NETHERITE_SKIN_LONG.get()));

                        // Glass Skin - all variants
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.GLASS_SKIN.get()));
                        output.accept(
                                BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.GLASS_SKIN.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.GLASS_SKIN.get()));
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.GLASS_SKIN_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.GLASS_SKIN_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.GLASS_SKIN_LONG.get()));

                        // Heat - all variants
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.HEAT.get()));
                        output.accept(BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.HEAT.get()));
                        output.accept(
                                BrewingRecipes.createVanillaLingeringPotionWithLavaType(ModPotionTypes.HEAT.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.HEAT_LONG.get()));
                        output.accept(
                                BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.HEAT_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.HEAT_LONG.get()));
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.HEAT_STRONG.get()));
                        output.accept(
                                BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.HEAT_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.HEAT_STRONG.get()));

                        // Flammability - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAMMABILITY.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.FLAMMABILITY.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.FLAMMABILITY.get()));
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.FLAMMABILITY_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.FLAMMABILITY_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.FLAMMABILITY_LONG.get()));

                        // Pyromancy - all variants
                        output.accept(BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.PYROMANCY.get()));
                        output.accept(
                                BrewingRecipes.createVanillaSplashPotionWithLavaType(ModPotionTypes.PYROMANCY.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.PYROMANCY.get()));
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.PYROMANCY_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.PYROMANCY_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.PYROMANCY_LONG.get()));
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.PYROMANCY_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.PYROMANCY_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.PYROMANCY_STRONG.get()));

                        // Magma Walker - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.MAGMA_WALKER.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.MAGMA_WALKER.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.MAGMA_WALKER.get()));
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.MAGMA_WALKER_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.MAGMA_WALKER_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.MAGMA_WALKER_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaPotionWithLavaType(ModPotionTypes.MAGMA_WALKER_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.MAGMA_WALKER_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.MAGMA_WALKER_STRONG.get()));

                        // Lava Vision - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_VISION.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_VISION.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_VISION.get()));

                        // Lava Vision Long - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_VISION_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_VISION_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_VISION_LONG.get()));

                        // Lava Strider - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_STRIDER.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_STRIDER.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_STRIDER.get()));

                        // Lava Strider Long - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_STRIDER_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_STRIDER_LONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_STRIDER_LONG.get()));

                        // Lava Strider Strong - all variants
                        output.accept(
                                BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_STRIDER_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaSplashPotionWithLavaType(ModPotionTypes.LAVA_STRIDER_STRONG.get()));
                        output.accept(BrewingRecipes
                                .createVanillaLingeringPotionWithLavaType(ModPotionTypes.LAVA_STRIDER_STRONG.get()));

                        // Items
                        output.accept(ModItems.STRIDER_HIDE.get());
                    })
                    .build());

    /**
     * Register all creative mode tabs
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}