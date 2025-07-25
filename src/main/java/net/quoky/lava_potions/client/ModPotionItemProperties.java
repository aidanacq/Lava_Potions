package net.quoky.lava_potions.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Handles item properties for vanilla potion items to display lava potion
 * textures
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModPotionItemProperties {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Lava_Potions.LOGGER.info("Registering lava potion item properties");
            registerItemProperties();
        });
    }

    /**
     * Register item properties for vanilla potion items
     */
    public static void registerItemProperties() {
        ResourceLocation lavaTypeProperty = ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "lava_type");
        ResourceLocation lavaContentsProperty = ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID,
                "lava_contents");

        ItemProperties.register(Items.POTION, lavaTypeProperty,
                (stack, level, entity, seed) -> getLavaTypeValue(stack));

        ItemProperties.register(Items.SPLASH_POTION, lavaTypeProperty,
                (stack, level, entity, seed) -> getLavaTypeValue(stack));

        ItemProperties.register(Items.LINGERING_POTION, lavaTypeProperty,
                (stack, level, entity, seed) -> getLavaTypeValue(stack));

        // Register lava contents property to determine which contents texture to use
        ItemProperties.register(Items.POTION, lavaContentsProperty,
                (stack, level, entity, seed) -> getLavaContentsValue(stack));

        ItemProperties.register(Items.SPLASH_POTION, lavaContentsProperty,
                (stack, level, entity, seed) -> getLavaContentsValue(stack));

        ItemProperties.register(Items.LINGERING_POTION, lavaContentsProperty,
                (stack, level, entity, seed) -> getLavaContentsValue(stack));
    }

    private static float getLavaTypeValue(net.minecraft.world.item.ItemStack stack) {
        if (!BrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return 0.0F;
        }

        Potion potion = PotionUtils.getPotion(stack);
        if (potion == ModPotionTypes.LAVA_BOTTLE.get()) {
            return 1.0F;
        } else if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
            return 2.0F;
        } else if (potion == ModPotionTypes.OBSIDIAN_SKIN.get() || potion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
            return 3.0F;
        } else if (potion == ModPotionTypes.NETHERITE_SKIN.get()
                || potion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
            return 4.0F;
        } else if (potion == ModPotionTypes.GLASS_SKIN.get() || potion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
            return 5.0F;
        } else if (potion == ModPotionTypes.FLAMMABILITY.get() || potion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
            return 6.0F;
        } else if (potion == ModPotionTypes.HEAT.get() || potion == ModPotionTypes.HEAT_LONG.get() ||
                potion == ModPotionTypes.HEAT_STRONG.get()) {
            return 7.0F;
        } else if (potion == ModPotionTypes.PYROMANCY.get() || potion == ModPotionTypes.PYROMANCY_LONG.get() ||
                potion == ModPotionTypes.PYROMANCY_STRONG.get()) {
            return 8.0F;
        } else if (potion == ModPotionTypes.MAGMA_WALKER.get() || potion == ModPotionTypes.MAGMA_WALKER_LONG.get() ||
                potion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
            return 9.0F;
        } else if (potion == ModPotionTypes.LAVA_VISION.get() || potion == ModPotionTypes.LAVA_VISION_LONG.get()) {
            return 10.0F;
        } else if (potion == ModPotionTypes.LAVA_STRIDER.get() || potion == ModPotionTypes.LAVA_STRIDER_LONG.get() ||
                potion == ModPotionTypes.LAVA_STRIDER_STRONG.get()) {
            return 11.0F;
        }

        return 0.0F;
    }

    private static float getLavaContentsValue(net.minecraft.world.item.ItemStack stack) {
        if (!BrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return 0.0F;
        }

        // All lava potions should use lava_contents texture with appropriate tinting
        if (ModPotionTypes.isLavaPotion(PotionUtils.getPotion(stack))) {
            return 1.0F;
        }

        return 0.0F;
    }
}