package net.quoky.lava_potions.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.quoky.lava_potions.client.PotionBagRenderer;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PotionBagItem extends Item {
    public static final String INVENTORY_TAG = "PotionBagInventory";
    public static final String IS_OPEN_TAG = "IsOpen";
    public static final String SLOTS_FILLED_TAG = "SlotsFilled";
    public static final int INVENTORY_SIZE = 5;

    public PotionBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Mark the bag as open
            setOpen(itemStack, true);
            updateSlotStates(itemStack);
            NetworkHooks.openScreen(serverPlayer, new PotionBagMenuProvider(itemStack), 
                buf -> buf.writeItem(itemStack));
        } else if (level.isClientSide) {
            // Mark as open on client side too for immediate visual feedback
            setOpen(itemStack, true);
            updateSlotStates(itemStack);
        }
        
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    public static ItemStackHandler getInventory(ItemStack stack) {
        ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                // Save the inventory to NBT when contents change
                CompoundTag nbt = stack.getOrCreateTag();
                nbt.put(INVENTORY_TAG, this.serializeNBT());
                // Update slot states when inventory changes
                updateSlotStates(stack);
            }
        };
        
        // Load existing inventory from NBT
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(INVENTORY_TAG)) {
            inventory.deserializeNBT(nbt.getCompound(INVENTORY_TAG));
        }
        
        return inventory;
    }

    public static boolean isEmpty(ItemStack stack) {
        ItemStackHandler inventory = getInventory(stack);
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void setOpen(ItemStack stack, boolean open) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putBoolean(IS_OPEN_TAG, open);
    }

    public static boolean isOpen(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.getBoolean(IS_OPEN_TAG);
    }

    public static int getFilledSlotCount(ItemStack stack) {
        ItemStackHandler inventory = getInventory(stack);
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Updates the slot fill states in NBT based on current inventory
     */
    public static void updateSlotStates(ItemStack stack) {
        ItemStackHandler inventory = getInventory(stack);
        CompoundTag nbt = stack.getOrCreateTag();
        
        ListTag slotsList = new ListTag();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            slotsList.add(IntTag.valueOf(inventory.getStackInSlot(i).isEmpty() ? 0 : 1));
        }
        nbt.put(SLOTS_FILLED_TAG, slotsList);
    }

    /**
     * Gets the slot fill states as an array
     */
    public static boolean[] getSlotStates(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        boolean[] states = new boolean[INVENTORY_SIZE];
        
        if (nbt != null && nbt.contains(SLOTS_FILLED_TAG)) {
            ListTag slotsList = nbt.getList(SLOTS_FILLED_TAG, 3); // 3 = IntTag
            for (int i = 0; i < Math.min(INVENTORY_SIZE, slotsList.size()); i++) {
                states[i] = slotsList.getInt(i) == 1;
            }
        }
        
        return states;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private PotionBagRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new PotionBagRenderer(Minecraft.getInstance().getEntityModels());
                }
                return this.renderer;
            }
        });
    }

    private static class PotionBagMenuProvider implements MenuProvider {
        private final ItemStack potionBag;

        public PotionBagMenuProvider(ItemStack potionBag) {
            this.potionBag = potionBag;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.lava_potions.potion_bag");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new PotionBagMenu(containerId, playerInventory, potionBag);
        }
    }
} 