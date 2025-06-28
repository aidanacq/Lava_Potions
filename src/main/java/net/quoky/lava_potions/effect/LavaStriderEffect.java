package net.quoky.lava_potions.effect;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

/**
 * Effect that provides lava striding capabilities with swimming speed bonuses
 * Tier 1 (amplifier 0): 1.5x faster than normal swimming
 * Tier 2 (amplifier 1): 2.5x faster than normal swimming
 */
public class LavaStriderEffect extends BaseSkinEffect {
    
    // UUID for the swim speed modifier
    private static final UUID SWIM_SPEED_MODIFIER_UUID = UUID.fromString("a8b6c7d4-e5f6-4a8b-9c7d-5e6f7a8b9c0d");
    
    // Speed multipliers for different tiers
    private static final double TIER_1_SWIM_SPEED_MULTIPLIER = 0.25;
    private static final double TIER_2_SWIM_SPEED_MULTIPLIER = 0.75;
    
    public LavaStriderEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x005ff4);
    }
    
    @Override
    public Map<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getAttributeModifiers() {
        return ImmutableMap.of();
    }
    
    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.LivingEntity livingEntity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(livingEntity, attributeMap, amplifier);
        
        // Calculate swim speed multiplier based on amplifier
        double swimSpeedMultiplier = amplifier == 0 ? TIER_1_SWIM_SPEED_MULTIPLIER : TIER_2_SWIM_SPEED_MULTIPLIER;
        
        // Create swim speed modifier
        AttributeModifier swimSpeedModifier = new AttributeModifier(
            SWIM_SPEED_MODIFIER_UUID,
            "Lava Strider Swim Speed",
            swimSpeedMultiplier,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        
        // Apply the swim speed modifier
        if (attributeMap.hasAttribute(ForgeMod.SWIM_SPEED.get())) {
            attributeMap.getInstance(ForgeMod.SWIM_SPEED.get()).addTransientModifier(swimSpeedModifier);
        }
    }
    
    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.LivingEntity livingEntity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(livingEntity, attributeMap, amplifier);
        
        // Remove swim speed modifier
        if (attributeMap.hasAttribute(ForgeMod.SWIM_SPEED.get())) {
            attributeMap.getInstance(ForgeMod.SWIM_SPEED.get()).removeModifier(SWIM_SPEED_MODIFIER_UUID);
        }
    }
}