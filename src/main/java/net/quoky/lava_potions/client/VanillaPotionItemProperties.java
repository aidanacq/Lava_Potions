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
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

/**
 * Handles item properties for vanilla potion items to display lava potion textures
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VanillaPotionItemProperties {
    
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
        
        ItemProperties.register(Items.POTION, lavaTypeProperty, 
            (stack, level, entity, seed) -> getLavaTypeValue(stack));
        
        ItemProperties.register(Items.SPLASH_POTION, lavaTypeProperty, 
            (stack, level, entity, seed) -> getLavaTypeValue(stack));
        
        ItemProperties.register(Items.LINGERING_POTION, lavaTypeProperty, 
            (stack, level, entity, seed) -> getLavaTypeValue(stack));
    }
    
    private static float getLavaTypeValue(net.minecraft.world.item.ItemStack stack) {
        if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return 0.0F;
        }
        
        Potion potion = PotionUtils.getPotion(stack);
        if (potion == ModPotionTypes.LAVA_BOTTLE.get()) {
            return 1.0F;
        } else if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
            return 2.0F;
        }
        
        return 0.0F;
    }
} 