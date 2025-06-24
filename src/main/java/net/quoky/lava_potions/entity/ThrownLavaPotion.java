package net.quoky.lava_potions.entity;

import java.util.List;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;

public class ThrownLavaPotion extends ThrowableItemProjectile {
    
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
                            if (owner instanceof LivingEntity) {
                                entity.hurt(this.damageSources().thorns(owner), damage);
                            } else {
                                entity.hurt(this.damageSources().magic(), damage);
                            }
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
                    if (owner instanceof LivingEntity) {
                        livingEntity.hurt(this.damageSources().thorns(owner), 6.0F);
                    } else {
                        livingEntity.hurt(this.damageSources().magic(), 6.0F);
                    }
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
                                if (owner instanceof LivingEntity) {
                                    entity.hurt(this.damageSources().thorns(owner), 2.0F);
                                } else {
                                    entity.hurt(this.damageSources().magic(), 2.0F);
                                }
                            }
                            // Set entities on fire for 5 seconds
                            entity.setSecondsOnFire(5);
                        } else if (ModPotionTypes.isEffectLavaPotion(basePotion)) {
                            // Apply custom lingering cloud effects for each specific lava potion type
                            // Obsidian Skin: No lingering damage, no fire
                            if (basePotion == ModPotionTypes.OBSIDIAN_SKIN.get() || basePotion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                                // No lingering effects for obsidian skin
                            }
                            // Netherite Skin: No lingering damage, no fire
                            else if (basePotion == ModPotionTypes.NETHERITE_SKIN.get() || basePotion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                                // No lingering effects for netherite skin
                            }
                            // Glass Skin: No lingering damage, no fire
                            else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                                // No lingering effects for glass skin
                            }
                            // Heat: Sets entities on fire for 10 seconds
                            else if (basePotion == ModPotionTypes.HEAT.get() || basePotion == ModPotionTypes.HEAT_LONG.get() ||
                                     basePotion == ModPotionTypes.HEAT_STRONG.get()) {
                                entity.setSecondsOnFire(10);
                            }
                            // Flammability: No lingering damage, no fire
                            else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                                // No lingering effects for flammability
                            }
                            // Pyromancy: Sets entities on fire for 15 seconds
                            else if (basePotion == ModPotionTypes.PYROMANCY.get() || basePotion == ModPotionTypes.PYROMANCY_LONG.get() ||
                                     basePotion == ModPotionTypes.PYROMANCY_STRONG.get()) {
                                entity.setSecondsOnFire(15);
                            }
                            // Magma Walker: Sets entities on fire for 5 seconds
                            else if (basePotion == ModPotionTypes.MAGMA_WALKER.get() || basePotion == ModPotionTypes.MAGMA_WALKER_LONG.get() ||
                                     basePotion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
                                entity.setSecondsOnFire(5);
                            }
                        }
                    }
                }
            }
        };
        
        // Use potion directly
        Potion cloudPotion = PotionUtils.getPotion(itemStack);
        
        // Set lingering cloud properties
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        if(this.getOwner() instanceof LivingEntity livingOwner) {
            cloud.setOwner(livingOwner);
        }
        
        // For effect potions, apply the effects directly to the cloud
        if (ModPotionTypes.isEffectLavaPotion(cloudPotion)) {
            for (MobEffectInstance effect : cloudPotion.getEffects()) {
                // Adjust duration for lingering effect
                // Lingering potions have 1/4 the duration of a regular potion
                int lingeringDuration = effect.getDuration() / 4;
                cloud.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    lingeringDuration,
                    effect.getAmplifier()
                ));
            }
        }
        
        // Handle cloud color tinting based on potion type
        int cloudColor = 0xFFFFFF; // Default white
        if (ModPotionTypes.isBasicLavaPotion(basePotion)) {
            cloudColor = 0xFFFFFF; // No tint for basic lava potions
        }
        // Obsidian Skin
        else if (basePotion == ModPotionTypes.OBSIDIAN_SKIN.get() || basePotion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
            cloudColor = 0x8e5de3; // Purple
        }
        // Netherite Skin
        else if (basePotion == ModPotionTypes.NETHERITE_SKIN.get() || basePotion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
            cloudColor = 0xa47e75; // Light brown
        }
        // Glass Skin
        else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
            cloudColor = 0xc2f3ff; // Light blue
        }
        // Heat
        else if (basePotion == ModPotionTypes.HEAT.get() || basePotion == ModPotionTypes.HEAT_LONG.get() ||
                 basePotion == ModPotionTypes.HEAT_STRONG.get()) {
            cloudColor = 0xf7a236; // Red
        }
        // Flammability
        else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
            cloudColor = 0xffec99; // Gold/amber
        }
        // Pyromancy
        else if (basePotion == ModPotionTypes.PYROMANCY.get() || basePotion == ModPotionTypes.PYROMANCY_LONG.get() ||
                 basePotion == ModPotionTypes.PYROMANCY_STRONG.get()) {
            cloudColor = 0xe5291f; // Orange
        }
        // Magma Walker
        else if (basePotion == ModPotionTypes.MAGMA_WALKER.get() || basePotion == ModPotionTypes.MAGMA_WALKER_LONG.get() ||
                 basePotion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
            cloudColor = 0xd05c00; // Orange
        }
        
        // Apply tint to the cloud particles
        cloud.setFixedColor(cloudColor);

        // Add the cloud to the world
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
                    // Light brown color for netherite skin (0xa47e75)
                    burstR = 0.643f; // 164/255
                    burstG = 0.494f; // 126/255
                    burstB = 0.459f; // 117/255
                } else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                    // Light blue color for glass skin (0xc2f3ff)
                    burstR = 0.761f; // 194/255
                    burstG = 0.953f; // 243/255
                    burstB = 1.0f;   // 255/255
                } else if (basePotion == ModPotionTypes.HEAT.get() || basePotion == ModPotionTypes.HEAT_LONG.get() ||
                           basePotion == ModPotionTypes.HEAT_STRONG.get()) {
                    // Red-orange color for heat (0xf7a236)
                    burstR = 0.969f; // 247/255
                    burstG = 0.635f; // 162/255
                    burstB = 0.212f; // 54/255
                } else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                    // Light yellow-gold for flammability (0xffec99)
                    burstR = 1.0f;   // 255/255
                    burstG = 0.925f; // 236/255
                    burstB = 0.6f;   // 153/255
                } else if (basePotion == ModPotionTypes.PYROMANCY.get() || basePotion == ModPotionTypes.PYROMANCY_LONG.get() ||
                           basePotion == ModPotionTypes.PYROMANCY_STRONG.get()) {
                    // Bright red-orange for pyromancy (0xe5291f)
                    burstR = 0.898f; // 229/255
                    burstG = 0.161f; // 41/255
                    burstB = 0.122f; // 31/255
                } else if (basePotion == ModPotionTypes.MAGMA_WALKER.get() || basePotion == ModPotionTypes.MAGMA_WALKER_LONG.get()) {
                    // Dark orange for magma walker (0xd05c00)
                    burstR = 0.816f; // 208/255
                    burstG = 0.361f; // 92/255
                    burstB = 0.0f;   // 0/255
                } else if (basePotion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
                    // Dark orange for magma walker (0xd05c00)
                    burstR = 0.816f; // 208/255
                    burstG = 0.361f; // 92/255
                    burstB = 0.0f;   // 0/255
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
                        // Light brown color for netherite skin (0xa47e75)
                        ringR = 0.643f; // 164/255
                        ringG = 0.494f; // 126/255
                        ringB = 0.459f; // 117/255
                    } else if (basePotion == ModPotionTypes.GLASS_SKIN.get() || basePotion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                        // Light blue color for glass skin (0xc2f3ff)
                        ringR = 0.761f; // 194/255
                        ringG = 0.953f; // 243/255
                        ringB = 1.0f;   // 255/255
                    } else if (basePotion == ModPotionTypes.HEAT.get() || basePotion == ModPotionTypes.HEAT_LONG.get() ||
                              basePotion == ModPotionTypes.HEAT_STRONG.get()) {
                        // Red-orange color for heat (0xf7a236)
                        ringR = 0.969f; // 247/255
                        ringG = 0.635f; // 162/255
                        ringB = 0.212f; // 54/255
                    } else if (basePotion == ModPotionTypes.FLAMMABILITY.get() || basePotion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                        // Light yellow-gold for flammability (0xffec99)
                        ringR = 1.0f;   // 255/255
                        ringG = 0.925f; // 236/255
                        ringB = 0.6f;   // 153/255
                    } else if (basePotion == ModPotionTypes.PYROMANCY.get() || basePotion == ModPotionTypes.PYROMANCY_LONG.get() ||
                              basePotion == ModPotionTypes.PYROMANCY_STRONG.get()) {
                        // Bright red-orange for pyromancy (0xe5291f)
                        ringR = 0.898f; // 229/255
                        ringG = 0.161f; // 41/255
                        ringB = 0.122f; // 31/255
                    } else if (basePotion == ModPotionTypes.MAGMA_WALKER.get() || basePotion == ModPotionTypes.MAGMA_WALKER_LONG.get()) {
                        // Dark orange for magma walker (0xd05c00)
                        ringR = 0.816f; // 208/255
                        ringG = 0.361f; // 92/255
                        ringB = 0.0f;   // 0/255
                    } else if (basePotion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
                        // Dark orange for magma walker (0xd05c00)
                        ringR = 0.816f; // 208/255
                        ringG = 0.361f; // 92/255
                        ringB = 0.0f;   // 0/255
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
    
    // NBT data for custom potion type
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(BrewingRecipes.LAVA_POTION_DATA_TAG, 10)) {
            this.setItem(ItemStack.of(compound.getCompound(BrewingRecipes.LAVA_POTION_DATA_TAG)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ItemStack itemstack = this.getItem();
        if (!itemstack.isEmpty()) {
            compound.put(BrewingRecipes.LAVA_POTION_DATA_TAG, itemstack.save(new CompoundTag()));
        }
    }
    
    // Override to ensure the correct entity is spawned
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public ItemStack getItem() {
        ItemStack itemstack = super.getItem();
        if (itemstack.isEmpty()) {
            // Default to base lava bottle if no item is set
            return BrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get());
        }
        return itemstack;
    }
}