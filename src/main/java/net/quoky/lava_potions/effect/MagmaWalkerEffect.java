package net.quoky.lava_potions.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.quoky.lava_potions.block.DecayableMagmaBlock;
import net.quoky.lava_potions.block.ModBlocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Effect that creates magma platforms when walking on ground near lava
 * Tier I: 5x5 square with corners removed (21 blocks)
 * Tier II: 7x7 square with corners removed (45 blocks)
 * Grants speed when standing on magma blocks
 * Spawns flame particles at the player's feet
 * Essentially the opposite of the frost walker enchantment with a slightly different properties
 */
public class MagmaWalkerEffect extends MobEffect {
    
    // Track previous positions for each player using integer coordinates for efficiency
    private static final Map<UUID, BlockPos> previousBlockPositions = new HashMap<>();
    
    // Cache platform blocks for each player to avoid redundant placement
    private static final Map<UUID, Set<BlockPos>> lastPlatformBlocks = new HashMap<>();
    
    // Constants for platform sizes
    private static final int TIER_I_SIZE = 5;
    private static final int TIER_II_SIZE = 7;
    private static final double MOVEMENT_THRESHOLD = 0.001; // Small epsilon for floating point comparison
    
    public MagmaWalkerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xe76200);
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Run platform creation every tick for better responsiveness
        // Run particle effects every 4 ticks (5 times per second) like Fire Avatar
        return duration > 0 && duration % 4 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Only apply to players
        if (!(entity instanceof Player player)) {
            return;
        }
        
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        
        // Check if player is standing on a magma block
        BlockPos groundPos = playerPos.below();
        BlockState groundState = level.getBlockState(groundPos);
        
        // Apply speed effect when standing on magma blocks
        if (groundState.is(Blocks.MAGMA_BLOCK) || groundState.is(ModBlocks.DECAYABLE_MAGMA_BLOCK.get())) {
            int speedLevel = amplifier == 0 ? 0 : 1; // Tier I: Speed I, Tier II+: Speed II
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, speedLevel, false, false));
        }
        
        // Spawn flame particles at player's feet (client-side only)
        if (level.isClientSide) {
            spawnFlameParticles(player);
        }
        
        // Check if platform should be created based on movement
        if (shouldCreatePlatform(player)) {
            createMagmaPlatform(level, player, amplifier);
        }
        
        // Update previous position for next tick
        updatePreviousPosition(player);
    }
    
    /**
     * Optimized movement detection using integer coordinates
     * Returns true if X or Z changed by a whole number and Y remained unchanged
     */
    private boolean shouldCreatePlatform(Player player) {
        UUID playerId = player.getUUID();
        BlockPos currentBlockPos = player.blockPosition();
        BlockPos previousBlockPos = previousBlockPositions.get(playerId);
        
        // If no previous position exists, this is the initial application of the effect
        // Don't create a platform on initial effect gain
        if (previousBlockPos == null) {
            return false;
        }
        
        // Use integer-based comparison for efficiency
        boolean xChanged = currentBlockPos.getX() != previousBlockPos.getX();
        boolean zChanged = currentBlockPos.getZ() != previousBlockPos.getZ();
        boolean yUnchanged = currentBlockPos.getY() == previousBlockPos.getY();
        
        return yUnchanged && (xChanged || zChanged);
    }
    
    /**
     * Updates the previous block position for the player using integer coordinates
     */
    private void updatePreviousPosition(Player player) {
        UUID playerId = player.getUUID();
        BlockPos currentBlockPos = player.blockPosition();
        previousBlockPositions.put(playerId, currentBlockPos);
    }

    /**
     * Spawns flame particles at the player's feet
     */
    private void spawnFlameParticles(Player player) {
        Vec3 playerPosVec = player.position();
        float bodyYaw = (float) Math.toRadians(player.yBodyRot);
        
        // Calculate body center position (at feet level)
        double bodyX = playerPosVec.x;
        double bodyY = playerPosVec.y + 0.1; // Slightly above ground level
        double bodyZ = playerPosVec.z;
        
        // Calculate forward and right vectors based on body rotation
        double rightX = Math.cos(bodyYaw);
        double rightZ = Math.sin(bodyYaw);
        
        // Foot positions relative to body center
        double footSideOffset = 0.15;    // Distance from center to each foot
        
        // Calculate actual foot positions
        double rightFootX = bodyX + (rightX * footSideOffset);
        double rightFootY = bodyY;
        double rightFootZ = bodyZ + (rightZ * footSideOffset);
        
        double leftFootX = bodyX - (rightX * footSideOffset);
        double leftFootY = bodyY;
        double leftFootZ = bodyZ - (rightZ * footSideOffset);
        
        // Alternate between left and right foot based on tick count
        boolean spawnRightFoot = (player.tickCount / 4) % 2 == 0; // Every 4 ticks, alternate
        
        if (spawnRightFoot) {
            // Spawn flame particle from right foot
            double rightVelX = (player.getRandom().nextDouble() - 0.5) * 0.02;
            double rightVelY = player.getRandom().nextDouble() * 0.02 + 0.01;
            double rightVelZ = (player.getRandom().nextDouble() - 0.5) * 0.02;
            
            player.level().addParticle(ParticleTypes.FLAME,
                rightFootX + (player.getRandom().nextDouble() - 0.5) * 0.08,
                rightFootY + (player.getRandom().nextDouble() - 0.5) * 0.08,
                rightFootZ + (player.getRandom().nextDouble() - 0.5) * 0.08,
                rightVelX, rightVelY, rightVelZ);
        } else {
            // Spawn flame particle from left foot
            double leftVelX = (player.getRandom().nextDouble() - 0.5) * 0.02;
            double leftVelY = player.getRandom().nextDouble() * 0.02 + 0.01;
            double leftVelZ = (player.getRandom().nextDouble() - 0.5) * 0.02;
            
            player.level().addParticle(ParticleTypes.FLAME,
                leftFootX + (player.getRandom().nextDouble() - 0.5) * 0.08,
                leftFootY + (player.getRandom().nextDouble() - 0.5) * 0.08,
                leftFootZ + (player.getRandom().nextDouble() - 0.5) * 0.08,
                leftVelX, leftVelY, leftVelZ);
        }
    }
    
    /**
     * Creates a magma platform around the player with optimized batch block placement
     */
    private void createMagmaPlatform(Level level, Player player, int amplifier) {
        UUID playerId = player.getUUID();
        BlockPos playerPos = player.blockPosition();
        Set<BlockPos> newPlatformBlocks = new HashSet<>();
        
        // Determine platform size based on amplifier (tier)
        int platformSize = amplifier == 0 ? TIER_I_SIZE : TIER_II_SIZE;
        int halfSize = platformSize / 2;
        
        // Get the target Y level (same as player's feet)
        int targetY = playerPos.getY() - 1;
        
        // Pre-create the magma block state to avoid repeated calls
        BlockState magmaBlockState = DecayableMagmaBlock.createDecayableMagmaBlock();
        
        // Batch collect all positions that need magma blocks
        Set<BlockPos> positionsToPlace = new HashSet<>();
        
        // Only check blocks that are likely to be lava (optimized targeting)
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                // Skip corner blocks for both tiers
                if (Math.abs(x) == halfSize && Math.abs(z) == halfSize) {
                    continue;
                }
                
                BlockPos checkPos = new BlockPos(playerPos.getX() + x, targetY, playerPos.getZ() + z);
                BlockPos abovePos = checkPos.above();
                
                // Only check if the block is lava with air above
                BlockState blockState = level.getBlockState(checkPos);
                if (blockState.is(Blocks.LAVA)) {
                    BlockState aboveState = level.getBlockState(abovePos);
                    if (aboveState.isAir()) {
                        positionsToPlace.add(checkPos);
                        newPlatformBlocks.add(checkPos);
                    }
                }
            }
        }
        
        // Batch place all magma blocks efficiently
        for (BlockPos pos : positionsToPlace) {
            // Use optimized block placement with proper flags
            level.setBlock(pos, magmaBlockState, 3);
        }
        
        // Update platform cache
        lastPlatformBlocks.put(playerId, newPlatformBlocks);
    }
    
    /**
     * Cleanup method to remove cached data when effect ends
     */
    public static void cleanupPlayerData(UUID playerId) {
        previousBlockPositions.remove(playerId);
        lastPlatformBlocks.remove(playerId);
    }
} 