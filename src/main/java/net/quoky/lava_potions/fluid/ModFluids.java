package net.quoky.lava_potions.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Fluid registration for Create mod compatibility
 * Pre-registers fluids with Create's expected registry names to override default textures
 */
public class ModFluids {
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    
    // Use Create's namespace for compatibility
    public static final DeferredRegister<FluidType> FLUID_TYPES = 
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "create");
    public static final DeferredRegister<Fluid> FLUIDS = 
        DeferredRegister.create(ForgeRegistries.FLUIDS, "create");
    
    // Awkward Lava Potion fluid with lava textures
    public static final RegistryObject<FluidType> AWKWARD_LAVA_POTION_FLUID_TYPE = FLUID_TYPES.register(
        "potion/lava_potions/awkward_lava",
        () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid.create.potion.lava_potions.awkward_lava")
            .canSwim(false)
            .canDrown(false)
            .pathType(null)
            .adjacentPathType(null)
            .lightLevel(15)
            .density(3000)
            .viscosity(6000)
            .temperature(1300)
            .canConvertToSource(false)
            .canHydrate(false)
        ) {
            @Override
            public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_still");
                    }
                    
                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_flow");
                    }
                });
            }
        }
    );
    
    public static final RegistryObject<FlowingFluid> AWKWARD_LAVA_POTION_FLOWING = FLUIDS.register(
        "potion/lava_potions/awkward_lava_flowing",
        () -> new ForgeFlowingFluid.Flowing(ModFluids.AWKWARD_LAVA_POTION_PROPERTIES)
    );
    
    public static final RegistryObject<FlowingFluid> AWKWARD_LAVA_POTION_SOURCE = FLUIDS.register(
        "potion/lava_potions/awkward_lava",
        () -> new ForgeFlowingFluid.Source(ModFluids.AWKWARD_LAVA_POTION_PROPERTIES)
    );
    
    public static final ForgeFlowingFluid.Properties AWKWARD_LAVA_POTION_PROPERTIES = new ForgeFlowingFluid.Properties(
        AWKWARD_LAVA_POTION_FLUID_TYPE, AWKWARD_LAVA_POTION_SOURCE, AWKWARD_LAVA_POTION_FLOWING
    ).slopeFindDistance(2).levelDecreasePerBlock(2);
    
    /**
     * Register fluids only if Create mod is loaded
     */
    public static void register(IEventBus eventBus) {
        if (CREATE_LOADED) {
            Lava_Potions.LOGGER.info("Registering Create compatibility fluids with lava textures");
            FLUID_TYPES.register(eventBus);
            FLUIDS.register(eventBus);
        } else {
            Lava_Potions.LOGGER.info("Create mod not detected - skipping fluid registration");
        }
    }
} 