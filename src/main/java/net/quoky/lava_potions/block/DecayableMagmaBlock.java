package net.quoky.lava_potions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.quoky.lava_potions.effect.ModEffects;
import net.quoky.lava_potions.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Decayable magma block that follows specific decay logic:
 * - Created with age 0, ticks every 1-2 seconds
 * - Ages when 1/3 chance succeeds OR fewer than 4 adjacent decayable magma blocks
 * - Turns to lava at age 4 and ages adjacent blocks (no cascade)
 * - Adjacent blocks immediately decay if fewer than 2 adjacent blocks when this block is removed
 * - Silk touch drops nothing, other methods place lava
 * - Pauses aging when part of a player platform
 */
public class DecayableMagmaBlock extends MagmaBlock {
    
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    private static final int MAX_AGE = 4;
    private static final float DECAY_CHANCE = 0.33f;
    private static final int MIN_ADJACENT_FOR_STABILITY = 4;
    private static final int MIN_ADJACENT_FOR_IMMEDIATE_DECAY = 2;
    private static final double PLATFORM_CHECK_RADIUS = 10.0; // 10 block radius for platform check
    private static final double PLATFORM_CHECK_RADIUS_SQUARED = PLATFORM_CHECK_RADIUS * PLATFORM_CHECK_RADIUS;
    
    // Cache for platform protection checks to avoid recalculating for nearby blocks
    private static final Map<BlockPos, Boolean> protectionCache = new HashMap<>();
    private static final int CACHE_SIZE_LIMIT = 1000; // Prevent memory leaks
    
    // Cache for adjacent block counts to avoid repeated calculations
    private static final Map<BlockPos, Integer> adjacentCountCache = new HashMap<>();
    private static final int ADJACENT_CACHE_SIZE_LIMIT = 500; // Smaller cache for adjacent counts
    
    public DecayableMagmaBlock() {
        super(Properties.copy(Blocks.MAGMA_BLOCK));
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
     * Count adjacent decayable magma blocks with caching for efficiency
     * Optimized to avoid repeated block state lookups and cache results
     */
    private int countAdjacentDecayableMagmaBlocks(Level level, BlockPos pos) {
        // Check cache first
        Integer cachedCount = adjacentCountCache.get(pos);
        if (cachedCount != null) {
            return cachedCount;
        }
        
        int count = 0;
        // Use a more efficient approach by checking all 6 directions at once
        BlockPos[] adjacentPositions = {
            pos.relative(Direction.NORTH),
            pos.relative(Direction.SOUTH),
            pos.relative(Direction.EAST),
            pos.relative(Direction.WEST),
            pos.relative(Direction.UP),
            pos.relative(Direction.DOWN)
        };
        
        for (BlockPos adjacentPos : adjacentPositions) {
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (adjacentState.getBlock() instanceof DecayableMagmaBlock) {
                count++;
            }
        }
        
        // Cache the result
        if (adjacentCountCache.size() >= ADJACENT_CACHE_SIZE_LIMIT) {
            // Clear cache if it gets too large
            adjacentCountCache.clear();
        }
        adjacentCountCache.put(pos, count);
        
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
                // Use block state update instead of full block placement for efficiency
                BlockState newBlockState = state.setValue(AGE, newAge);
                level.setBlockAndUpdate(pos, newBlockState);
            }
        }
    }
    
    /**
     * Remove the given block, then check adjacent blocks and age them if source = 0
     */
    private void removeBlock(Level level, BlockPos pos, int source) {
        // Remove the block (turn to lava)
        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        
        // Clear protection cache for this position
        protectionCache.remove(pos);
        
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
     * Optimized protection check with caching
     * Checks if this block is within the "safe" platform area of a nearby player with the Magma Walker effect.
     */
    private boolean isProtectedByPlatform(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }

        // Check cache first
        Boolean cachedResult = protectionCache.get(pos);
        if (cachedResult != null) {
            return cachedResult;
        }

        boolean isProtected = false;
        
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
                        isProtected = true;
                        break; // Found protection, no need to check other players
                    }
                }
            }
        }
        
        // Cache the result
        if (protectionCache.size() >= CACHE_SIZE_LIMIT) {
            // Clear cache if it gets too large (simple FIFO approach)
            protectionCache.clear();
        }
        protectionCache.put(pos, isProtected);
        
        return isProtected;
    }
    
    /**
     * Manual tick method for decay - called every 1-2 seconds
     */
    public void tickBlock(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Do DECAY_CHANCE math and if successful, call incrementAge with source = 0
        if (random.nextFloat() <= DECAY_CHANCE) {
            incrementAge(level, pos, state, 0);
            return;
        }
        
        // Call incrementAge with source = 0 if adjacent count < MIN_ADJACENT_FOR_STABILITY
        int adjacentCount = countAdjacentDecayableMagmaBlocks(level, pos);
        if (adjacentCount < MIN_ADJACENT_FOR_STABILITY) {
            incrementAge(level, pos, state, 0);
        }
    }
    
    /**
     * Called when this block is placed - register it for ticking
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        DecayableMagmaBlockTicker.registerBlock(level, pos);
        
        // Clear protection cache for this position
        protectionCache.remove(pos);
        
        // Invalidate adjacent count cache for this position and adjacent positions
        invalidateAdjacentCountCache(pos);
    }
    
    /**
     * Called when this block is removed - check adjacent blocks for immediate decay
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        
        // Unregister from ticker
        DecayableMagmaBlockTicker.unregisterBlock(pos);
        
        // Clear protection cache for this position
        protectionCache.remove(pos);
        
        // Invalidate adjacent count cache for this position and adjacent positions
        invalidateAdjacentCountCache(pos);
        
        // Place lava when block is removed (simplified - no silk touch detection for now)
        if (newState.isAir()) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        }
        
        // Collect adjacent blocks that need immediate decay for batch processing
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
     * Invalidate adjacent count cache for a position and its adjacent positions
     */
    private void invalidateAdjacentCountCache(BlockPos pos) {
        // Remove cache for this position
        adjacentCountCache.remove(pos);
        
        // Remove cache for all adjacent positions
        for (Direction direction : Direction.values()) {
            adjacentCountCache.remove(pos.relative(direction));
        }
    }
    
    /**
     * Clear all caches - call this when players leave or effects end
     */
    public static void clearAllCaches() {
        protectionCache.clear();
        adjacentCountCache.clear();
    }
    
    /**
     * Clear protection cache - call this when players leave or effects end
     * @deprecated Use clearAllCaches() instead
     */
    @Deprecated
    public static void clearProtectionCache() {
        clearAllCaches();
    }
} 