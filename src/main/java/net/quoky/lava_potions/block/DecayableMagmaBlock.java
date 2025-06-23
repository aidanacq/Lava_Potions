package net.quoky.lava_potions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.quoky.lava_potions.effect.ModEffects;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decayable magma block that follows specific decay logic:
 * - Created with age 0, ages using standard Minecraft ticking
 * - Ages when 1/3 chance succeeds OR fewer than 4 adjacent decayable magma blocks
 * - Turns to lava at age 4 and ages adjacent blocks (no cascade)
 * - Adjacent blocks immediately decay if fewer than 2 adjacent blocks when this block is removed
 * - Pauses aging when part of a player platform
 * Uses native Minecraft scheduleTick system instead of custom ticking
 */
public class DecayableMagmaBlock extends MagmaBlock {
    
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    private static final int MAX_AGE = 4;
    private static final float DECAY_CHANCE = 0.33f;
    private static final int MIN_ADJACENT_FOR_STABILITY = 4;
    private static final int MIN_ADJACENT_FOR_IMMEDIATE_DECAY = 2;
    private static final double PLATFORM_CHECK_RADIUS = 10.0; // 10 block radius for platform check
    private static final double PLATFORM_CHECK_RADIUS_SQUARED = PLATFORM_CHECK_RADIUS * PLATFORM_CHECK_RADIUS;
    
    // Track whether lava should be placed when block is broken by player
    private static final Map<BlockPos, Boolean> shouldPlaceLava = new HashMap<>();
    
    public DecayableMagmaBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
    
    /**
     * Create a new decayable magma block
     */
    public static BlockState createDecayableMagmaBlock() {
        return ModBlocks.DECAYABLE_MAGMA_BLOCK.get().defaultBlockState();
    }
    
    /**
     * Count adjacent decayable magma blocks
     */
    private int countAdjacentDecayableMagmaBlocks(Level level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (adjacentState.getBlock() instanceof DecayableMagmaBlock) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * If not protected by platform, increment age by 1, then check if age >= 4 and call removeBlock if needed
     */
    private void incrementAge(Level level, BlockPos pos, BlockState state, int source) {
        if (!isProtectedByPlatform(level, pos)) {
            int currentAge = state.getValue(AGE);
            int newAge = currentAge + 1;
            
            if (newAge >= MAX_AGE) {
                removeBlock(level, pos, source);
            } else {
                // Use block state update
                BlockState newBlockState = state.setValue(AGE, newAge);
                level.setBlockAndUpdate(pos, newBlockState);
                // Schedule next tick using standard Minecraft system (3-4 seconds)
                level.scheduleTick(pos, this, Mth.nextInt(level.getRandom(), 180, 240));
            }
        } else {
            // Still schedule next tick even if protected, to check again later (3-4 seconds)
            level.scheduleTick(pos, this, Mth.nextInt(level.getRandom(), 180, 240));
        }
    }
    
    /**
     * Remove the given block, then check adjacent blocks and age them if source = 0
     */
    private void removeBlock(Level level, BlockPos pos, int source) {
        // Remove the block (turn to lava)
        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        
        // If source = 0, call ageAdjacentBlocks
        if (source == 0) {
            ageAdjacentBlocks(level, pos);
        }
    }
    
    /**
     * Age adjacent blocks by calling incrementAge with source = 1 for each
     * Optimized to collect all adjacent blocks first for better performance
     */
    private void ageAdjacentBlocks(Level level, BlockPos pos) {
        // Collect all adjacent decayable magma blocks first
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            
            if (adjacentState.getBlock() instanceof DecayableMagmaBlock) {
                incrementAge(level, adjacentPos, adjacentState, 1);
            }
        }
    }
    
    /**
     * Protection check - simplified version without caching
     * Checks if this block is within the "safe" platform area of a nearby player with the Magma Walker effect.
     */
    private boolean isProtectedByPlatform(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }

        for (Player player : level.players()) {
            // Use squared distance for efficiency (avoid square root)
            double distanceSquared = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
            if (distanceSquared > PLATFORM_CHECK_RADIUS_SQUARED) {
                continue;
            }

            MobEffectInstance effectInstance = player.getEffect(ModEffects.MAGMA_WALKER.get());
            if (effectInstance != null) {
                int amplifier = effectInstance.getAmplifier();
                int platformSize = (amplifier >= 1) ? 7 : 5; // Tier I: 5x5, Tier II+: 7x7
                int halfSize = platformSize / 2;

                BlockPos playerFootPos = player.blockPosition().below();
                
                // Check if the block is within the platform's square bounds and at the correct y-level
                if (pos.getY() == playerFootPos.getY()) {
                    int dX = pos.getX() - playerFootPos.getX();
                    int dZ = pos.getZ() - playerFootPos.getZ();
                    
                    if (Math.abs(dX) <= halfSize && Math.abs(dZ) <= halfSize) {
                        // Exclude the corner blocks
                        if (Math.abs(dX) == halfSize && Math.abs(dZ) == halfSize) {
                            continue; // This is a corner block, not protected
                        }
                        return true; // Found protection
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Standard Minecraft tick method - replaces custom ticker system
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Do DECAY_CHANCE math and if successful, call incrementAge with source = 0
        if (random.nextFloat() <= DECAY_CHANCE) {
            incrementAge(level, pos, state, 0);
            return;
        }
        
        // Call incrementAge with source = 0 if adjacent count < MIN_ADJACENT_FOR_STABILITY
        int adjacentCount = countAdjacentDecayableMagmaBlocks(level, pos);
        if (adjacentCount < MIN_ADJACENT_FOR_STABILITY) {
            incrementAge(level, pos, state, 0);
        } else {
            // Schedule next tick if not aging this time (3-4 seconds)
            level.scheduleTick(pos, this, Mth.nextInt(random, 180, 240));
        }
    }
    
    /**
     * Called when this block is placed - schedule first tick using standard Minecraft system
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Schedule first tick using standard Minecraft system (3-4 seconds)
        level.scheduleTick(pos, this, Mth.nextInt(level.getRandom(), 180, 240));
    }
    
    /**
     * Called when a player is about to destroy this block - used to determine if lava should be placed
     */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Check if player is in creative mode
        boolean isCreative = player.isCreative();
        
        // Check if player is using silk touch
        boolean hasSilkTouch = player.getMainHandItem().getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;
        
        // Store whether we should place lava (only if NOT creative and NOT silk touch)
        shouldPlaceLava.put(pos, !isCreative && !hasSilkTouch);
        
        super.playerWillDestroy(level, pos, state, player);
    }
    
    /**
     * Called when this block is removed - check adjacent blocks for immediate decay
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        
        // Check if we should place lava based on how the block was broken
        Boolean placeLava = shouldPlaceLava.remove(pos);
        boolean shouldPlace = placeLava == null ? true : placeLava; // Default to true for non-player breaks
        
        // Place lava when block is removed, unless broken by creative player or silk touch
        if (newState.isAir() && shouldPlace) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        }
        
        // Check adjacent blocks for immediate decay
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            
            if (adjacentState.getBlock() instanceof DecayableMagmaBlock) {
                // Count how many decayable magma blocks are adjacent to this adjacent block
                int adjacentCount = countAdjacentDecayableMagmaBlocks(level, adjacentPos);
                
                // If fewer than 2 adjacent blocks, immediately decay
                if (adjacentCount < MIN_ADJACENT_FOR_IMMEDIATE_DECAY) {
                    removeBlock(level, adjacentPos, 0);
                }
            }
        }
    }
    
    /**
     * Override to ensure this block never drops any items
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.emptyList();
    }
} 