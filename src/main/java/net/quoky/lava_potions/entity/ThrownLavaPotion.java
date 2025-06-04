package net.quoky.lava_potions.entity;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

public class ThrownLavaPotion extends ThrowableItemProjectile implements ItemSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger("ThrownLavaPotion");
    
    public ThrownLavaPotion(EntityType<? extends ThrownLavaPotion> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownLavaPotion(Level level, LivingEntity entity) {
        super(ModEntityTypes.THROWN_LAVA_POTION.get(), entity, level);
    }

    public ThrownLavaPotion(Level level, double x, double y, double z) {
        super(ModEntityTypes.THROWN_LAVA_POTION.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        // Use vanilla potion item instead of custom lava potion
        return Items.POTION;
    }

    private ParticleOptions getParticle() {
        return ParticleTypes.FLAME;
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
            
            // Apply impact damage for both splash and lingering potions
            List<LivingEntity> entitiesForDamage = this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(radius, radius, radius));
            
            Entity owner = this.getOwner();
            
            for (LivingEntity entity : entitiesForDamage) {
                // Skip applying damage to the thrower
                if (entity != owner) {
                    double distance = this.distanceToSqr(entity);
                    if (distance < radius * radius) {
                        // Apply 4 hit points (2 hearts) of damage
                        entity.hurt(entity.damageSources().thrown(this, owner instanceof LivingEntity ? (LivingEntity) owner : null), 4.0F);
                    }
                }
            }

            // Apply immediate fire effect to all living entities in radius for both splash and lingering potions
            List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(radius, radius, radius));

            for (LivingEntity entity : affectedEntities) {
                double distance = this.distanceToSqr(entity);

                if (distance < radius * radius) {
                    // Apply effects based on potion type
                    if (ModPotionTypes.isBaseLavaBottle(basePotion) || ModPotionTypes.isAwkwardLava(basePotion)) {
                        // Set entities on fire for 5 seconds for both splash and lingering potions
                        entity.setSecondsOnFire(5);
                    } else {
                        // For future effect-based potions
                        // Apply effects based on the potion type
                        entity.setSecondsOnFire(5);
                        
                        // Additional effects will go here when implemented
                    }
                }
            }
            
            // Create additional lingering effect if it's a lingering potion
            if (isLingering) {
                createAreaEffectCloud(itemstack, potion, basePotion);
            }
            
            // Trigger splash particles via entity event
            this.level().broadcastEntityEvent(this, (byte)3);
            
            // Always break the potion
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        
        Entity target = hitResult.getEntity();
        if (target instanceof LivingEntity livingEntity) {
            // Direct hit sets entity on fire for longer (10 seconds)
            ItemStack itemstack = this.getItem();
            Potion potion = PotionUtils.getPotion(itemstack);
            Potion basePotion = potion; // Use potion directly
            
            // Different effects based on potion type
            if (ModPotionTypes.isBaseLavaBottle(basePotion) || ModPotionTypes.isAwkwardLava(basePotion)) {
                // Basic lava bottle just sets entity on fire
                livingEntity.setSecondsOnFire(10);
            } else {
                // For future effect-based potions
                // Apply specific effects based on the potion type
                livingEntity.setSecondsOnFire(10);
                
                // Additional effects will go here when implemented
            }
        }
    }
    
    /**
     * Check if this is a lingering potion based on custom logic
     */
    private boolean isLingeringPotion(ItemStack itemStack) {
        // Check NBT tag first (for converted vanilla potions)
        if (itemStack.hasTag() && itemStack.getTag().getBoolean("IsLingering")) {
            return true;
        }
        
        // Check if it's a vanilla lingering potion item
        return itemStack.getItem() == Items.LINGERING_POTION;
    }
    
    private void createAreaEffectCloud(ItemStack itemStack, Potion potion, Potion basePotion) {
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ()) {
            // Override to set entities on fire when they're in the lingering cloud
            @Override
            public void tick() {
                super.tick();
                
                // Check for living entities in the cloud every 10 ticks (0.5 seconds)
                if (this.tickCount % 10 == 0) {
                    List<LivingEntity> entitiesInCloud = this.level().getEntitiesOfClass(LivingEntity.class, 
                            this.getBoundingBox());
                    
                    for (LivingEntity entity : entitiesInCloud) {
                        // Different effects based on potion type
                        if (ModPotionTypes.isBaseLavaBottle(basePotion) || ModPotionTypes.isAwkwardLava(basePotion)) {
                            // Set entities on fire for 5 seconds
                            entity.setSecondsOnFire(5);
                        } else {
                            // For future effect-based potions
                            // Apply specific effects based on the potion type
                            entity.setSecondsOnFire(5);
                            
                            // Additional effects will go here when implemented
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
        
        // Fire particles
        cloud.setParticle(ParticleTypes.FLAME);
        
        // Set the cloud color based on potion type
        ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(basePotion);
        if (potionId != null && potionId.getPath().contains("awkward")) {
            cloud.setFixedColor(0xFF8800); // Darker orange for awkward
        } else {
            cloud.setFixedColor(0xFF5500); // Default orange color
        }
        
        this.level().addFreshEntity(cloud);
    }
    
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemStack itemstack = this.getItem();
            Potion potion = PotionUtils.getPotion(itemstack);
            Potion basePotion = potion; // Use potion directly
            
            // Explosion particle in the center
            this.level().addParticle(ParticleTypes.EXPLOSION, 
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            
            // Create a burst of flame particles
            for (int i = 0; i < 60; i++) {
                double offsetX = this.random.nextGaussian() * 0.15;
                double offsetY = this.random.nextGaussian() * 0.15;
                double offsetZ = this.random.nextGaussian() * 0.15;
                
                // Different particle colors based on potion type
                ParticleOptions particleType;
                if (ModPotionTypes.isAwkwardLava(basePotion)) {
                    // Alternate between flame and smoke for awkward lava
                    particleType = i % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE;
                } else {
                    particleType = ParticleTypes.FLAME;
                }
                
                this.level().addParticle(
                    particleType,
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
                
                // Different particle colors based on potion type
                ParticleOptions particleType = ParticleTypes.LAVA;
                if (ModPotionTypes.isAwkwardLava(basePotion) && i % 3 == 0) {
                    particleType = ParticleTypes.LARGE_SMOKE;
                }
                
                this.level().addParticle(
                    particleType, 
                    this.getX() + x, 
                    this.getY() + 0.1, 
                    this.getZ() + z, 
                    x * 0.1, 
                    0.15, 
                    z * 0.1
                );
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
        return itemstack.isEmpty() ? VanillaPotionBrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get()) : itemstack;
    }
} 