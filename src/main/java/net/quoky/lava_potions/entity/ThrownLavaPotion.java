package net.quoky.lava_potions.entity;

import java.util.List;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.ModPotionBrewingRecipes;

public class ThrownLavaPotion extends ThrowableItemProjectile implements ItemSupplier {
    
    public ThrownLavaPotion(EntityType<? extends ThrownLavaPotion> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownLavaPotion(Level level, LivingEntity entity) {
        super(ModEntityTypes.THROWN_LAVA_POTION.get(), entity, level);
        // Adjust the throw velocity to match vanilla potions
        double velocityFactor = 0.5;  // Standard potion throw speed
        
        // Set velocity based on the thrower's look direction and a fixed speed
        this.setDeltaMovement(
            -Math.sin(entity.getYRot() * ((float)Math.PI / 180F)) * Math.cos(entity.getXRot() * ((float)Math.PI / 180F)) * velocityFactor,
            -Math.sin(entity.getXRot() * ((float)Math.PI / 180F)) * velocityFactor - 0.1F,
            Math.cos(entity.getYRot() * ((float)Math.PI / 180F)) * Math.cos(entity.getXRot() * ((float)Math.PI / 180F)) * velocityFactor
        );
    }

    public ThrownLavaPotion(Level level, double x, double y, double z) {
        super(ModEntityTypes.THROWN_LAVA_POTION.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.POTION;
    }

    private ParticleOptions getParticle() {
        return ParticleTypes.FLAME;
    }
    
    // Match vanilla potion gravity
    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            Potion potion = PotionUtils.getPotion(itemstack);

            // Use the potion directly - no need for getRegularVariant since we only have base types
            Potion basePotion = potion;

            // Splash effect radius
            float radius = 4.0F;

            // Check if this is a lingering potion based on the item stack NBT or custom logic
            boolean isLingering = isLingeringPotion(itemstack);

            // Apply immediate fire effect to all living entities in radius for both splash and lingering potions
            List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(radius, radius, radius));

            for (LivingEntity entity : affectedEntities) {
                double distance = this.distanceToSqr(entity);

                if (distance < radius * radius) {
                    // Apply effects based on potion type
                    if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
                        // Set entities on fire for 5 seconds for basic lava potions
                        entity.setSecondsOnFire(5);
                    } else if (ModPotionTypes.isEffectLavaPotion(basePotion)) {
                        // Apply potion effects based on the potion type
                        double distanceSquared = this.distanceToSqr(entity);
                        if (distanceSquared < (radius * 0.75) * (radius * 0.75)) {
                            // Apply potion effects within 75% of the radius for more concentrated effect
                            if (!this.level().isClientSide) {
                                List<MobEffectInstance> effects = potion.getEffects();
                                for (MobEffectInstance effect : effects) {
                                    // Create a new instance of the effect to ensure it has the correct duration
                                    MobEffectInstance newEffect = new MobEffectInstance(
                                        effect.getEffect(),
                                        effect.getDuration(),
                                        effect.getAmplifier(),
                                        effect.isAmbient(),
                                        effect.isVisible()
                                    );
                                    entity.addEffect(newEffect);
                                }
                            }
                        }
                    }
                }
            }

            // Apply impact damage only for basic lava potions (lava bottle and awkward lava)
            if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
                List<LivingEntity> entitiesForDamage = this.level().getEntitiesOfClass(LivingEntity.class,
                        this.getBoundingBox().inflate(radius, radius, radius));

                Entity owner = this.getOwner();

