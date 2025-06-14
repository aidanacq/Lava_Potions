package net.quoky.lava_potions.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.effect.ModEffects;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModPotionTypes {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Lava_Potions.MOD_ID);
    
    public static final RegistryObject<Potion> LAVA_BOTTLE = POTIONS.register("lava_bottle", Potion::new);
    public static final RegistryObject<Potion> AWKWARD_LAVA = POTIONS.register("awkward_lava", Potion::new);
    
    // Register obsidian skin potion (2 minutes normal, 4 minutes extended)
    public static final RegistryObject<Potion> OBSIDIAN_SKIN = POTIONS.register("obsidian_skin", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.OBSIDIAN_SKIN.get(), 2400),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400, 0),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400),
            new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2400, 0)
            
        ));
    
    public static final RegistryObject<Potion> OBSIDIAN_SKIN_LONG = POTIONS.register("obsidian_skin_long", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.OBSIDIAN_SKIN.get(), 4800),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, 0),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800),
            new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 4800, 0)
            
        ));
    
    // Register netherite skin potion (45 seconds normal, 1:30 minutes extended)
    public static final RegistryObject<Potion> NETHERITE_SKIN = POTIONS.register("netherite_skin", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.NETHERITE_SKIN.get(), 900),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900, 3),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900),
            new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 900, 0)
        ));
    
    public static final RegistryObject<Potion> NETHERITE_SKIN_LONG = POTIONS.register("netherite_skin_long", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.NETHERITE_SKIN.get(), 1800),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1800, 3),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800),
            new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1800, 0)
        ));
        
    // Register glass skin potion
    public static final RegistryObject<Potion> GLASS_SKIN = POTIONS.register("glass_skin", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.GLASS_SKIN.get(), 3600),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)
        ));
    
    public static final RegistryObject<Potion> GLASS_SKIN_STRONG = POTIONS.register("glass_skin_strong", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.GLASS_SKIN.get(), 1800, 1),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800)
        ));
    
    public static final RegistryObject<Potion> GLASS_SKIN_LONG = POTIONS.register("glass_skin_long", 
        () -> new Potion(
            new MobEffectInstance(ModEffects.GLASS_SKIN.get(), 9600),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)
        ));
    
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
        return isBaseLavaBottle(potion) || isAwkwardLava(potion) || 
               potion == OBSIDIAN_SKIN.get() || potion == OBSIDIAN_SKIN_LONG.get() || 
               potion == NETHERITE_SKIN.get() || potion == NETHERITE_SKIN_LONG.get() || 
               potion == GLASS_SKIN.get() || potion == GLASS_SKIN_STRONG.get() || potion == GLASS_SKIN_LONG.get();
    }
    
    public static boolean isBasicLavaPotion(Potion potion) {
        return isBaseLavaBottle(potion) || isAwkwardLava(potion);
    }
    
    public static boolean isEffectLavaPotion(Potion potion) {
        return isLavaPotion(potion) && !isBasicLavaPotion(potion);
    }
    
    private static void initPotionTypes() {
        POTION_TYPES.add(LAVA_BOTTLE.get());
        POTION_TYPES.add(AWKWARD_LAVA.get());
        
        // Add all the obsidian skin potions
        POTION_TYPES.add(OBSIDIAN_SKIN.get());
        POTION_TYPES.add(OBSIDIAN_SKIN_LONG.get());
        
        // Add all the netherite skin potions
        POTION_TYPES.add(NETHERITE_SKIN.get());
        POTION_TYPES.add(NETHERITE_SKIN_LONG.get());
        
        // Add all the glass skin potions
        POTION_TYPES.add(GLASS_SKIN.get());
        POTION_TYPES.add(GLASS_SKIN_STRONG.get());
        POTION_TYPES.add(GLASS_SKIN_LONG.get());
        
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