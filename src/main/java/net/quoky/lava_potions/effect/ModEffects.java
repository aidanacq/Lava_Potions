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
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Lava_Potions.MOD_ID);

    // Register the Flame Body effect
    public static final RegistryObject<MobEffect> FLAME_BODY = MOB_EFFECTS.register("flame_body",
            () -> new FlameBodyEffect());

    // Register the Hot Hands effect - we'll implement this class later
    public static final RegistryObject<MobEffect> HOT_HANDS = MOB_EFFECTS.register("hot_hands",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF3300) {
                // Temporary implementation, will be replaced with proper class
            });

    // Register the Magma Walker effect - we'll implement this class later
    public static final RegistryObject<MobEffect> MAGMA_WALKER = MOB_EFFECTS.register("magma_walker",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF5500) {
                // Temporary implementation, will be replaced with proper class
            });

    // Register the Lava Vision effect - we'll implement this class later
    public static final RegistryObject<MobEffect> LAVA_VISION = MOB_EFFECTS.register("lava_vision",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
                // Temporary implementation, will be replaced with proper class
            });

    // Register the Lava Glide effect - we'll implement this class later
    public static final RegistryObject<MobEffect> LAVA_GLIDE = MOB_EFFECTS.register("lava_glide",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xDD4400) {
                // Temporary implementation, will be replaced with proper class
            });

    // Register all mob effects
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}