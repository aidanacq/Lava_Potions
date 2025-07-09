package net.quoky.lava_potions.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Effect that creates magma platforms when walking on lava
 * Tier I: 5x5 square with corners removed (21 blocks)
 * Tier II: 7x7 square with corners removed (45 blocks)
 * Grants speed when standing on magma blocks
 * Spawns flame particles at the player's feet
 * Uses native Minecraft systems for platform creation
 */
public class MagmaWalkerEffect extends MobEffect {

    // Constants for platform sizes
    private static final int TIER_I_SIZE = 5;
    private static final int TIER_II_SIZE = 7;

    public MagmaWalkerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xd05c00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Run particle effects every 4 ticks (5 times per second)
        // Platform creation is handled by movement events, not ticks
        return duration > 0 && duration % 4 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        BlockPos entityPos = entity.blockPosition();

        // Check if entity is standing on a magma block
        BlockPos groundPos = entityPos.below();
        BlockState groundState = level.getBlockState(groundPos);

        // Apply speed effect when standing on magma blocks
        if (groundState.is(Blocks.MAGMA_BLOCK) || groundState.getBlock() instanceof DecayableMagmaBlock) {
            int speedLevel = amplifier == 0 ? 0 : 1; // Tier I: Speed I, Tier II+: Speed II
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, speedLevel,
                    false, false));
        }

        // Spawn flame particles only for players (server-side so all clients can see)
        if (!level.isClientSide && entity instanceof Player player) {
            spawnFlameParticles(player);
        }
    }

    /**
     * Creates a magma platform around the player using native block replacement
     * logic
     * This method is called by the event handler when the player moves
     */
    public void createMagmaPlatform(Level level, Player player, int amplifier) {
        if (level.isClientSide) {
            return;
        }

        BlockPos playerPos = player.blockPosition();

        // Determine platform size based on amplifier (tier)
        int platformSize = amplifier == 0 ? TIER_I_SIZE : TIER_II_SIZE;
        int halfSize = platformSize / 2;

        // Get the target Y level (same as player's feet)
        int targetY = playerPos.getY() - 1;

        // Pre-create the magma block state
        BlockState magmaBlockState = DecayableMagmaBlock.createDecayableMagmaBlock();

        // Simple block replacement using native-style predicates and logic
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                // Skip corner blocks for both tiers
                if (Math.abs(x) == halfSize && Math.abs(z) == halfSize) {
                    continue;
                }

                BlockPos checkPos = new BlockPos(playerPos.getX() + x, targetY, playerPos.getZ() + z);
                BlockPos abovePos = checkPos.above();

                // Simple predicate checks (similar to ReplaceDisk logic)
                BlockState blockState = level.getBlockState(checkPos);
                BlockState aboveState = level.getBlockState(abovePos);

                // Check if position matches our criteria: lava block with air above
                if (blockState.is(Blocks.LAVA) && blockState.getFluidState().isSource() && aboveState.isAir()) {
                    // Replace lava source with decayable magma block
                    level.setBlock(checkPos, magmaBlockState, 3);
                }
            }
        }
    }

    /**
     * Spawns flame particles at the player's feet
     */
    private void spawnFlameParticles(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

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
        double footSideOffset = 0.15; // Distance from center to each foot

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
            // Spawn flame particle from right foot using server method
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    rightFootX + (player.getRandom().nextDouble() - 0.5) * 0.08,
                    rightFootY + (player.getRandom().nextDouble() - 0.5) * 0.08,
                    rightFootZ + (player.getRandom().nextDouble() - 0.5) * 0.08,
                    1, // Particle count
                    0.0, // X offset
                    0.0, // Y offset
                    0.0, // Z offset
                    0.0); // Speed (we'll use velocity instead)
        } else {
            // Spawn flame particle from left foot using server method
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    leftFootX + (player.getRandom().nextDouble() - 0.5) * 0.08,
                    leftFootY + (player.getRandom().nextDouble() - 0.5) * 0.08,
                    leftFootZ + (player.getRandom().nextDouble() - 0.5) * 0.08,
                    1, // Particle count
                    0.0, // X offset
                    0.0, // Y offset
                    0.0, // Z offset
                    0.0); // Speed (we'll use velocity instead)
        }
    }
}