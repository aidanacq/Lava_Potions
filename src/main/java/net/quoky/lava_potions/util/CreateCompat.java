package net.quoky.lava_potions.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.ModItems;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

/**
 * Compatibility with Create mod
 * 
 * Create automatically detects all potions in ForgeRegistries.POTIONS and creates:
 * 1. Potion fluids for mixing recipes
 * 2. JEI integration (only for REGULAR bottle types, not splash/lingering)
 * 3. Automatic mixing recipes based on vanilla brewing recipes
 * 
 * Our lava potions (lava_bottle and awkward_lava) will be automatically detected
 * and integrated into Create's potion fluid system.
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CreateCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("CreateCompat");
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    private static final int LAVA_AMOUNT_REQUIRED = 250; // 250mb = 1/4 bucket
    
    // Define Create registry paths as constants to avoid hard-coding them in multiple places
    private static final String CREATE_NAMESPACE = "create";
    private static final String BASIN_BLOCK_PATH = "basin";
    private static final String BASIN_BLOCKENTITY_PATH = "basin";
    private static final String BASIN_CLASS_IDENTIFIER = "Basin";
    
    /**
     * Log Create integration status on mod initialization
     */
    static {
        if (CREATE_LOADED) {
            LOGGER.info("Create mod detected! Lava potions will be automatically integrated:");
            LOGGER.info("- Lava Bottle and Awkward Lava Potion will be available as potion fluids");
            LOGGER.info("- Mixing recipes will be automatically generated");
            LOGGER.info("- JEI integration will show lava potion fluids (regular variants only)");
            LOGGER.info("- Basin interaction for filling lava bottles is enabled");
        } else {
            LOGGER.info("Create mod not detected. Basin interaction disabled.");
        }
    }
    
    /**
     * Check if a potion should be included in Create's potion fluid system
     * This helps ensure only the base lava potions are properly integrated
     */
    public static boolean shouldIncludeInCreateIntegration(Potion potion) {
        if (!CREATE_LOADED) {
            return false;
        }
        
        // Only include base lava bottle and awkward lava (the 2 registered types)
        return ModPotionTypes.isLavaPotion(potion);
    }
    
    /**
     * Get the Create potion fluid equivalent for a lava potion
     * This can be used by other parts of the mod to interact with Create's fluid system
     */
    public static FluidStack getLavaPotionFluid(Potion potion, int amount) {
        if (!CREATE_LOADED || !shouldIncludeInCreateIntegration(potion)) {
            return FluidStack.EMPTY;
        }
        
        try {
            // Use reflection to access Create's PotionFluid.of method
            Class<?> potionFluidClass = Class.forName("com.simibubi.create.content.fluids.potion.PotionFluid");
            Class<?> bottleTypeClass = Class.forName("com.simibubi.create.content.fluids.potion.PotionFluid$BottleType");
            
            Object regularBottleType = Enum.valueOf((Class<Enum>) bottleTypeClass, "REGULAR");
            
            java.lang.reflect.Method ofMethod = potionFluidClass.getMethod("of", int.class, Potion.class, bottleTypeClass);
            FluidStack result = (FluidStack) ofMethod.invoke(null, amount, potion, regularBottleType);
            
            LOGGER.debug("Created Create potion fluid for {}: {}mb", 
                ForgeRegistries.POTIONS.getKey(potion), amount);
            
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to create Create potion fluid for {}: {}", 
                ForgeRegistries.POTIONS.getKey(potion), e.getMessage());
            return FluidStack.EMPTY;
        }
    }
    
    /**
     * Handle right-clicking on Create basin blocks to fill lava bottles
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!CREATE_LOADED || event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        // Check if player is holding a glass bottle
        if (heldItem.getItem() != Items.GLASS_BOTTLE) {
            return;
        }

        // Check if the clicked block is a basin
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        // Skip if no block entity at this position
        if (blockEntity == null) {
            return;
        }
        
        // Check if this is a Create basin
        if (!isCreateBasin(level, pos, blockEntity)) {
            return;
        }
        
        LOGGER.debug("Found Create basin at {}", pos);
        
        // Try to get fluid handler capability
        blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidHandler -> {
            // Handle basin's fluid tanks (usually just one)
            for (int tankIndex = 0; tankIndex < fluidHandler.getTanks(); tankIndex++) {
                FluidStack fluid = fluidHandler.getFluidInTank(tankIndex);
                
                // Check if the tank contains lava and enough of it
                if (fluid.isEmpty() || fluid.getAmount() < LAVA_AMOUNT_REQUIRED) {
                    continue;
                }
                
                // Check if it's lava using both registry name and direct comparison
                if (!isLavaFluid(fluid)) {
                    continue;
                }
                
                LOGGER.debug("Basin contains lava: {}mb", fluid.getAmount());
                
                // Create a vanilla lava bottle instead of custom lava potion
                ItemStack lavaBottle = VanillaPotionBrewingRecipes.createVanillaPotionWithLavaType(
                    ModPotionTypes.LAVA_BOTTLE.get());
                
                // Remove lava from the basin
                FluidStack drainedFluid = fluidHandler.drain(
                    new FluidStack(fluid.getFluid(), LAVA_AMOUNT_REQUIRED), 
                    FluidAction.EXECUTE
                );
                
                // Only proceed if we actually drained something
                if (drainedFluid.isEmpty() || drainedFluid.getAmount() < LAVA_AMOUNT_REQUIRED) {
                    LOGGER.debug("Failed to drain lava from basin");
                    continue;
                }
                
                LOGGER.debug("Drained {}mb of lava from basin", drainedFluid.getAmount());
                
                // Play sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 0.8F);
                
                // Fire game event
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                
                // Remove one glass bottle (if not in creative)
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                
                // Give the lava bottle to the player
                if (heldItem.isEmpty()) {
                    player.setItemInHand(event.getHand(), lavaBottle);
                } else {
                    if (!player.getInventory().add(lavaBottle)) {
                        player.drop(lavaBottle, false);
                    }
                }
                
                // Cancel the event
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                event.setUseItem(Event.Result.DENY);
                
                // Break the loop once we've handled one tank
                return;
            }
            
            LOGGER.debug("Basin doesn't contain enough lava or processing failed");
        });
    }
    
    /**
     * Check if a fluid is lava using multiple detection methods
     */
    private static boolean isLavaFluid(FluidStack fluid) {
        // Direct comparison with vanilla fluid
        if (fluid.getFluid() == Fluids.LAVA) {
            return true;
        }
        
        // Check registry name
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
        if (fluidId != null) {
            // Check for vanilla lava
            if ("minecraft:lava".equals(fluidId.toString()) || "lava".equals(fluidId.getPath())) {
                return true;
            }
            
            // Check for modded lava variants that might have "lava" in their name
            // This is a more permissive check, but reasonable for compatibility
            if (fluidId.getPath().contains("lava")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Comprehensive check to determine if a block entity is a Create basin
     * Uses multiple approaches to be resilient to Create mod updates
     */
    public static boolean isCreateBasin(Level level, BlockPos pos, BlockEntity blockEntity) {
        // Method 1: Check BlockEntity type registry name
        ResourceLocation blockEntityId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType());
        if (blockEntityId != null && CREATE_NAMESPACE.equals(blockEntityId.getNamespace()) && 
            blockEntityId.getPath().contains(BASIN_BLOCKENTITY_PATH)) {
            return true;
        }
        
        // Method 2: Check Block registry name
        Block block = level.getBlockState(pos).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId != null && CREATE_NAMESPACE.equals(blockId.getNamespace()) && 
            blockId.getPath().equals(BASIN_BLOCK_PATH)) {
            return true;
        }
        
        // Method 3: Check class name (most error-prone but useful as fallback)
        String className = blockEntity.getClass().getName();
        if (className.contains(CREATE_NAMESPACE) && className.contains(BASIN_CLASS_IDENTIFIER)) {
            return true;
        }
        
        // Not a basin if all checks fail
        return false;
    }
} 