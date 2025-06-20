package net.quoky.lava_potions.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.block.ModBlocks;
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.util.CreateCompat;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LavaBottleHandler {
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    private static final int LAVA_AMOUNT_TO_ADD = 250;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (heldItem.getItem() == Items.GLASS_BOTTLE) {
            handleFillingBottle(event, level, player, heldItem);
            return;
        }
        
        if (heldItem.getItem() == Items.LAVA_BUCKET) {
            handleLavaBucketUse(event, level, player, heldItem);
            return;
        }
        
        if (BrewingRecipes.isVanillaPotionWithLavaType(heldItem)) {
            Potion potion = PotionUtils.getPotion(heldItem);
            if (potion == ModPotionTypes.LAVA_BOTTLE.get()) {
                handleEmptyingLavaBottle(event, level, player, heldItem);
            }
        }
        
        if (heldItem.isEmpty()) {
            handleEmptyHandInteraction(event, level, player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (heldItem.getItem() != Items.GLASS_BOTTLE) {
            return;
        }

        BlockPos lavaPos = findLavaInSight(level, player);
        if (lavaPos != null) {
            createLavaBottle(level, player, event.getHand(), lavaPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
    
    /**
     * Handle filling a bottle with lava
     */
    private static void handleFillingBottle(PlayerInteractEvent.RightClickBlock event, 
                                          Level level, Player player, ItemStack heldItem) {
        // Check the block that was clicked
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Handle vanilla lava cauldron interaction - convert to layered system
        if (clickedState.is(Blocks.LAVA_CAULDRON)) {
            createLavaBottle(level, player, event.getHand(), clickedPos);
            
            // Convert vanilla lava cauldron to layered lava cauldron with level 2 (2/3 full)
            level.setBlockAndUpdate(clickedPos, ModBlocks.LAVA_CAULDRON.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 2));
            
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, clickedPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
            return;
        }
        
        // Handle layered lava cauldron interaction - reduce level
        if (clickedState.is(ModBlocks.LAVA_CAULDRON.get())) {
            createLavaBottle(level, player, event.getHand(), clickedPos);
            
            int currentLevel = clickedState.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
            currentLevel--;
            
            if (currentLevel > 0) {
                // Still has lava, reduce the level
                level.setBlockAndUpdate(clickedPos, clickedState.setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, currentLevel));
            } else {
                // No more lava, empty the cauldron
                level.setBlockAndUpdate(clickedPos, Blocks.CAULDRON.defaultBlockState());
            }
            
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, clickedPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
            return;
        }

        if (clickedState.getBlock() == Blocks.LAVA && clickedState.getFluidState().isSource()) {
            // Direct click on lava source
            createLavaBottle(level, player, event.getHand(), clickedPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
            return;
        }

        // Check if we're looking through lava
        BlockPos lavaPos = findLavaInSight(level, player);
        if (lavaPos != null) {
            createLavaBottle(level, player, event.getHand(), lavaPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
        }
    }
    
    /**
     * Handle right-clicking with empty hand to check lava cauldron level
     */
    private static void handleEmptyHandInteraction(PlayerInteractEvent.RightClickBlock event, 
                                                 Level level, Player player) {
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Handle checking vanilla lava cauldron level
        if (clickedState.is(Blocks.LAVA_CAULDRON)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Lava cauldron is full"));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
        
        // Handle checking layered lava cauldron level
        if (clickedState.is(ModBlocks.LAVA_CAULDRON.get())) {
            int currentLevel = clickedState.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
            
            if (!level.isClientSide) {
                String levelText;
                switch (currentLevel) {
                    case 3:
                        levelText = "full";
                        break;
                    case 2:
                        levelText = "2/3 full";
                        break;
                    case 1:
                        levelText = "1/3 full";
                        break;
                    default:
                        levelText = "empty";
                        break;
                }
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Lava cauldron is " + levelText));
            }
            
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
    
    /**
     * Handle using a lava bucket on cauldrons to create leveled system
     */
    private static void handleLavaBucketUse(PlayerInteractEvent.RightClickBlock event, 
                                          Level level, Player player, ItemStack heldItem) {
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Handle filling empty cauldron with lava bucket
        if (clickedState.is(Blocks.CAULDRON)) {
            // Fill cauldron with layered lava cauldron at level 3 (full)
            level.setBlockAndUpdate(clickedPos, ModBlocks.LAVA_CAULDRON.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
            
            // Play sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // Fire game event
            level.gameEvent(player, GameEvent.FLUID_PLACE, clickedPos);
            
            // Give back empty bucket (if not in creative)
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (heldItem.isEmpty()) {
                    player.setItemInHand(event.getHand(), emptyBucket);
                } else if (!player.getInventory().add(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
            }
            
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
        }
    }
    
    /**
     * Handle emptying a lava bottle into a block
     */
    private static void handleEmptyingLavaBottle(PlayerInteractEvent.RightClickBlock event, 
                                               Level level, Player player, ItemStack heldItem) {
        BlockPos clickedPos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        
        // Try to empty into a Create basin if available
        if (CREATE_LOADED && blockEntity != null && CreateCompat.isCreateBasin(level, clickedPos, blockEntity)) {
            // Try to get fluid handler capability
            blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidHandler -> {
                // Create a new FluidStack with lava
                FluidStack lavaStack = new FluidStack(Fluids.LAVA, LAVA_AMOUNT_TO_ADD);
                
                // Check if the tank can accept this fluid
                int filled = fluidHandler.fill(lavaStack, IFluidHandler.FluidAction.SIMULATE);
                if (filled > 0) {
                    // Actually fill the tank
                    fluidHandler.fill(lavaStack, IFluidHandler.FluidAction.EXECUTE);
                    
                    // Play sound
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 0.8F);
                    
                    // Fire game event
                    level.gameEvent(player, GameEvent.FLUID_PLACE, clickedPos);
                    
                    // Give back empty bottle
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                        ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
                        if (heldItem.isEmpty()) {
                            player.setItemInHand(event.getHand(), emptyBottle);
                        } else if (!player.getInventory().add(emptyBottle)) {
                            player.drop(emptyBottle, false);
                        }
                    }
                    
                    // Cancel the event
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    event.setUseItem(Event.Result.DENY);
                }
            });
        }
    }

    /**
     * Find lava in the player's line of sight
     * @param level The level
     * @param player The player
     * @return The position of lava, or null if none found
     */
    private static BlockPos findLavaInSight(Level level, Player player) {
        // Get player's eye position and view vector
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);

        // Define the reach distance (default vanilla reach is around 5.0)
        double reach = 5.0D;
        Vec3 targetPos = eyePos.add(viewVec.x * reach, viewVec.y * reach, viewVec.z * reach);

        // First try vanilla's fluid targeting
        BlockHitResult vanillaHit = level.clip(new ClipContext(
                eyePos, targetPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,
                player));

        if (vanillaHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = vanillaHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() == Blocks.LAVA && state.getFluidState().isSource()) {
                return pos;
            }
        }

        // Manual ray casting as a fallback
        // We'll check every 0.25 blocks along the line of sight
        double stepSize = 0.25D;
        double distance = reach;

        for (double d = 0; d <= distance; d += stepSize) {
            double scale = d / distance;

            // Interpolate between eye position and target position
            double x = eyePos.x + (targetPos.x - eyePos.x) * scale;
            double y = eyePos.y + (targetPos.y - eyePos.y) * scale;
            double z = eyePos.z + (targetPos.z - eyePos.z) * scale;

            BlockPos checkPos = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
            BlockState state = level.getBlockState(checkPos);

            if (state.getBlock() == Blocks.LAVA && state.getFluidState().isSource()) {
                return checkPos;
            }
        }

        return null;
    }

    /**
     * Create a lava bottle and give it to the player
     * @param level The level
     * @param player The player
     * @param hand The hand
     * @param lavaPos The position of the lava
     */
    private static void createLavaBottle(Level level, Player player, InteractionHand hand, BlockPos lavaPos) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Create vanilla lava bottle using our brewing recipes system
        ItemStack lavaBottle = BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get());

        // Play sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

        // Fire game event
        level.gameEvent(player, GameEvent.FLUID_PICKUP, lavaPos);

        // Remove one glass bottle (if not in creative)
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }

        // Give the lava bottle to the player
        if (heldItem.isEmpty()) {
            player.setItemInHand(hand, lavaBottle);
        } else {
            if (!player.getInventory().add(lavaBottle)) {
                player.drop(lavaBottle, false);
            }
        }
    }
}