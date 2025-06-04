package net.quoky.lava_potions.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.entity.ThrownLavaPotion;
import net.quoky.lava_potions.potion.ModPotionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LavaPotionItem extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger("LavaPotionItem");
    
    public LavaPotionItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // LavaPotionItem shouldn't handle filling bottles from lava
        // That's handled by LavaBottleHandler
        return InteractionResult.PASS;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Potion potionType = getLavaPotionType(stack);
        
        // If it's a splash or lingering potion, throw it
        if (ModPotionTypes.isSplashPotion(potionType) || ModPotionTypes.isLingeringPotion(potionType)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            
            if (!level.isClientSide) {
                ThrownLavaPotion thrownPotion = new ThrownLavaPotion(level, player);
                thrownPotion.setItem(stack);
                thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), 
                        -20.0F, 0.5F, 1.0F);
                level.addFreshEntity(thrownPotion);
            }
            
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        
        // Otherwise, drink it
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player)entity : null;
        
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        
        if (!level.isClientSide()) {
            // Add the delayed fire effect as a scheduled task
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 5, () -> {
                if (entity.isAlive()) {
                    entity.setSecondsOnFire(5);
                }
            }));
        }
        
        // Play drinking sound
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 
                level.random.nextFloat() * 0.1F + 0.9F);
        
        // Add drink stat
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        // Return empty bottle if consumed
        if (player == null || !player.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            
            if (player != null) {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return 32; // Same as vanilla potions
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        Potion potion = getLavaPotionType(stack);
        
        if (ModPotionTypes.isSplashPotion(potion) || ModPotionTypes.isLingeringPotion(potion)) {
            return UseAnim.NONE; // No drinking animation for splash/lingering potions
        }
        
        return UseAnim.DRINK;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        // Get the proper name based on potion type
        Potion potion = getLavaPotionType(stack);
        ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(potion);
        if (potionId != null) {
            return Component.translatable("potion." + potionId.getNamespace() + "." + potionId.getPath());
        }
        return Component.translatable("item." + Lava_Potions.MOD_ID + ".lava_potion");
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        // Add potion effect descriptions from NBT data if needed
        super.appendHoverText(stack, level, tooltipComponents, flag);
    }
    
    /**
     * Gets the potion type from the item's NBT data
     */
    public static Potion getLavaPotionType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Potion")) {
            return ModPotionTypes.getPotionTypeFromId(tag.getString("Potion"));
        }
        return ModPotionTypes.LAVA_BOTTLE.get(); // Default to lava bottle
    }
    
    /**
     * Creates an ItemStack with the specified potion type
     */
    public static ItemStack getPotionItemStack(Potion potionType) {
        LOGGER.info("Creating potion item stack for potion: {}", 
            ForgeRegistries.POTIONS.getKey(potionType));
        
        ItemStack stack = new ItemStack(ModItems.LAVA_POTION.get());
        CompoundTag tag = stack.getOrCreateTag();
        
        // Get the proper potion ID
        ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(potionType);
        if (potionId == null) {
            LOGGER.error("Failed to get registry key for potion type");
            potionId = new ResourceLocation(Lava_Potions.MOD_ID, "lava_bottle");
        }
        
        // Set the potion tag
        tag.putString("Potion", potionId.toString());
        LOGGER.info("Created potion with NBT: {}", tag);
        
        return stack;
    }
} 