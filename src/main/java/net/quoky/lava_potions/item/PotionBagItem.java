package net.quoky.lava_potions.item;

import net.minecraft.nbt.CompoundTag;
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

import javax.annotation.Nullable;

public class PotionBagItem extends Item {
    public static final String INVENTORY_TAG = "PotionBagInventory";
    public static final String IS_OPEN_TAG = "IsOpen";
    public static final int INVENTORY_SIZE = 5;

    public PotionBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            setOpen(itemStack, true);
            NetworkHooks.openScreen(serverPlayer, new PotionBagMenuProvider(itemStack),
                    buf -> buf.writeItem(itemStack));
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
            }

            @Override
            public int getSlotLimit(int slot) {
                ItemStack stack = this.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    int normalMaxStack = stack.getItem().getMaxStackSize(stack);
                    // Only increase the limit to 16 if the item normally stacks to less than 16
                    return Math.max(normalMaxStack, 16);
                }
                return 16; // Default to 16 for empty slots
            }
        };

        // Load existing inventory from NBT
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(INVENTORY_TAG)) {
            inventory.deserializeNBT(nbt.getCompound(INVENTORY_TAG));
        }

        return inventory;
    }

    public static void setOpen(ItemStack stack, boolean open) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putBoolean(IS_OPEN_TAG, open);
    }

    public static boolean isOpen(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.getBoolean(IS_OPEN_TAG);
    }

    /**
     * Gets a bitmask representing which slots are filled (0-31)
     * Used only when the bag is open to determine which overlay model to use
     */
    public static int getSlotMask(ItemStack stack) {
        ItemStackHandler inventory = getInventory(stack);
        int mask = 0;
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                mask |= (1 << i);
            }
        }
        return mask;
    }

    /**
     * Checks if a specific slot is filled
     */
    public static boolean isSlotFilled(ItemStack stack, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= INVENTORY_SIZE) {
            return false;
        }
        ItemStackHandler inventory = getInventory(stack);
        return !inventory.getStackInSlot(slotIndex).isEmpty();
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