package net.quoky.lava_potions.fluid;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Custom fluid system that creates a new fluid with ID 'create:potion/lava_potions/awkward_lava'
 * This fluid uses the same NBT system as Create's potion fluid but with a custom namespace
 */
public class ModFluids {
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    
    public static final DeferredRegister<FluidType> FLUID_TYPES = 
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "create");
        
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = 
        DeferredRegister.create(ForgeRegistries.FLUIDS, "create");
    
    // Custom fluid type for lava potions
    public static final RegistryObject<FluidType> LAVA_POTION_FLUID_TYPE = FLUID_TYPES.register(
        "potion/lava_potions/awkward_lava", 
        () -> new LavaPotionFluidType(
            FluidType.Properties.create()
                .descriptionId("fluid.create.potion")
                .canSwim(false)
                .canDrown(false)
                .pathType(null)
                .adjacentPathType(null)
                .canConvertToSource(false)
                .canHydrate(false)
                .lightLevel(15) // Lava-like light level
                .density(1000)
                .temperature(1300)
                .viscosity(1000),
            ResourceLocation.fromNamespaceAndPath("create", "fluid/potion_still"),
            ResourceLocation.fromNamespaceAndPath("create", "fluid/potion_flow")
        )
    );
    
    // Source and flowing variants
    public static final RegistryObject<LavaPotionFluid> LAVA_POTION_SOURCE = FLUIDS.register(
        "potion/lava_potions/awkward_lava",
        () -> new LavaPotionFluid.Source()
    );
    
    public static final RegistryObject<LavaPotionFluid> LAVA_POTION_FLOWING = FLUIDS.register(
        "potion/lava_potions/awkward_lava_flowing", 
        () -> new LavaPotionFluid.Flowing()
    );
    
    /**
     * Custom fluid that mimics Create's PotionFluid but with custom namespace
     */
    public static class LavaPotionFluid extends VirtualFluid {
        
        public static class Source extends LavaPotionFluid {
            public Source() {
                super(createProperties(), true);
            }
        }
        
        public static class Flowing extends LavaPotionFluid {
            public Flowing() {
                super(createProperties(), false);
            }
        }
        
        private static ForgeFlowingFluid.Properties createProperties() {
            return new ForgeFlowingFluid.Properties(LAVA_POTION_FLUID_TYPE, LAVA_POTION_SOURCE, LAVA_POTION_FLOWING);
        }
        
        protected LavaPotionFluid(ForgeFlowingFluid.Properties properties, boolean source) {
            super(properties, source);
        }
        
        /**
         * Create a FluidStack with the custom lava potion fluid
         */
        public static FluidStack of(int amount, Potion potion, PotionFluid.BottleType bottleType) {
            FluidStack fluidStack = new FluidStack(LAVA_POTION_SOURCE.get(), amount);
            addPotionToFluidStack(fluidStack, potion);
            writeEnumToNBT(fluidStack.getOrCreateTag(), "Bottle", bottleType);
            
            // Add lava-specific metadata but don't override textures to avoid atlas issues
            CompoundTag tag = fluidStack.getOrCreateTag();
            tag.putBoolean("BehaveLikeLava", true);
            tag.putBoolean("PreventPlacement", true);
            tag.putString("FluidNamespace", "create:potion/lava_potions/awkward_lava");
            
            return fluidStack;
        }
        
        /**
         * Create a FluidStack with custom effects
         */
        public static FluidStack withEffects(int amount, Potion potion, List<MobEffectInstance> customEffects) {
            FluidStack fluidStack = of(amount, potion, PotionFluid.BottleType.REGULAR);
            appendEffects(fluidStack, customEffects);
            return fluidStack;
        }
        
        /**
         * Add potion data to fluid stack (copied from Create's implementation)
         */
        public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
            ResourceLocation resourcelocation = ForgeRegistries.POTIONS.getKey(potion);
            if (potion == Potions.EMPTY) {
                fs.removeChildTag("Potion");
                return fs;
            }
            fs.getOrCreateTag().putString("Potion", resourcelocation.toString());
            return fs;
        }
        
        /**
         * Append custom effects (copied from Create's implementation)
         */
        public static FluidStack appendEffects(FluidStack fs, Collection<MobEffectInstance> customEffects) {
            if (customEffects.isEmpty())
                return fs;
            CompoundTag compoundnbt = fs.getOrCreateTag();
            ListTag listnbt = compoundnbt.getList("CustomPotionEffects", 9);
            for (MobEffectInstance effectinstance : customEffects)
                listnbt.add(effectinstance.save(new CompoundTag()));
            compoundnbt.put("CustomPotionEffects", listnbt);
            return fs;
        }
        
        /**
         * Helper method to write enum to NBT (replaces NBTHelper.writeEnum)
         */
        private static void writeEnumToNBT(CompoundTag tag, String key, Enum<?> enumValue) {
            tag.putString(key, enumValue.name());
        }
        
        /**
         * Helper method to read enum from NBT (replaces NBTHelper.readEnum)
         */
        public static <T extends Enum<T>> T readEnumFromNBT(CompoundTag tag, String key, Class<T> enumClass) {
            if (!tag.contains(key)) {
                return enumClass.getEnumConstants()[0]; // Default to first value
            }
            try {
                return Enum.valueOf(enumClass, tag.getString(key));
            } catch (IllegalArgumentException e) {
                return enumClass.getEnumConstants()[0]; // Default to first value on error
            }
        }
    }
    
    /**
     * Custom fluid type that handles NBT data like Create's PotionFluidType
     */
    public static class LavaPotionFluidType extends FluidType {
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;
        
        public LavaPotionFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }
        
        @Override
        public void initializeClient(java.util.function.Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    // Use vanilla lava texture for awkward lava
                    return ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_still");
                }
                
                @Override
                public ResourceLocation getFlowingTexture() {
                    // Use vanilla lava texture for awkward lava
                    return ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_flow");
                }
                
                @Override
                public ResourceLocation getStillTexture(FluidStack stack) {
                    // Always return lava texture for our custom fluid
                    return getStillTexture();
                }
                
                @Override
                public ResourceLocation getFlowingTexture(FluidStack stack) {
                    // Always return lava texture for our custom fluid
                    return getFlowingTexture();
                }
                
                @Override
                public int getTintColor(FluidStack stack) {
                    // Always return pure white (no tint) for awkward lava to show natural lava color
                    return 0xFFFFFFFF;
                }
            });
        }
        
        @Override
        public String getDescriptionId(FluidStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            try {
                // Use Create's PotionFluidHandler to get the correct item type
                Class<?> handlerClass = Class.forName("com.simibubi.create.content.fluids.potion.PotionFluidHandler");
                java.lang.reflect.Method itemFromBottleTypeMethod = handlerClass.getMethod("itemFromBottleType", 
                    Class.forName("com.simibubi.create.content.fluids.potion.PotionFluid$BottleType"));
                
                PotionFluid.BottleType bottleType = LavaPotionFluid.readEnumFromNBT(tag, "Bottle", PotionFluid.BottleType.class);
                ItemLike itemFromBottleType = (ItemLike) itemFromBottleTypeMethod.invoke(null, bottleType);
                
                return PotionUtils.getPotion(tag).getName(
                    itemFromBottleType.asItem().getDescriptionId() + ".effect."
                );
            } catch (Exception e) {
                return "fluid.create.potion";
            }
        }
        
        public int getTintColor(FluidStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
            return color;
        }
    }
    
    /**
     * Register fluid system only if Create mod is loaded
     */
    public static void register(IEventBus eventBus) {
        if (CREATE_LOADED) {
            FLUID_TYPES.register(eventBus);
            FLUIDS.register(eventBus);
            Lava_Potions.LOGGER.info("Registered custom lava potion fluid: create:potion/lava_potions/awkward_lava");
        } else {
            Lava_Potions.LOGGER.info("Create mod not detected - skipping custom fluid registration");
        }
    }
} 