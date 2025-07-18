package net.quoky.lava_potions.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.ModItems;
import net.quoky.lava_potions.item.PotionBagItem;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionBagItemProperties {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(PotionBagItemProperties::registerItemProperties);
    }

    public static void registerItemProperties() {
        // Register the open/closed property
        ResourceLocation isOpenProperty = ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "is_open");
        ItemProperties.register(ModItems.POTION_BAG.get(), isOpenProperty,
                (stack, level, entity, seed) -> PotionBagItem.isOpen(stack) ? 1.0F : 0.0F);

        // Register the slot mask property for open bags
        ResourceLocation slotMaskProperty = ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "slot_mask");
        ItemProperties.register(ModItems.POTION_BAG.get(), slotMaskProperty,
                (stack, level, entity, seed) -> PotionBagItem.isOpen(stack) ? (float) PotionBagItem.getSlotMask(stack) : 0.0F);
    }
} 