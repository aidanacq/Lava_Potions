package net.quoky.lava_potions.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.quoky.lava_potions.entity.ModEntityTypes;
import net.quoky.lava_potions.item.LavaPotionItem;
import net.quoky.lava_potions.item.ModItems;
import net.quoky.lava_potions.potion.ModPotionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        return ModItems.LAVA_POTION.get();
    }

    private ParticleOptions getParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            Potion potion = LavaPotionItem.getLavaPotionType(itemstack);

            // Splash effect radius
            float radius = 4.0F;

            // Apply fire effect to all living entities in radius for splash potions
            List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(radius, radius, radius));

            for (LivingEntity entity : affectedEntities) {
                double distance = this.distanceToSqr(entity);

                if (distance < radius * radius) {
                    // The closer the entity, the longer the fire duration
                    double fireSeconds = 5.0 * (1.0 - Math.sqrt(distance) / radius);

                    // Apply fire effect
                    entity.setSecondsOnFire((int) Math.max(1, fireSeconds));
                }
            }

            // Create lingering effect if it's a lingering potion
            if (ModPotionTypes.isLingeringPotion(potion)) {
                createAreaEffectCloud(itemstack, potion);
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
            // Direct hit sets entity on fire for longer
            livingEntity.setSecondsOnFire(8);
        }
    }
    
    private void createAreaEffectCloud(ItemStack itemStack, Potion potion) {
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
                        // Set entity on fire for 3 seconds
                        entity.setSecondsOnFire(3);
                    }
                }
            }
        };
        
        Entity entity = this.getOwner();
        
        if (entity instanceof LivingEntity) {
            cloud.setOwner((LivingEntity)entity);
        }
        
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        cloud.setDuration(300); // 15 seconds
        
        // Fire particles
        cloud.setParticle(ParticleTypes.FLAME);
        
        // Set the cloud as a burning cloud
        cloud.setFixedColor(0xFF5500); // Orange color
        
        this.level().addFreshEntity(cloud);
    }
    
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            // Explosion particle in the center
            this.level().addParticle(ParticleTypes.EXPLOSION, 
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            
            // Create a burst of flame particles
            for (int i = 0; i < 60; i++) {
                double speed = 0.15;
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
            
            // Create a circular ring of flame particles
            double radius = 1.5;
            for (int i = 0; i < 36; i++) {
                double angle = Math.toRadians(i * 10);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                this.level().addParticle(
                    ParticleTypes.FLAME, 
                    this.getX() + x, 
                    this.getY() + 0.1, 
                    this.getZ() + z, 
                    x * 0.1, 
                    0.15, 
                    z * 0.1
                );
            }
        }
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public ItemStack getItem() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemStack(ModItems.LAVA_POTION.get()) : itemstack;
    }
} 