package net.quoky.lava_potions.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.network.ModPackets;
import net.quoky.lava_potions.network.ShootFireballPacket;

/**
 * Fire Avatar effect that:
 * - Adds fire aspect to all attacks regardless of item held (tier I, II)
 * - Allows shooting fireballs with empty hand (1.5 second cooldown)
 * - Fireballs behave like ghast fireballs with appropriate damage scaling
 * - Provides immunity to own fireballs
 * - Spawns flame particles from player's hands
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class PyromancyEffect extends MobEffect {

    public PyromancyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xe5291f); // Orange-red color
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Tick every 4 ticks (5 times per second) for particle effects
        return duration > 0 && duration % 4 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Only spawn particles for players on server side so all clients can see
        if (!entity.level().isClientSide && entity instanceof Player player) {
            if (!(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            
            // Get player position and body rotation (not head rotation)
            Vec3 playerPos = player.position();
            float bodyYaw = (float) Math.toRadians(player.yBodyRot);
            
            // Calculate body center position (lower waist level)
            double bodyX = playerPos.x;
            double bodyY = playerPos.y + (player.isCrouching() ? 0.45 : 0.75); // Slightly adjusted hand height
            double bodyZ = playerPos.z;
            
            // Calculate forward and right vectors based on body rotation
            double rightX = Math.cos(bodyYaw);
            double rightZ = Math.sin(bodyYaw);
            
            // Hand positions relative to body center
            double handSideOffset = 0.35;    // Distance from center to each hand
            
            // Calculate actual hand positions
            double handDownOffset = 0.05;    // Hands slightly below waist level
            
            double rightHandX = bodyX + (rightX * handSideOffset);
            double rightHandY = bodyY - handDownOffset;
            double rightHandZ = bodyZ + (rightZ * handSideOffset);
            
            double leftHandX = bodyX - (rightX * handSideOffset);
            double leftHandY = bodyY - handDownOffset;
            double leftHandZ = bodyZ - (rightZ * handSideOffset);
            
            // Spawn flame particles from both hands using server method
            // Right hand particle
            serverLevel.sendParticles(ParticleTypes.FLAME,
                rightHandX + (player.getRandom().nextDouble() - 0.5) * 0.08,
                rightHandY + (player.getRandom().nextDouble() - 0.5) * 0.08,
                rightHandZ + (player.getRandom().nextDouble() - 0.5) * 0.08,
                1, // Particle count
                0.0, // X offset
                0.0, // Y offset
                0.0, // Z offset
                0.0); // Speed
            
            // Left hand particle
            serverLevel.sendParticles(ParticleTypes.FLAME,
                leftHandX + (player.getRandom().nextDouble() - 0.5) * 0.08,
                leftHandY + (player.getRandom().nextDouble() - 0.5) * 0.08,
                leftHandZ + (player.getRandom().nextDouble() - 0.5) * 0.08,
                1, // Particle count
                0.0, // X offset
                0.0, // Y offset
                0.0, // Z offset
                0.0); // Speed
        }
    }

    /**
     * Event handler for adding fire aspect to attacks
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            if (attacker.hasEffect(ModEffects.PYROMANCY.get())) {
                int amplifier = attacker.getEffect(ModEffects.PYROMANCY.get()).getAmplifier();
                
                // Set target on fire based on amplifier (tier I = 4 seconds, tier II = 8 seconds)
                int fireDuration = amplifier == 0 ? 4 : 8;

                LivingEntity target = event.getEntity();
                target.setSecondsOnFire(fireDuration);
            }
        }
        
        // Handle fireball damage immunity for Pyromancy users
        if (event.getEntity() instanceof Player player &&
            player.hasEffect(ModEffects.PYROMANCY.get()) &&
            event.getSource().getDirectEntity() instanceof LargeFireball fireball) {
            
            // Check if the fireball was shot by this player
            if (fireball.getOwner() == player) {
                event.setCanceled(true); // Cancel damage from own fireball
            }
        }
    }

    /**
     * Event handler for shooting fireballs with empty hand (left-click empty space)
     * This runs on the client side and sends a packet to the server
     */
    @SubscribeEvent
    public static void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        
        // Only process on client side - this event is client-side only
        if (player.level().isClientSide &&
            player.hasEffect(ModEffects.PYROMANCY.get()) &&
            player.getMainHandItem().isEmpty() &&
            !player.isCrouching()) { // Don't shoot fireball if crouching
            
            // Send packet to server to handle the fireball shooting
            ModPackets.INSTANCE.sendToServer(new ShootFireballPacket());
        }
    }

    /**
     * Event handler for shooting fireballs or placing fire with empty hand (left-click on blocks)
     */
    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        
        if (player.hasEffect(ModEffects.PYROMANCY.get()) && player.getMainHandItem().isEmpty()) {
            if (player.isCrouching()) {
                // Place fire on the clicked face - handle on server side
                if (!player.level().isClientSide) {
                    placeFireOnBlockFace(player, event.getPos(), event.getFace());
                }
                event.setCanceled(true); // Prevent normal block interaction
            } else if (player.level().isClientSide) {
                // Send packet to server to handle the fireball shooting (client side only)
                ModPackets.INSTANCE.sendToServer(new ShootFireballPacket());
            }
        }
    }

    /**
     * Attempts to place fire on the specified face of a block
     */
    private static void placeFireOnBlockFace(Player player, BlockPos clickedPos, Direction face) {
        BlockPos firePos = clickedPos.relative(face);
        BlockState currentState = player.level().getBlockState(firePos);
        BlockState belowState = player.level().getBlockState(firePos.below());
        
        // Check if we can place fire at this position
        if (currentState.isAir() && Blocks.FIRE.canSurvive(Blocks.FIRE.defaultBlockState(), player.level(), firePos)) {
            // Place fire block
            player.level().setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }
}