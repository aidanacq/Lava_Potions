package net.quoky.lava_potions.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.entity.ThrownLavaPotion;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class VanillaPotionBehaviorHandler {
    
    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();
        
        if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return;
        }
        
        if (stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            
            if (!level.isClientSide) {
                ThrownLavaPotion thrownPotion = new ThrownLavaPotion(level, player);
                ItemStack lavaPotion = createLavaPotionFromVanilla(stack);
                thrownPotion.setItem(lavaPotion);
                thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), 
                        -20.0F, 0.5F, 1.0F);
                level.addFreshEntity(thrownPotion);
            }
            
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEntityFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        
        if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return;
        }
        
        Potion potion = PotionUtils.getPotion(stack);
        
        if (!level.isClientSide()) {
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 5, () -> {
                if (entity.isAlive()) {
                    entity.setSecondsOnFire(5);
                    if (ModPotionTypes.isBaseLavaBottle(potion) || ModPotionTypes.isAwkwardLava(potion)) {
                        entity.hurt(entity.damageSources().lava(), 4.0F);
                    }
                }
            }));
        }
        
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 
                level.random.nextFloat() * 0.1F + 0.9F);
    }
    
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return;
        }
        
        Potion potion = PotionUtils.getPotion(stack);
        
        if (!event.getToolTip().isEmpty()) {
            ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(potion);
            if (potionId != null) {
                String baseName = "potion." + potionId.getNamespace() + "." + potionId.getPath();
                
                if (stack.getItem() == Items.SPLASH_POTION) {
                    baseName = "splash_" + baseName;
                } else if (stack.getItem() == Items.LINGERING_POTION) {
                    baseName = "lingering_" + baseName;
                }
                
                Component properName = Component.translatable(baseName);
                event.getToolTip().set(0, properName);
            }
        }
    }
    
    private static ItemStack createLavaPotionFromVanilla(ItemStack vanillaPotion) {
        if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(vanillaPotion)) {
            return vanillaPotion;
        }
        
        Potion potion = PotionUtils.getPotion(vanillaPotion);
        
        ItemStack throwablePotion;
        if (vanillaPotion.getItem() == Items.SPLASH_POTION) {
            throwablePotion = VanillaPotionBrewingRecipes.createVanillaSplashPotionWithLavaType(potion);
        } else if (vanillaPotion.getItem() == Items.LINGERING_POTION) {
            throwablePotion = VanillaPotionBrewingRecipes.createVanillaLingeringPotionWithLavaType(potion);
        } else {
            throwablePotion = VanillaPotionBrewingRecipes.createVanillaPotionWithLavaType(potion);
        }
        
        return throwablePotion;
    }
} 