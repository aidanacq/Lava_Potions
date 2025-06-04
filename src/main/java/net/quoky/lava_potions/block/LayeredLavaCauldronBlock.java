package net.quoky.lava_potions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;


public class LayeredLavaCauldronBlock extends LayeredCauldronBlock {
    
    public LayeredLavaCauldronBlock(Properties properties) {
        super(properties, precipitation -> precipitation == net.minecraft.world.level.biome.Biome.Precipitation.RAIN, CauldronInteraction.LAVA);
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        // Same height calculation as water cauldrons
        return (6.0 + (double) state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        // Return signal strength based on level (same as water cauldrons)
        return state.getValue(LEVEL);
    }



    // Override to ensure proper light emission at all levels
    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        // Lava cauldrons should emit light level 15 regardless of level
        return 15;
    }

    /**
     * Handle entity collision - damage entities like vanilla lava cauldrons
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Only damage if the cauldron has lava (level > 0)
        if (state.getValue(LEVEL) > 0) {
            // Apply lava damage to the entity
            entity.hurt(entity.damageSources().lava(), 4.0F);
            
            // Set the entity on fire for 15 seconds (same as vanilla lava)
            entity.setSecondsOnFire(15);
        }
    }
} 