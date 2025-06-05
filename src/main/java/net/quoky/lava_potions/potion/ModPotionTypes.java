package net.quoky.lava_potions.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModPotionTypes {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Lava_Potions.MOD_ID);
    
    public static final RegistryObject<Potion> LAVA_BOTTLE = POTIONS.register("lava_bottle", Potion::new);
    public static final RegistryObject<Potion> AWKWARD_LAVA = POTIONS.register("awkward_lava", Potion::new);
    
    public static final List<Potion> POTION_TYPES = new ArrayList<>();
    
    public static Potion getPotionTypeFromId(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation != null) {
            Potion potion = ForgeRegistries.POTIONS.getValue(resourceLocation);
            if (potion != null) {
                return potion;
            }
        }
        return LAVA_BOTTLE.get();
    }
    
    public static String getPotionTypeId(Potion potion) {
        ResourceLocation resourceLocation = ForgeRegistries.POTIONS.getKey(potion);
        return resourceLocation != null ? resourceLocation.toString() : Lava_Potions.MOD_ID + ":lava_bottle";
    }
    
    public static boolean isBaseLavaBottle(Potion potion) {
        return potion == LAVA_BOTTLE.get();
    }
    
    public static boolean isAwkwardLava(Potion potion) {
        return potion == AWKWARD_LAVA.get();
    }
    
    public static boolean isLavaPotion(Potion potion) {
        return isBaseLavaBottle(potion) || isAwkwardLava(potion);
    }
    
    private static void initPotionTypes() {
        POTION_TYPES.add(LAVA_BOTTLE.get());
        POTION_TYPES.add(AWKWARD_LAVA.get());
        Lava_Potions.LOGGER.info("Initialized {} lava potion types", POTION_TYPES.size());
    }
    
    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModPotionTypes::initPotionTypes);
    }
} 