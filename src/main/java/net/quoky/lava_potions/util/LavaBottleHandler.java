package net.quoky.lava_potions.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles filling glass bottles with lava
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LavaBottleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("LavaBottleHandler");

    /**
     * Handle right-clicking on blocks
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        // Check if player is holding a glass bottle
        if (heldItem.getItem() != Items.GLASS_BOTTLE) {
            return;
        }

        // Check the block that was clicked
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Handle lava cauldron interaction
        if (clickedState.is(Blocks.LAVA_CAULDRON)) {
            createLavaBottle(level, player, event.getHand(), clickedPos);
            // Lower the cauldron level or remove lava
            level.setBlockAndUpdate(clickedPos, Blocks.CAULDRON.defaultBlockState());
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, clickedPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
            return;
        }

        if (clickedState.getBlock() == Blocks.LAVA && clickedState.getFluidState().isSource()) {
            // Direct click on lava source
            createLavaBottle(level, player, event.getHand(), clickedPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
            return;
        }

        // Check if we're looking through lava
        BlockPos lavaPos = findLavaInSight(level, player);
        if (lavaPos != null) {
            createLavaBottle(level, player, event.getHand(), lavaPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
        }
    }

    /**
     * Handle right-clicking in the air
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        // Check if player is holding a glass bottle
        if (heldItem.getItem() != Items.GLASS_BOTTLE) {
            return;
        }

        // Check if we're looking at lava
        BlockPos lavaPos = findLavaInSight(level, player);
        if (lavaPos != null) {
            createLavaBottle(level, player, event.getHand(), lavaPos);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    /**
     * Find lava in the player's line of sight
     * @param level The level
     * @param player The player
     * @return The position of lava, or null if none found
     */
    private static BlockPos findLavaInSight(Level level, Player player) {
        // Get player's eye position and view vector
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);

        // Define the reach distance (default vanilla reach is around 5.0)
        double reach = 5.0D;
        Vec3 targetPos = eyePos.add(viewVec.x * reach, viewVec.y * reach, viewVec.z * reach);

        // First try vanilla's fluid targeting
        BlockHitResult vanillaHit = level.clip(new ClipContext(
                eyePos, targetPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,
                player));

        if (vanillaHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = vanillaHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() == Blocks.LAVA && state.getFluidState().isSource()) {
                return pos;
            }
        }

        // Manual ray casting as a fallback
        // We'll check every 0.25 blocks along the line of sight
        double stepSize = 0.25D;
        double distance = reach;

        for (double d = 0; d <= distance; d += stepSize) {
            double scale = d / distance;

            // Interpolate between eye position and target position
            double x = eyePos.x + (targetPos.x - eyePos.x) * scale;
            double y = eyePos.y + (targetPos.y - eyePos.y) * scale;
            double z = eyePos.z + (targetPos.z - eyePos.z) * scale;

            BlockPos checkPos = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
            BlockState state = level.getBlockState(checkPos);

            if (state.getBlock() == Blocks.LAVA && state.getFluidState().isSource()) {
                return checkPos;
            }
        }

        return null;
    }

    /**
     * Create a lava bottle and give it to the player
     * @param level The level
     * @param player The player
     * @param hand The hand
     * @param lavaPos The position of the lava
     */
    private static void createLavaBottle(Level level, Player player, InteractionHand hand, BlockPos lavaPos) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Create lava bottle
        ItemStack lavaBottle = new ItemStack(ModItems.LAVA_POTION.get());
        CompoundTag tag = lavaBottle.getOrCreateTag();
        tag.putString("Potion", Lava_Potions.MOD_ID + ":lava_bottle");

        // Play sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

        // Fire game event
        level.gameEvent(player, GameEvent.FLUID_PICKUP, lavaPos);

        // Remove one glass bottle (if not in creative)
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }

        // Give the lava bottle to the player
        if (heldItem.isEmpty()) {
            player.setItemInHand(hand, lavaBottle);
        } else {
            if (!player.getInventory().add(lavaBottle)) {
                player.drop(lavaBottle, false);
            }
        }
    }
}