package net.quoky.lava_potions.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Effect that sets attackers on fire when they are near the player
 */
public class HeatEffect extends MobEffect {
    public HeatEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xf7a236); // Darker flame red color
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Tier 1 (amplifier 0): expand hit box by 2 blocks in all directions
        // Tier 2 (amplifier 1): expand hit box by 2.5 blocks in all directions
        double radius = amplifier == 0 ? 2.0 : 2.5;
        AABB expandedBoundingBox = entity.getBoundingBox().inflate(radius);

        // Spawn smoke particles at random locations within the expanded bounding box
        // (server-side so all clients can see)
        if (!entity.level().isClientSide) {
            spawnParticles(entity, expandedBoundingBox);
        }

        // DO NOT REMOVE - This self-fire logic will be reused elsewhere later
        // Only apply fire if entity is not already on fire, not in water, and not in
        // creative mode
        /*
         * if (entity instanceof Player player) {
         * if (entity.getRemainingFireTicks() <= 0 && !player.isInWater() &&
         * !player.isCreative()) {
         * // Set the entity on fire (they have fire resistance so no damage)
         * entity.setRemainingFireTicks(40); // 2 seconds (40 ticks)
         * }
         * } else if (entity.getRemainingFireTicks() <= 0 && !entity.isInWater()) {
         * // For non-player entities, just check water
         * entity.setRemainingFireTicks(40);
         * }
         */

        if (!entity.level().isClientSide) {
            meltBlocksInAoe(entity, expandedBoundingBox, amplifier);
            List<LivingEntity> entities = entity.level().getEntitiesOfClass(LivingEntity.class, expandedBoundingBox);

            for (LivingEntity target : entities) {
                // Don't damage self
                if (target != entity) {
                    // Check line of sight before applying damage
                    if (hasLineOfSight(entity, target)) {
                        // For Tier I (amplifier 0): 1 damage + 4 seconds on fire
                        // For Tier II (amplifier 1): 3 damage + 7 seconds on fire
                        int fireDuration = amplifier == 0 ? 4 : 7;
                        float damage = amplifier == 0 ? 1.0F : 3.0F;

                        // Apply damage to attacker if they are not fire immune and don't have fire
                        // resistance
                        if (!target.fireImmune() && !target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                            // Check if entity will die from this damage - if so, set them on fire briefly
                            // to ensure cooked meat drops
                            if (target.getHealth() <= damage) {
                                target.setSecondsOnFire(1);
                            }
                            target.hurt(entity.damageSources().generic(), damage);
                        }

                        // Set attacker on fire only if they are not in water
                        if (!target.isInWater()) {
                            target.setSecondsOnFire(fireDuration);
                        }
                    }
                }
            }
        }
    }

    /**
     * Melt meltable blocks in a given area of effect
     * Tier 1 (amplifier 0) melts snow, ice, frosted ice, and powdered snow
     * Tier 2 (amplifier 1) also melts packed ice
     * 
     * @param entity      The entity with the effect
     * @param expandedBox The area of effect
     * @param amplifier   The amplifier of the effect
     */
    private void meltBlocksInAoe(LivingEntity entity, AABB expandedBox, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide) {
            return;
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                (int) Math.floor(expandedBox.minX),
                (int) Math.floor(expandedBox.minY),
                (int) Math.floor(expandedBox.minZ),
                (int) Math.floor(expandedBox.maxX),
                (int) Math.floor(expandedBox.maxY),
                (int) Math.floor(expandedBox.maxZ))) {
            BlockState blockState = level.getBlockState(pos);
            Block block = blockState.getBlock();

            if (block == Blocks.ICE || block == Blocks.FROSTED_ICE) {
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            } else if (block == Blocks.SNOW || block == Blocks.POWDER_SNOW) {
                level.removeBlock(pos, false);
            }
        }
    }

    /**
     * Spawn smoke particles at random locations within the expanded hitbox
     * Only spawns particles in air blocks to ensure proper visibility
     * Also spawns bubble particles in water blocks and bubble pop particles above
     * water surfaces
     * Particles will not spawn inside the effect user's hitbox
     *
     * @param entity      The entity with the flame aura effect
     * @param expandedBox The pre-calculated expanded bounding box
     */
    private void spawnParticles(LivingEntity entity, AABB expandedBox) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Get the entity's hitbox for exclusion checks
        AABB entityHitbox = entity.getBoundingBox();

        // Attempt to spawn 9 bubble particles every tick
        for (int i = 0; i < 9; i++) {
            // Try up to 10 times to find a valid water block position
            for (int attempt = 0; attempt < 10; attempt++) {
                // Generate random position within the expanded bounding box
                double randomX = expandedBox.minX + entity.getRandom().nextDouble() * expandedBox.getXsize();
                double randomY = expandedBox.minY + entity.getRandom().nextDouble() * expandedBox.getYsize();
                double randomZ = expandedBox.minZ + entity.getRandom().nextDouble() * expandedBox.getZsize();

                // Skip if position is inside the entity's hitbox
                if (isPositionInsideHitbox(randomX, randomY, randomZ, entityHitbox)) {
                    continue;
                }

                BlockPos particlePos = BlockPos.containing(randomX, randomY, randomZ);
                BlockState blockState = serverLevel.getBlockState(particlePos);

                // Check if the position is in a water source block OR flowing water
                if (blockState.getBlock() == Blocks.WATER) {
                    boolean isSource = blockState.getFluidState().isSource();

                    if (isSource) {
                        // Still water - spawn anywhere in the block
                        serverLevel.sendParticles(
                            ParticleTypes.BUBBLE,
                            randomX,
                            randomY,
                            randomZ,
                            1, // Particle count
                            0.0, // X offset
                            0.0, // Y offset
                            0.0, // Z offset
                            0.0 // Speed
                        );
                        break; // Successfully spawned particle, exit attempt loop
                    } else {
                        // Flowing water - check height restriction based on flow level
                        int flowLevel = blockState.getFluidState().getAmount();
                        if (flowLevel > 0) {
                            // Calculate allowed height: flow_level + flow_level/2 + 1 pixels from bottom
                            double allowedHeightPixels = flowLevel + (flowLevel / 2.0) + 1;
                            double allowedHeightRatio = allowedHeightPixels / 16.0; // Convert pixels to block ratio

                            // Calculate the relative Y position within the block (0.0 to 1.0)
                            double relativeY = randomY - particlePos.getY();

                            // Check if within the allowed height from bottom
                            if (relativeY <= allowedHeightRatio) {
                                // Spawn bubble particle in flowing water
                                serverLevel.sendParticles(
                                    ParticleTypes.BUBBLE,
                                    randomX,
                                    randomY,
                                    randomZ,
                                    1, // Particle count
                                    0.0, // X offset
                                    0.0, // Y offset
                                    0.0, // Z offset
                                    0.0 // Speed
                                );
                                break; // Successfully spawned particle, exit attempt loop
                            }
                        }
                    }
                }
            }
        }

        // Attempt to spawn 40 bubble pop particles every tick
        for (int i = 0; i < 40; i++) {
            // Try up to 10 times to find a valid water surface position
            for (int attempt = 0; attempt < 10; attempt++) {
                // Generate random position within the expanded bounding box
                double randomX = expandedBox.minX + entity.getRandom().nextDouble() * expandedBox.getXsize();
                double randomY = expandedBox.minY + entity.getRandom().nextDouble() * expandedBox.getYsize();
                double randomZ = expandedBox.minZ + entity.getRandom().nextDouble() * expandedBox.getZsize();

                // Skip if position is inside the entity's hitbox
                if (isPositionInsideHitbox(randomX, randomY, randomZ, entityHitbox)) {
                    continue;
                }

                BlockPos particlePos = BlockPos.containing(randomX, randomY, randomZ);
                BlockState blockState = serverLevel.getBlockState(particlePos);

                // Check if the position is in a water block AND within the top 3 pixels
                if (blockState.getBlock() == Blocks.WATER) {
                    boolean isSource = blockState.getFluidState().isSource();

                    if (isSource) {
                        // Still water - check top 3 pixels
                        double relativeY = randomY - particlePos.getY();

                        // Check if within the top 3 pixels (top 0.187 of the block)
                        if (relativeY >= 0.813) { // 1.0 - 0.187 = 0.813
                            // Check if the block above is air
                            BlockPos abovePos = particlePos.above();
                            if (serverLevel.getBlockState(abovePos).isAir()) {
                                // Spawn bubble pop particle
                                serverLevel.sendParticles(
                                    ParticleTypes.BUBBLE_POP,
                                    randomX,
                                    randomY,
                                    randomZ,
                                    1, // Particle count
                                    0.0, // X offset
                                    0.0, // Y offset
                                    0.0, // Z offset
                                    0.0 // Speed
                                );
                                break; // Successfully spawned particle, exit attempt loop
                            }
                        }
                    } else {
                        // Flowing water - check height restriction based on flow level
                        int flowLevel = blockState.getFluidState().getAmount();
                        if (flowLevel > 0) {
                            // Calculate allowed height: flow_level + flow_level/2 + 1 pixels from bottom
                            double allowedHeightPixels = flowLevel + (flowLevel / 2.0) + 1;
                            double allowedHeightRatio = allowedHeightPixels / 16.0; // Convert pixels to block ratio

                            // Calculate the relative Y position within the block (0.0 to 1.0)
                            double relativeY = randomY - particlePos.getY();

                            // Check if within the allowed height from bottom AND within top 3 pixels of
                            // allowed area
                            if (relativeY <= allowedHeightRatio) {
                                // For flowing water, check if in top 3 pixels of the allowed area
                                double topThreePixelsOfAllowed = Math.max(0, allowedHeightRatio - (3.0 / 16.0));
                                if (relativeY >= topThreePixelsOfAllowed) {
                                    // Check if the block above is air
                                    BlockPos abovePos = particlePos.above();
                                    if (serverLevel.getBlockState(abovePos).isAir()) {
                                        // Spawn bubble pop particle in flowing water
                                        serverLevel.sendParticles(
                                            ParticleTypes.BUBBLE_POP,
                                            randomX,
                                            randomY,
                                            randomZ,
                                            1, // Particle count
                                            0.0, // X offset
                                            0.0, // Y offset
                                            0.0, // Z offset
                                            0.0 // Speed
                                        );
                                        break; // Successfully spawned particle, exit attempt loop
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Attempt to spawn 1 poof particle every tick
        for (int i = 0; i < 1; i++) {
            // Try up to 10 times to find a valid water surface position
            for (int attempt = 0; attempt < 10; attempt++) {
                // Generate random position within the expanded bounding box
                double randomX = expandedBox.minX + entity.getRandom().nextDouble() * expandedBox.getXsize();
                double randomY = expandedBox.minY + entity.getRandom().nextDouble() * expandedBox.getYsize();
                double randomZ = expandedBox.minZ + entity.getRandom().nextDouble() * expandedBox.getZsize();

                // Skip if position is inside the entity's hitbox
                if (isPositionInsideHitbox(randomX, randomY, randomZ, entityHitbox)) {
                    continue;
                }

                BlockPos particlePos = BlockPos.containing(randomX, randomY, randomZ);
                BlockState blockState = serverLevel.getBlockState(particlePos);

                // Check if the position is in a water block AND within the top 3 pixels
                if (blockState.getBlock() == Blocks.WATER) {
                    boolean isSource = blockState.getFluidState().isSource();

                    if (isSource) {
                        // Still water - check top 3 pixels
                        double relativeY = randomY - particlePos.getY();

                        // Check if within the top 3 pixels (top 0.187 of the block)
                        if (relativeY >= 0.813) { // 1.0 - 0.187 = 0.813
                            // Check if the block above is air
                            BlockPos abovePos = particlePos.above();
                            if (serverLevel.getBlockState(abovePos).isAir()) {
                                // Spawn poof particle
                                serverLevel.sendParticles(
                                    ParticleTypes.POOF,
                                    randomX,
                                    randomY,
                                    randomZ,
                                    1, // Particle count
                                    0.0, // X offset
                                    0.0, // Y offset
                                    0.0, // Z offset
                                    0.0 // Speed
                                );
                                break; // Successfully spawned particle, exit attempt loop
                            }
                        }
                    } else {
                        // Flowing water - check height restriction based on flow level
                        int flowLevel = blockState.getFluidState().getAmount();
                        if (flowLevel > 0) {
                            // Calculate allowed height: flow_level + flow_level/2 + 1 pixels from bottom
                            double allowedHeightPixels = flowLevel + (flowLevel / 2.0) + 1;
                            double allowedHeightRatio = allowedHeightPixels / 16.0; // Convert pixels to block ratio

                            // Calculate the relative Y position within the block (0.0 to 1.0)
                            double relativeY = randomY - particlePos.getY();

                            // Check if within the allowed height from bottom AND within top 3 pixels of
                            // allowed area
                            if (relativeY <= allowedHeightRatio) {
                                // For flowing water, check if in top 3 pixels of the allowed area
                                double topThreePixelsOfAllowed = Math.max(0, allowedHeightRatio - (3.0 / 16.0));
                                if (relativeY >= topThreePixelsOfAllowed) {
                                    // Check if the block above is air
                                    BlockPos abovePos = particlePos.above();
                                    if (serverLevel.getBlockState(abovePos).isAir()) {
                                        // Spawn poof particle in flowing water
                                        serverLevel.sendParticles(
                                            ParticleTypes.POOF,
                                            randomX,
                                            randomY,
                                            randomZ,
                                            1, // Particle count
                                            0.0, // X offset
                                            0.0, // Y offset
                                            0.0, // Z offset
                                            0.0 // Speed
                                        );
                                        break; // Successfully spawned particle, exit attempt loop
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Attempt to spawn 3 smoke particles every tick
        for (int i = 0; i < 3; i++) {
            // Try up to 10 times to find a valid air block position
            for (int attempt = 0; attempt < 10; attempt++) {
                // Generate random position within the expanded bounding box
                double randomX = expandedBox.minX + entity.getRandom().nextDouble() * expandedBox.getXsize();
                double randomY = expandedBox.minY + entity.getRandom().nextDouble() * expandedBox.getYsize();
                double randomZ = expandedBox.minZ + entity.getRandom().nextDouble() * expandedBox.getZsize();

                // Skip if position is inside the entity's hitbox
                if (isPositionInsideHitbox(randomX, randomY, randomZ, entityHitbox)) {
                    continue;
                }

                BlockPos particlePos = BlockPos.containing(randomX, randomY, randomZ);

                // Check if the position is in an air block
                if (serverLevel.getBlockState(particlePos).isAir()) {
                    // Spawn smoke particle using server method to sync to all clients
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        randomX,
                        randomY,
                        randomZ,
                        1, // Particle count
                        0.0, // X offset
                        0.0, // Y offset
                        0.0, // Z offset
                        0.0 // Speed
                    );
                    break; // Successfully spawned particle, exit attempt loop
                }
            }
        }

        // Attempt to spawn 1 flame particle every 5th tick
        if (entity.tickCount % 5 == 0) {
            // Try up to 10 times to find a valid air block position
            for (int attempt = 0; attempt < 10; attempt++) {
                // Generate random position within the expanded bounding box
                double randomX = expandedBox.minX + entity.getRandom().nextDouble() * expandedBox.getXsize();
                double randomY = expandedBox.minY + entity.getRandom().nextDouble() * expandedBox.getYsize();
                double randomZ = expandedBox.minZ + entity.getRandom().nextDouble() * expandedBox.getZsize();

                // Skip if position is inside the entity's hitbox
                if (isPositionInsideHitbox(randomX, randomY, randomZ, entityHitbox)) {
                    continue;
                }

                BlockPos particlePos = BlockPos.containing(randomX, randomY, randomZ);

                // Check if the position is in an air block
                if (serverLevel.getBlockState(particlePos).isAir()) {
                    // Spawn flame particle using server method to sync to all clients
                    serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        randomX,
                        randomY,
                        randomZ,
                        1, // Particle count
                        0.0, // X offset
                        0.0, // Y offset
                        0.0, // Z offset
                        0.0 // Speed
                    );
                    break; // Successfully spawned particle, exit attempt loop
                }
            }
        }
    }

    /**
     * Check if a position is inside the given hitbox
     * 
     * @param x The X coordinate to check
     * @param y The Y coordinate to check
     * @param z The Z coordinate to check
     * @param hitbox The bounding box to check against
     * @return true if the position is inside the hitbox, false otherwise
     */
    private boolean isPositionInsideHitbox(double x, double y, double z, AABB hitbox) {
        return x >= hitbox.minX && x <= hitbox.maxX &&
               y >= hitbox.minY && y <= hitbox.maxY &&
               z >= hitbox.minZ && z <= hitbox.maxZ;
    }

    /**
     * Check if there is a clear line of sight between two entities
     * Performs comprehensive 9-point checks: from each point (top, middle, bottom) of the 
     * effect user's hitbox to each point (top, middle, bottom) of the target's hitbox
     * Line of sight is not blocked by fluids (water, lava, etc.) but is blocked by solid blocks
     * 
     * @param from The source entity
     * @param to   The target entity
     * @return true if there is a clear line of sight on any of the nine checks, false if all are blocked
     */
    private boolean hasLineOfSight(LivingEntity from, LivingEntity to) {
        AABB fromBB = from.getBoundingBox();
        AABB toBB = to.getBoundingBox();

        // Calculate the three Y positions for each entity's hitbox
        double fromTopY = fromBB.maxY;
        double fromMidY = (fromBB.minY + fromBB.maxY) / 2.0;
        double fromBottomY = fromBB.minY;

        double toTopY = toBB.maxY;
        double toMidY = (toBB.minY + toBB.maxY) / 2.0;
        double toBottomY = toBB.minY;

        // Create position vectors for all three levels of each entity
        Vec3 fromTopPos = new Vec3(fromBB.getCenter().x, fromTopY, fromBB.getCenter().z);
        Vec3 fromMidPos = new Vec3(fromBB.getCenter().x, fromMidY, fromBB.getCenter().z);
        Vec3 fromBottomPos = new Vec3(fromBB.getCenter().x, fromBottomY, fromBB.getCenter().z);

        Vec3 toTopPos = new Vec3(toBB.getCenter().x, toTopY, toBB.getCenter().z);
        Vec3 toMidPos = new Vec3(toBB.getCenter().x, toMidY, toBB.getCenter().z);
        Vec3 toBottomPos = new Vec3(toBB.getCenter().x, toBottomY, toBB.getCenter().z);

        // Perform all 9 line-of-sight checks
        Vec3[] fromPositions = {fromTopPos, fromMidPos, fromBottomPos};
        Vec3[] toPositions = {toTopPos, toMidPos, toBottomPos};

        for (Vec3 fromPos : fromPositions) {
            for (Vec3 toPos : toPositions) {
                BlockHitResult hitResult = from.level().clip(new ClipContext(
                        fromPos, toPos,
                        ClipContext.Block.COLLIDER, // Check for collision blocks (walls)
                        ClipContext.Fluid.NONE, // Allow line-of-sight through fluids (water, lava, etc.)
                        from));

                if (hitResult.getType() == HitResult.Type.MISS) {
                    return true; // At least one line of sight is clear
                }
            }
        }

        // All 9 checks failed - no line of sight
        return false;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Run damage checks every tick for responsiveness
        return true;
    }
}