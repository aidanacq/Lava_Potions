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
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS,
            Lava_Potions.MOD_ID);

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
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 900, 0)));

    public static final RegistryObject<Potion> NETHERITE_SKIN_LONG = POTIONS.register("netherite_skin_long",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.NETHERITE_SKIN.get(), 1800),
                    new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1800, 3),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1800, 0)));

    // Register glass skin potion
    public static final RegistryObject<Potion> GLASS_SKIN = POTIONS.register("glass_skin",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.GLASS_SKIN.get(), 3600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)));

    public static final RegistryObject<Potion> GLASS_SKIN_LONG = POTIONS.register("glass_skin_long",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.GLASS_SKIN.get(), 9600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)));

    public static final RegistryObject<Potion> HEAT = POTIONS.register("heat",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.HEAT.get(), 3600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)));

    public static final RegistryObject<Potion> HEAT_LONG = POTIONS.register("heat_long",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.HEAT.get(), 9600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)));

    public static final RegistryObject<Potion> HEAT_STRONG = POTIONS.register("heat_strong",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.HEAT.get(), 1800, 1),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800)));

    // Register flammability potion (3 minutes normal, 8 minutes extended)
    public static final RegistryObject<Potion> FLAMMABILITY = POTIONS.register("flammability",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.FLAMMABILITY.get(), 3600)));

    public static final RegistryObject<Potion> FLAMMABILITY_LONG = POTIONS.register("flammability_long",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.FLAMMABILITY.get(), 9600)));

    // Register pyromancy potion (2 minutes normal, 4 minutes long, 1 minute strong)
    public static final RegistryObject<Potion> PYROMANCY = POTIONS.register("pyromancy",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.PYROMANCY.get(), 2400),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400)));

    public static final RegistryObject<Potion> PYROMANCY_LONG = POTIONS.register("pyromancy_long",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.PYROMANCY.get(), 4800),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800)));

    public static final RegistryObject<Potion> PYROMANCY_STRONG = POTIONS.register("strong_pyromancy",
            () -> new Potion("pyromancy", new MobEffectInstance(ModEffects.PYROMANCY.get(), 1800, 1, false, true, true),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800, 0, false, true, true)));

    // Register magma walker potion (3 minutes normal, 8 minutes extended)
    public static final RegistryObject<Potion> MAGMA_WALKER = POTIONS.register("magma_walker",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.MAGMA_WALKER.get(), 3600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)));

    public static final RegistryObject<Potion> MAGMA_WALKER_LONG = POTIONS.register("magma_walker_long",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.MAGMA_WALKER.get(), 9600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)));

    public static final RegistryObject<Potion> MAGMA_WALKER_STRONG = POTIONS.register("magma_walker_strong",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.MAGMA_WALKER.get(), 1800, 1),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800)));

    // Register lava vision potion (3 minutes normal, 8 minutes extended)
    public static final RegistryObject<Potion> LAVA_VISION = POTIONS.register("lava_vision",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.LAVA_VISION.get(), 3600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)));

    public static final RegistryObject<Potion> LAVA_VISION_LONG = POTIONS.register("long_lava_vision",
            () -> new Potion(
                    new MobEffectInstance(ModEffects.LAVA_VISION.get(), 9600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)));

    public static final RegistryObject<Potion> LAVA_STRIDER = POTIONS.register("lava_strider",
            () -> new Potion(new MobEffectInstance(ModEffects.LAVA_STRIDER.get(), 3600, 0, false, true, true),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0, false, true, true)));

    public static final RegistryObject<Potion> LAVA_STRIDER_LONG = POTIONS.register("long_lava_strider",
            () -> new Potion("lava_strider",
                    new MobEffectInstance(ModEffects.LAVA_STRIDER.get(), 9600, 0, false, true, true),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600, 0, false, true, true)));

    public static final RegistryObject<Potion> LAVA_STRIDER_STRONG = POTIONS.register("strong_lava_strider",
            () -> new Potion("lava_strider",
                    new MobEffectInstance(ModEffects.LAVA_STRIDER.get(), 1800, 1, false, true, true),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800, 0, false, true, true)));

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
                potion == GLASS_SKIN.get() || potion == GLASS_SKIN_LONG.get() ||
                potion == HEAT.get() || potion == HEAT_LONG.get() || potion == HEAT_STRONG.get() ||
                potion == FLAMMABILITY.get() || potion == FLAMMABILITY_LONG.get() ||
                potion == PYROMANCY.get() || potion == PYROMANCY_LONG.get() || potion == PYROMANCY_STRONG.get() ||
                potion == MAGMA_WALKER.get() || potion == MAGMA_WALKER_LONG.get() || potion == MAGMA_WALKER_STRONG.get()
                ||
                potion == LAVA_VISION.get() || potion == LAVA_VISION_LONG.get() ||
                potion == LAVA_STRIDER.get() || potion == LAVA_STRIDER_LONG.get() || potion == LAVA_STRIDER_STRONG.get();
    }

    public static boolean isBasicLavaPotion(Potion potion) {
        return isBaseLavaBottle(potion) || isAwkwardLava(potion);
    }

    public static boolean isEffectLavaPotion(Potion potion) {
        return isLavaPotion(potion) && !isBasicLavaPotion(potion);
    }

    // Returns the Lava Potion for a given MobEffect, or null if not a Lava Potion effect
    public static Potion getPotionForEffect(net.minecraft.world.effect.MobEffect effect) {
        if (effect == ModEffects.OBSIDIAN_SKIN.get()) return OBSIDIAN_SKIN.get();
        if (effect == ModEffects.NETHERITE_SKIN.get()) return NETHERITE_SKIN.get();
        if (effect == ModEffects.GLASS_SKIN.get()) return GLASS_SKIN.get();
        if (effect == ModEffects.HEAT.get()) return HEAT.get();
        if (effect == ModEffects.FLAMMABILITY.get()) return FLAMMABILITY.get();
        if (effect == ModEffects.PYROMANCY.get()) return PYROMANCY.get();
        if (effect == ModEffects.MAGMA_WALKER.get()) return MAGMA_WALKER.get();
        if (effect == ModEffects.LAVA_VISION.get()) return LAVA_VISION.get();
        if (effect == ModEffects.LAVA_STRIDER.get()) return LAVA_STRIDER.get();
        // Add long/strong variants if needed
        return null;
    }

    // Returns the color for a Lava Potion, or -1 if not found
    public static int getPotionColor(Potion potion) {
        if (potion == OBSIDIAN_SKIN.get() || potion == OBSIDIAN_SKIN_LONG.get()) return 0x8e5de3;
        if (potion == NETHERITE_SKIN.get() || potion == NETHERITE_SKIN_LONG.get()) return 0xa47e75;
        if (potion == GLASS_SKIN.get() || potion == GLASS_SKIN_LONG.get()) return 0xc2f3ff;
        if (potion == HEAT.get() || potion == HEAT_LONG.get() || potion == HEAT_STRONG.get()) return 0xf7a236;
        if (potion == FLAMMABILITY.get() || potion == FLAMMABILITY_LONG.get()) return 0xffec99;
        if (potion == PYROMANCY.get() || potion == PYROMANCY_LONG.get() || potion == PYROMANCY_STRONG.get()) return 0xe5291f;
        if (potion == MAGMA_WALKER.get() || potion == MAGMA_WALKER_LONG.get() || potion == MAGMA_WALKER_STRONG.get()) return 0xd05c00;
        if (potion == LAVA_VISION.get() || potion == LAVA_VISION_LONG.get()) return 0x00ca98;
        if (potion == LAVA_STRIDER.get() || potion == LAVA_STRIDER_LONG.get() || potion == LAVA_STRIDER_STRONG.get()) return 0x005ff4;
        return -1;
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
        POTION_TYPES.add(GLASS_SKIN_LONG.get());

        // Add all the heat potions
        POTION_TYPES.add(HEAT.get());
        POTION_TYPES.add(HEAT_LONG.get());
        POTION_TYPES.add(HEAT_STRONG.get());

        // Add all the flammability potions
        POTION_TYPES.add(FLAMMABILITY.get());
        POTION_TYPES.add(FLAMMABILITY_LONG.get());

        // Add all the pyromancy potions
        POTION_TYPES.add(PYROMANCY.get());
        POTION_TYPES.add(PYROMANCY_LONG.get());
        POTION_TYPES.add(PYROMANCY_STRONG.get());

        // Add all the magma walker potions
        POTION_TYPES.add(MAGMA_WALKER.get());
        POTION_TYPES.add(MAGMA_WALKER_LONG.get());
        POTION_TYPES.add(MAGMA_WALKER_STRONG.get());

        // Add all the lava vision potions
        POTION_TYPES.add(LAVA_VISION.get());
        POTION_TYPES.add(LAVA_VISION_LONG.get());

        // Add all the lava strider potions
        POTION_TYPES.add(LAVA_STRIDER.get());
        POTION_TYPES.add(LAVA_STRIDER_LONG.get());
        POTION_TYPES.add(LAVA_STRIDER_STRONG.get());

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