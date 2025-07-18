package net.quoky.lava_potions.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionBagMenu extends AbstractContainerMenu {
    private final ItemStack potionBag;
    private final ItemStackHandler bagInventory;

    public PotionBagMenu(int containerId, Inventory playerInventory, ItemStack potionBag) {
        super(ModMenuTypes.POTION_BAG.get(), containerId);
        this.potionBag = potionBag;
        this.bagInventory = PotionBagItem.getInventory(potionBag);

        // Add potion bag slots (5 slots in a row, hopper-style positioning)
        for (int i = 0; i < PotionBagItem.INVENTORY_SIZE; i++) {
            this.addSlot(new PotionBagSlot(bagInventory, i, 44 + i * 18, 20));
        }

        // Add player inventory slots (hopper-style positioning)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Add player hotbar slots (hopper-style positioning)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    /**
     * Custom slot that only accepts items with 'potion' or 'bottle' in their item ID
     */
    private static class PotionBagSlot extends SlotItemHandler {
        public PotionBagSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id == null) return false;
            String path = id.getPath();
            // Exclude the potion bag item itself and only accept items with 'potion' or 'bottle' in their ID
            return (path.contains("potion") || path.contains("bottle")) && !path.contains("potion_bag");
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            int normalMaxStack = stack.getItem().getMaxStackSize(stack);
            // Only increase the limit to 16 if the item normally stacks to less than 16
            return Math.max(normalMaxStack, 16);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            // Split the stack if it exceeds the item's normal max stack size
            int normalMaxStack = stack.getItem().getMaxStackSize(stack);
            if (stack.getCount() > normalMaxStack) {
                // Calculate remaining items
                int remainingCount = stack.getCount() - normalMaxStack;
                
                // Set the stack to normal max size (this is what the player gets)
                stack.setCount(normalMaxStack);
                
                // Put the remaining items back in the slot
                ItemStack remainingStack = stack.copy();
                remainingStack.setCount(remainingCount);
                this.set(remainingStack);
            }
            
            super.onTake(player, stack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < PotionBagItem.INVENTORY_SIZE) {
                // Moving from bag to player inventory
                // Split the stack if it exceeds the item's normal max stack size
                int normalMaxStack = slotStack.getItem().getMaxStackSize(slotStack);
                if (slotStack.getCount() > normalMaxStack) {
                    // Create a stack with the normal max size
                    ItemStack normalStack = slotStack.copy();
                    normalStack.setCount(normalMaxStack);
                    
                    // Set the remaining items back in the bag slot
                    ItemStack remainingStack = slotStack.copy();
                    remainingStack.setCount(slotStack.getCount() - normalMaxStack);
                    
                    // Try to move the normal-sized stack to player inventory
                    if (!this.moveItemStackTo(normalStack, PotionBagItem.INVENTORY_SIZE, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    
                    // Put the remaining items back in the bag slot
                    slot.set(remainingStack);
                } else {
                    // Normal move if stack size is within limits
                    if (!this.moveItemStackTo(slotStack, PotionBagItem.INVENTORY_SIZE, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.set(ItemStack.EMPTY);
                }
            } else {
                // Moving from player inventory to bag
                // First try to fill existing stacks in the bag
                ItemStack remainingStack = slotStack.copy();
                
                // Check if we can add to existing stacks first
                for (int i = 0; i < PotionBagItem.INVENTORY_SIZE && !remainingStack.isEmpty(); i++) {
                    ItemStack bagStack = bagInventory.getStackInSlot(i);
                    if (!bagStack.isEmpty() && ItemStack.isSameItemSameTags(bagStack, remainingStack)) {
                        int maxStackSize = Math.max(bagStack.getItem().getMaxStackSize(bagStack), 16);
                        int spaceInStack = maxStackSize - bagStack.getCount();
                        if (spaceInStack > 0) {
                            int toAdd = Math.min(spaceInStack, remainingStack.getCount());
                            bagStack.setCount(bagStack.getCount() + toAdd);
                            bagInventory.setStackInSlot(i, bagStack);
                            remainingStack.setCount(remainingStack.getCount() - toAdd);
                        }
                    }
                }
                
                // If there are still items remaining, try to fill empty slots
                if (!remainingStack.isEmpty()) {
                    for (int i = 0; i < PotionBagItem.INVENTORY_SIZE && !remainingStack.isEmpty(); i++) {
                        ItemStack bagStack = bagInventory.getStackInSlot(i);
                        if (bagStack.isEmpty()) {
                            int maxStackSize = Math.max(remainingStack.getItem().getMaxStackSize(remainingStack), 16);
                            int toAdd = Math.min(maxStackSize, remainingStack.getCount());
                            ItemStack newStack = remainingStack.copy();
                            newStack.setCount(toAdd);
                            bagInventory.setStackInSlot(i, newStack);
                            remainingStack.setCount(remainingStack.getCount() - toAdd);
                        }
                    }
                }
                
                // Update the original slot with remaining items
                if (remainingStack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.set(remainingStack);
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Allow the menu to remain open as long as the player has the potion bag anywhere in their inventory
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameTags(stack, potionBag)) {
                return true;
            }
        }
        // Also check offhand slots
        for (ItemStack stack : player.getInventory().offhand) {
            if (ItemStack.isSameItemSameTags(stack, potionBag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Find the actual stack in the player's inventory and set it closed
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameTags(stack, potionBag)) {
                PotionBagItem.setOpen(stack, false);
                stack.getOrCreateTag().put(PotionBagItem.INVENTORY_TAG, bagInventory.serializeNBT());
                return;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (ItemStack.isSameItemSameTags(stack, potionBag)) {
                PotionBagItem.setOpen(stack, false);
                stack.getOrCreateTag().put(PotionBagItem.INVENTORY_TAG, bagInventory.serializeNBT());
                return;
            }
        }
        // Fallback: update the local reference if not found in inventory
        PotionBagItem.setOpen(potionBag, false);
        potionBag.getOrCreateTag().put(PotionBagItem.INVENTORY_TAG, bagInventory.serializeNBT());
    }
} 