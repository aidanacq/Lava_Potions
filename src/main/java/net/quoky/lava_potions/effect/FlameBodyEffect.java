package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that sets attackers on fire when they hit the player
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class FlameBodyEffect extends MobEffect {
    public FlameBodyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xDD2200); // Red-orange color
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Not needed for this effect, works through event handler
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // This effect doesn't tick
    }
    
    /**
     * Event handler for when an entity is attacked
     * Sets the attacker on fire if the entity has the Flame Body effect
     */
    @SubscribeEvent
    public static void onEntityAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ModEffects.FLAME_BODY.get())) {
                int amplifier = player.getEffect(ModEffects.FLAME_BODY.get()).getAmplifier();
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity) {
                    // Set attacker on fire - duration depends on potion amplifier
                    attacker.setSecondsOnFire(3 + amplifier * 2);
                }
            }
        }
    }
} 