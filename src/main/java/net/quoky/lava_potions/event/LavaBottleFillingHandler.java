package net.quoky.lava_potions.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.ModPotionBrewingRecipes;

/**
 * Handles filling glass bottles with lava to create lava bottles
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class LavaBottleFillingHandler {
    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        
        // Check if player is holding a glass bottle
        if (stack.getItem() != Items.GLASS_BOTTLE) {
            return;
        }
        
        BlockState blockState = level.getBlockState(pos);
        
        // Check if the block is a lava source
        if (blockState.is(Blocks.LAVA) && level.getFluidState(pos).isSource()) {
            if (!level.isClientSide) {
                // Create a lava bottle item (vanilla potion with our lava type)
                ItemStack lavaBottle = ModPotionBrewingRecipes.createVanillaPotionWithLavaType(ModPotionTypes.LAVA_BOTTLE.get());
                
                if (!lavaBottle.isEmpty()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    
                    // Give the lava bottle to the player
                    if (stack.isEmpty()) {
                        player.setItemInHand(event.getHand(), lavaBottle);
                    } else {
                        if (!player.getInventory().add(lavaBottle)) {
                            player.drop(lavaBottle, false);
                        }
                    }
                    
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            } else {
                // Client side - just indicate success
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
} 