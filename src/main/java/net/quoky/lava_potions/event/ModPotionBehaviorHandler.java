package net.quoky.lava_potions.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.effect.MagmaWalkerEffect;
import net.quoky.lava_potions.effect.ModEffects;
import net.quoky.lava_potions.entity.ThrownLavaPotion;
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler for various potion behaviors
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class ModPotionBehaviorHandler {

    // Position tracking for Magma Walker effect
    private static final Map<UUID, PlayerPositionData> playerPositions = new HashMap<>();

    /**
     * Data class to store player position information
     */
    private static class PlayerPositionData {
        public int previousX;
        public int previousZ;
        public float previousY;
        public boolean hasPrevious; // Track if we have valid previous coordinates

        public PlayerPositionData() {
            this.hasPrevious = false;
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();

        if (!BrewingRecipes.isVanillaPotionWithLavaType(stack)) {
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

        if (!BrewingRecipes.isVanillaPotionWithLavaType(stack)) {
            return;
        }

        Potion potion = PotionUtils.getPotion(stack);

        if (!level.isClientSide()) {
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 5, () -> {
                if (entity.isAlive()) {
                    if (ModPotionTypes.isBaseLavaBottle(potion) || ModPotionTypes.isAwkwardLava(potion)) {
                        entity.setSecondsOnFire(5);
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

        if (!BrewingRecipes.isVanillaPotionWithLavaType(stack)) {
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

                // Check if this is an effect potion with preserved durations
                if (ModPotionTypes.isEffectLavaPotion(potion) && stack.hasTag()
                        && stack.getTag().contains("lava_potions_data")) {
                    // Find duration lines in tooltip and replace them with the original values
                    CompoundTag customData = stack.getTag().getCompound("lava_potions_data");

                    // Check all effects in the potion
                    for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
                        String effectKey = "original_duration_" + effect.getEffect().getDescriptionId();

                        // If we have stored the original duration for this effect
                        if (customData.contains(effectKey)) {
                            int originalDuration = customData.getInt(effectKey);

                            // Find this effect's entry in the tooltip (usually starts with the effect name)
                            String effectName = Component.translatable(effect.getEffect().getDescriptionId())
                                    .getString();

                            for (int i = 0; i < event.getToolTip().size(); i++) {
                                String line = event.getToolTip().get(i).getString();
                                if (line.contains(effectName)) {
                                    // Replace the countdown duration with the original duration
                                    int minutes = originalDuration / 1200; // 20 ticks * 60 seconds
                                    int seconds = (originalDuration % 1200) / 20;

                                    String timeDisplay = minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
                                    String newLine = effectName;
                                    if (effect.getAmplifier() > 0) {
                                        newLine += " " + Component
                                                .translatable("potion.potency." + effect.getAmplifier()).getString();
                                    }
                                    newLine += " (" + timeDisplay + ")";

                                    event.getToolTip().set(i, Component.literal(newLine));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static ItemStack createLavaPotionFromVanilla(ItemStack vanillaPotion) {
        if (!BrewingRecipes.isVanillaPotionWithLavaType(vanillaPotion)) {
            return vanillaPotion;
        }

        Potion potion = PotionUtils.getPotion(vanillaPotion);

        ItemStack throwablePotion;
        if (vanillaPotion.getItem() == Items.SPLASH_POTION) {
            throwablePotion = BrewingRecipes.createVanillaSplashPotionWithLavaType(potion);
        } else if (vanillaPotion.getItem() == Items.LINGERING_POTION) {
            throwablePotion = BrewingRecipes.createVanillaLingeringPotionWithLavaType(potion);
        } else {
            throwablePotion = BrewingRecipes.createVanillaPotionWithLavaType(potion);
        }

        return throwablePotion;
    }

    /**
     * Handle Magma Walker platform creation using proper coordinate tracking
     * Checks every tick for position changes and creates platforms only during
     * horizontal movement
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Check all players for Magma Walker effect
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            MobEffectInstance effect = player.getEffect(ModEffects.MAGMA_WALKER.get());
            if (effect != null) {
                checkMagmaWalkerMovement(player, effect.getAmplifier());
            } else {
                // Clean up position data if player no longer has effect
                playerPositions.remove(player.getUUID());
            }
        }
    }

    /**
     * Check player movement and create platform if conditions are met
     */
    private static void checkMagmaWalkerMovement(Player player, int amplifier) {
        if (player.level().isClientSide) {
            return;
        }

        UUID playerId = player.getUUID();
        PlayerPositionData posData = playerPositions.computeIfAbsent(playerId, k -> new PlayerPositionData());

        // Get current position at middle of bottom of hitbox
        double playerX = player.getX();
        double playerZ = player.getZ();
        double playerY = player.getY();

        // Convert to integer coordinates for x/z, float for y
        int currentX = (int) Math.floor(playerX);
        int currentZ = (int) Math.floor(playerZ);
        float currentY = (float) playerY;

        // If this is the first tick with the effect, just store current position and
        // don't create platform
        if (!posData.hasPrevious) {
            posData.previousX = currentX;
            posData.previousZ = currentZ;
            posData.previousY = currentY;
            posData.hasPrevious = true;
            return;
        }

        // Check for movement
        boolean xChanged = currentX != posData.previousX;
        boolean zChanged = currentZ != posData.previousZ;
        boolean yChanged = currentY != posData.previousY;

        // Platform creation logic: horizontal movement (x or z changed) with no
        // vertical movement (y same) and player on ground
        if ((xChanged || zChanged) && !yChanged && player.onGround()) {
            createMagmaWalkerPlatform(player, amplifier);
        }

        // Update previous coordinates only if any coordinate changed
        if (xChanged || zChanged || yChanged) {
            posData.previousX = currentX;
            posData.previousZ = currentZ;
            posData.previousY = currentY;
        }
    }

    /**
     * Create magma walker platform around player
     */
    private static void createMagmaWalkerPlatform(Player player, int amplifier) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Determine platform size based on amplifier (tier)
        int platformSize = amplifier == 0 ? 5 : 7;
        int halfSize = platformSize / 2;

        // Get the target Y level (same as player's feet)
        int targetY = playerPos.getY() - 1;

        // Check if there's lava that needs replacement
        boolean shouldCreatePlatform = false;
        for (int x = -halfSize; x <= halfSize && !shouldCreatePlatform; x++) {
            for (int z = -halfSize; z <= halfSize && !shouldCreatePlatform; z++) {
                // Skip corner blocks for both tiers
                if (Math.abs(x) == halfSize && Math.abs(z) == halfSize) {
                    continue;
                }

                BlockPos checkPos = new BlockPos(playerPos.getX() + x, targetY, playerPos.getZ() + z);
                BlockPos aboveCheckPos = checkPos.above();

                BlockState blockState = level.getBlockState(checkPos);
                BlockState aboveBlockState = level.getBlockState(aboveCheckPos);

                // Check for lava block with air above
                if (blockState.is(Blocks.LAVA) && aboveBlockState.isAir()) {
                    shouldCreatePlatform = true;
                }
            }
        }

        if (shouldCreatePlatform) {
            // Create platform using the MagmaWalkerEffect method
            if (ModEffects.MAGMA_WALKER.get() instanceof MagmaWalkerEffect magmaWalkerEffect) {
                magmaWalkerEffect.createMagmaPlatform(level, player, amplifier);
            }
        }
    }
}