                for (LivingEntity entity : entitiesForDamage) {
                    // Apply damage to all entities
                    double distanceSquared = this.distanceToSqr(entity);
                    if (distanceSquared < radius * radius) {
                        // Calculate distance-based damage
                        // Direct hit (distance 0) = 8 hit points
                        // Maximum range (distance 4) = 4 hit points
                        // Damage decreases by 1 hit point per unit distance
                        double distance = Math.sqrt(distanceSquared);
                        float damage = Math.max(4.0F, 8.0F - (float)distance);
                        // Check if entity will die from this damage
                        if (entity.getHealth() <= damage) {
                            entity.setSecondsOnFire(1);
                        }

                        // Don't damage fire immune entities
                        if (!entity.fireImmune()) {
                            // Use thorns damage to provide knockback
                            entity.hurt(entity.damageSources().thorns(this), damage);
                        }
                    }
                }
            }

            // Create additional lingering effect if it's a lingering potion
            if (isLingering) {
                createAreaEffectCloud(itemstack, potion, basePotion);
            }

            // Trigger splash particles via entity event
            this.level().broadcastEntityEvent(this, (byte)3);

            // Play glass breaking sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 
                this.random.nextFloat() * 0.1F + 0.9F);

            // Always break the potion
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        Entity target = hitResult.getEntity();
        if (target instanceof LivingEntity livingEntity) {
            ItemStack itemstack = this.getItem();
            Potion potion = PotionUtils.getPotion(itemstack);
            Potion basePotion = potion; // Use potion directly
            Entity owner = this.getOwner();

            // Apply direct hit damage only for basic lava potions
            if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
                // Check if entity will die from this damage
                if (livingEntity.getHealth() <= 6.0F) {
                    livingEntity.setSecondsOnFire(1);
                }
                // Apply direct hit damage of 6 hit points (3 hearts) but don't damage fire immune entities
                if (!livingEntity.fireImmune()) {
                    // Use thorns damage to provide knockback
                    livingEntity.hurt(livingEntity.damageSources().thorns(this), 6.0F);
                }
                // Basic lava bottle sets entity on fire for longer (10 seconds)
                livingEntity.setSecondsOnFire(10);
            } else if (ModPotionTypes.isEffectLavaPotion(basePotion)) {
                // Apply potion effects directly on hit
                if (!this.level().isClientSide) {
                    List<MobEffectInstance> effects = potion.getEffects();
                    for (MobEffectInstance effect : effects) {
                        // Create a new instance of the effect to ensure it has the correct duration
                        MobEffectInstance newEffect = new MobEffectInstance(
                            effect.getEffect(),
                            effect.getDuration(),
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible()
                        );
                        livingEntity.addEffect(newEffect);
                    }
                }
            }
        }
    }

    /**
     * Check if this is a lingering potion based on item type
     */
    private boolean isLingeringPotion(ItemStack itemStack) {
        // Check if it's a vanilla lingering potion item
        return itemStack.getItem() == Items.LINGERING_POTION;
    }

    private void createAreaEffectCloud(ItemStack itemStack, Potion potion, Potion basePotion) {
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ()) {
            // Override to set entities on fire and apply damage when they're in the lingering cloud
            @Override
            public void tick() {
                super.tick();
                
                // Check for living entities in the cloud every 10 ticks (0.5 seconds)
                if (this.tickCount % 10 == 0) {
                    List<LivingEntity> entitiesInCloud = this.level().getEntitiesOfClass(LivingEntity.class, 
                            this.getBoundingBox());
                    
                    Entity owner = this.getOwner();
                    
                    for (LivingEntity entity : entitiesInCloud) {
                        // Apply effects based on potion type
                        if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
                            // Apply damage to all entities, including the owner
                            // Check if entity will die from this damage
                            if (entity.getHealth() <= 2.0F) {
                                entity.setSecondsOnFire(1);
                            }
                            // Apply flat 2 hit points of damage for lingering cloud (basic potions only)
                            // Don't damage fire immune entities
                            if (!entity.fireImmune()) {
                                // Use thorns damage to provide knockback
                                entity.hurt(entity.damageSources().thorns(this), 2.0F);
                            }
                            // Set entities on fire for 5 seconds
                            entity.setSecondsOnFire(5);
                        } else if (ModPotionTypes.isEffectLavaPotion(basePotion)) {
                            // Apply potion effects every 20 ticks (1 second)
                            if (this.tickCount % 20 == 0 && !this.level().isClientSide) {
                                // Apply the potion effects
                                List<MobEffectInstance> effects = potion.getEffects();
                                for (MobEffectInstance effect : effects) {
                                    // Apply with shorter duration (20% of normal) for lingering effect
                                    int shorterDuration = Math.max(20, effect.getDuration() / 5);
                                    // Create a new effect instance with a fresh duration
                                    MobEffectInstance newEffect = new MobEffectInstance(
                                        effect.getEffect(),
                                        shorterDuration,
                                        effect.getAmplifier(),
                                        effect.isAmbient(),
                                        effect.isVisible()
                                    );
                                    entity.addEffect(newEffect);
                                }
                            }
                        }
                    }
                }
            }
        };
        
        Entity owner = this.getOwner();
        
        if (owner instanceof LivingEntity) {
            cloud.setOwner((LivingEntity)owner);
        }
        
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        cloud.setDuration(300); // 15 seconds
        
        // Set the cloud particle type and color based on potion type
        if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
            // Basic lava potions use flame particles
            cloud.setParticle(ParticleTypes.FLAME);
            cloud.setFixedColor(0xFF5500);
        } else {
            // Get the appropriate color for the effect potion
            int color = 0xFFFFFF;
            if (basePotion == ModPotionTypes.OBSIDIAN_SKIN.get() || basePotion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                color = 0x8e5de3; // Purple for obsidian skin
            } else if (basePotion == ModPotionTypes.NETHERITE_SKIN.get() || basePotion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                color = 0x786561; // Light brown for netherite skin
            } else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                color = 0xebf9fc; // Light blue for glass skin
            } else if (basePotion == ModPotionTypes.FLAME_AURA.get() || basePotion == ModPotionTypes.FLAME_AURA_LONG.get() ||
                        basePotion == ModPotionTypes.FLAME_AURA_STRONG.get()) {
                color = 0xad3c36; // Red for flame aura
            } else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                color = 0xe0c122; // Gold/amber for flammability
            }
            
            // For effect potions, use vanilla particle effect with correct tint
            cloud.setParticle(ParticleTypes.ENTITY_EFFECT);
            cloud.setFixedColor(color);
        }
        
        this.level().addFreshEntity(cloud);
    }
    
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemStack itemstack = this.getItem();
            Potion potion = PotionUtils.getPotion(itemstack);
            Potion basePotion = potion; // Use potion directly
            
            if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
                // Basic potions: explosion particle + burst of flame particles + ring of lava particles
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
                
                // Create a burst of flame particles
                for (int i = 0; i < 60; i++) {
                    double offsetX = this.random.nextGaussian() * 0.15;
                    double offsetY = this.random.nextGaussian() * 0.15;
                    double offsetZ = this.random.nextGaussian() * 0.15;
                    
                    this.level().addParticle(
                        ParticleTypes.FLAME,
                        this.getX(), 
                        this.getY(), 
                        this.getZ(),
                        offsetX, 
                        offsetY + 0.1, 
                        offsetZ
                    );
                }
                
                // Create a circular ring of lava particles
                double radius = 1.5;
                for (int i = 0; i < 36; i++) {
                    double angle = Math.toRadians(i * 10);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    this.level().addParticle(
                        ParticleTypes.LAVA,
                        this.getX() + x, 
                        this.getY(), 
                        this.getZ() + z,
                        0, 
                        0.1, 
                        0
                    );
                }
            } else if (ModPotionTypes.isEffectLavaPotion(basePotion)) {
                // Effect potions: specific particle effects
                this.level().addParticle(ParticleTypes.EXPLOSION, 
                        this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
                
                // Create a smaller burst of flame particles mixed with potion-colored particles for effect potions
                // Get color based on potion type
                float burstR = 1.0f;
                float burstG = 1.0f;
                float burstB = 1.0f;
                
                if (basePotion == ModPotionTypes.OBSIDIAN_SKIN.get() || basePotion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                    // Purple color for obsidian skin (0x8e5de3)
                    burstR = 0.557f; // 142/255
                    burstG = 0.365f; // 93/255
                    burstB = 0.890f; // 227/255
                } else if (basePotion == ModPotionTypes.NETHERITE_SKIN.get() || basePotion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                    // Light brown color for netherite skin (0x786561)
                    burstR = 0.471f; // 186/255
                    burstG = 0.396f; // 130/255
                    burstB = 0.380f; // 95/255
                } else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                    // Light blue color for glass skin (0xebf9fc)
                    burstR = 0.922f; // 235/255
                    burstG = 0.976f; // 249/255
                    burstB = 0.988f; // 252/255
                } else if (basePotion == ModPotionTypes.FLAME_AURA.get() || basePotion == ModPotionTypes.FLAME_AURA_LONG.get() ||
                           basePotion == ModPotionTypes.FLAME_AURA_STRONG.get()) {
                    // Red color for flame aura (0xad3c36)
                    burstR = 0.678f; // 173/255
                    burstG = 0.235f; // 60/255
                    burstB = 0.212f; // 54/255
                } else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                    // Gold/amber color for flammability (0xe0c122)
                    burstR = 0.878f; // 222/255
                    burstG = 0.757f; // 191/255
                    burstB = 0.133f; // 34/255
                }
                
                // Create 30 flame particles
                for (int i = 0; i < 30; i++) {
                    double offsetX = this.random.nextGaussian() * 0.15;
                    double offsetY = this.random.nextGaussian() * 0.15;
                    double offsetZ = this.random.nextGaussian() * 0.15;
                    
                    this.level().addParticle(
                        ParticleTypes.FLAME,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        offsetX,
                        offsetY + 0.1,
                        offsetZ
                    );
                }
                
                // Create 20 potion-colored particles
                for (int i = 0; i < 20; i++) {
                    double offsetX = this.random.nextGaussian() * 0.15;
                    double offsetY = this.random.nextGaussian() * 0.15;
                    double offsetZ = this.random.nextGaussian() * 0.15;
                    
                    this.level().addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        burstR,
                        burstG,
                        burstB
                    );
                }
                
                // Create a ring of potion particles with appropriate color
                double radius = 1.5;
                for (int i = 0; i < 36; i++) {
                    double angle = Math.toRadians(i * 10);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    // Get color based on potion type
                    float ringR = 1.0f;
                    float ringG = 1.0f;
                    float ringB = 1.0f;
                    
                    if (basePotion == ModPotionTypes.OBSIDIAN_SKIN.get() || basePotion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                        // Purple color for obsidian skin (0x8e5de3)
                        ringR = 0.557f; // 142/255
                        ringG = 0.365f; // 93/255
                        ringB = 0.890f; // 227/255
                    } else if (basePotion == ModPotionTypes.NETHERITE_SKIN.get() || basePotion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                        // Light brown color for netherite skin (0x786561)
                        ringR = 0.471f; // 186/255
                        ringG = 0.396f; // 130/255
                        ringB = 0.380f; // 95/255
                    } else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                        // Light blue color for glass skin (0xebf9fc)
                        ringR = 0.922f; // 235/255
                        ringG = 0.976f; // 249/255
                        ringB = 0.988f; // 252/255
                    } else if (basePotion == ModPotionTypes.FLAME_AURA.get() || basePotion == ModPotionTypes.FLAME_AURA_LONG.get() ||
                              basePotion == ModPotionTypes.FLAME_AURA_STRONG.get()) {
                        // Red color for flame aura (0xad3c36)
                        ringR = 0.678f; // 173/255
                        ringG = 0.235f; // 60/255
                        ringB = 0.212f; // 54/255
                    } else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                        // Gold/amber color for flammability (0xe0c122)
                        ringR = 0.878f; // 222/255
                        ringG = 0.757f; // 191/255
                        ringB = 0.133f; // 34/255
                    }
                    
                    this.level().addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        this.getX() + x, 
                        this.getY(), 
                        this.getZ() + z,
                        ringR, 
                        ringG, 
                        ringB
                    );
                }
                
                // No additional potion-specific particle effects
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public ItemStack getItem() {
        ItemStack itemstack = this.getItemRaw();
        // Use vanilla potion with lava type as default instead of custom item
        return itemstack.isEmpty() ? ModPotionBrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()) : itemstack;
    }
} 