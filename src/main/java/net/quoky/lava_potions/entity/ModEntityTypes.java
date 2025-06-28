package net.quoky.lava_potions.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

public class ModEntityTypes {
    // Create a Deferred Register for entity types
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(ForgeRegistries.ENTITY_TYPES, Lava_Potions.MOD_ID);

    // Register the thrown lava potion entity
    public static final RegistryObject<EntityType<ThrownLavaPotion>> THROWN_LAVA_POTION = ENTITY_TYPES.register(
            "thrown_lava_potion",
            () -> EntityType.Builder.<ThrownLavaPotion>of(ThrownLavaPotion::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F) // Same size as vanilla thrown potions
                    .clientTrackingRange(4) // Same tracking range as vanilla thrown potions
                    .updateInterval(10) // Same update interval as vanilla thrown potions
                    .build(ResourceLocation.tryParse(Lava_Potions.MOD_ID + ":" + "thrown_lava_potion").toString()));

    /**
     * Register entity types
     */
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}