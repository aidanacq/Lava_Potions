package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

public class ModEffects {
    // Create a Deferred Register for mob effects
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
            Lava_Potions.MOD_ID);

    // Register the Heat effect
    public static final RegistryObject<MobEffect> HEAT = MOB_EFFECTS.register("heat",
            HeatEffect::new);

    // Register the Obsidian Skin effect
    public static final RegistryObject<MobEffect> OBSIDIAN_SKIN = MOB_EFFECTS.register("obsidian_skin",
            ObsidianSkinEffect::new);

    // Register the Netherite Skin effect
    public static final RegistryObject<MobEffect> NETHERITE_SKIN = MOB_EFFECTS.register("netherite_skin",
            NetheriteSkinEffect::new);

    // Register the Glass Skin effect
    public static final RegistryObject<MobEffect> GLASS_SKIN = MOB_EFFECTS.register("glass_skin",
            GlassSkinEffect::new);

    // Register the Flammability effect
    public static final RegistryObject<MobEffect> FLAMMABILITY = MOB_EFFECTS.register("flammability",
            FlammabilityEffect::new);

    // Register the Pyromancy effect
    public static final RegistryObject<MobEffect> PYROMANCY = MOB_EFFECTS.register("pyromancy",
            PyromancyEffect::new);

    // Register the Magma Walker effect
    public static final RegistryObject<MobEffect> MAGMA_WALKER = MOB_EFFECTS.register("magma_walker",
            MagmaWalkerEffect::new);

    // Register the Lava Vision effect
    public static final RegistryObject<MobEffect> LAVA_VISION = MOB_EFFECTS.register("lava_vision",
            LavaVisionEffect::new);

    // Register the Lava Strider effect
    public static final RegistryObject<MobEffect> LAVA_STRIDER = MOB_EFFECTS.register("lava_strider",
            LavaStriderEffect::new);

    // Register all mob effects
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}