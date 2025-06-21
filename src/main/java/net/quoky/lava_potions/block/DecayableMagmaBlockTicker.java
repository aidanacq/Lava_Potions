package net.quoky.lava_potions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

/**
 * Manages manual ticking of decayable magma blocks every 3-4 seconds
 * Optimized for performance with efficient tick management
 */
@Mod.EventBusSubscriber
public class DecayableMagmaBlockTicker {
    
    private static final Map<BlockPos, Integer> blockTickers = new ConcurrentHashMap<>();
    private static final int MIN_TICK_INTERVAL = 60; // 3 seconds (60 ticks)
    private static final int MAX_TICK_INTERVAL = 80; // 4 seconds (80 ticks)
    private static final int TICK_INTERVAL_RANGE = MAX_TICK_INTERVAL - MIN_TICK_INTERVAL + 1;
    
    /**
     * Register a decayable magma block for ticking
     */
    public static void registerBlock(Level level, BlockPos pos) {
        if (level instanceof ServerLevel) {
            // Set a random tick interval between 3-4 seconds
            int tickInterval = MIN_TICK_INTERVAL + level.getRandom().nextInt(TICK_INTERVAL_RANGE);
            blockTickers.put(pos, tickInterval);
        }
    }
    
    /**
     * Unregister a decayable magma block from ticking
     */
    public static void unregisterBlock(BlockPos pos) {
        blockTickers.remove(pos);
    }
    
    /**
     * Server tick event to handle manual ticking
     * Optimized to process blocks efficiently and avoid redundant operations
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Get the server level
            ServerLevel level = event.getServer().overworld();
            if (level == null || blockTickers.isEmpty()) return;
            
            RandomSource random = level.getRandom();

            // Collect blocks that are ready to tick for batch processing
            Map<BlockPos, BlockState> blocksToTick = new HashMap<>();
            Map<BlockPos, Integer> updatedTickers = new HashMap<>();
            
            // Use iterator to safely remove entries during iteration
            Iterator<Map.Entry<BlockPos, Integer>> iterator = blockTickers.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Integer> entry = iterator.next();
                BlockPos pos = entry.getKey();
                int ticksRemaining = entry.getValue() - 1;

                if (ticksRemaining <= 0) {
                    // Time to tick this block
                    BlockState state = level.getBlockState(pos);
                    
                    if (state.getBlock() instanceof DecayableMagmaBlock) {
                        // Collect block for batch ticking
                        blocksToTick.put(pos, state);
                    } else {
                        // Block is no longer a decayable magma block, remove from ticker
                        iterator.remove();
                    }
                } else {
                    // Update remaining ticks
                    updatedTickers.put(pos, ticksRemaining);
                }
            }
            
            // Batch process all blocks that are ready to tick
            for (Map.Entry<BlockPos, BlockState> entry : blocksToTick.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                
                // Tick the block
                ((DecayableMagmaBlock) state.getBlock()).tickBlock(state, level, pos, random);

                // Check if block still exists after ticking
                BlockState newState = level.getBlockState(pos);
                if (newState.getBlock() instanceof DecayableMagmaBlock) {
                    // Set new random tick interval for next tick
                    int newTickInterval = MIN_TICK_INTERVAL + random.nextInt(TICK_INTERVAL_RANGE);
                    updatedTickers.put(pos, newTickInterval);
                } else {
                    // Block no longer exists, remove from ticker
                    blockTickers.remove(pos);
                }
            }
            
            // Update all ticker values at once
            blockTickers.putAll(updatedTickers);
        }
    }
    
    /**
     * Get the number of blocks currently being ticked
     * Useful for debugging and monitoring
     */
    public static int getActiveBlockCount() {
        return blockTickers.size();
    }
    
    /**
     * Clear all registered blocks
     * Useful for cleanup when world is unloaded
     */
    public static void clearAllBlocks() {
        blockTickers.clear();
    }
} 