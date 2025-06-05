package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
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
            FlameBodyEffect::new);

    // Register all mob effects
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}