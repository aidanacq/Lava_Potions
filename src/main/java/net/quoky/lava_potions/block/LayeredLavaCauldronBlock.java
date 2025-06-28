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
        return (6.0 + (double) state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LEVEL) > 0) {
            entity.hurt(entity.damageSources().lava(), 4.0F);
            entity.setSecondsOnFire(15);
        }
    }
}