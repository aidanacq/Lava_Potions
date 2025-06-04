package net.quoky.lava_potions.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionReplacementHandler {
    
    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        // Add our custom lava potion items to our custom tab
        if (event.getTabKey().location().toString().equals("lava_potions:lava_potions")) {
            // Add vanilla potion items with lava types to our custom tab
            for (Potion potion : ModPotionTypes.POTION_TYPES) {
                ItemStack vanillaPotionWithLavaType = VanillaPotionBrewingRecipes.createVanillaPotionWithLavaType(potion);
                event.accept(vanillaPotionWithLavaType);
            }
        }
        
        // For other tabs, we'll handle the replacement differently
        // Since we can't easily remove items from the event, we'll rely on the brewing system
        // to create the correct vanilla potion items with lava functionality
    }
} 