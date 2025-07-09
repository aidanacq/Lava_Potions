package net.quoky.lava_potions.item;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class PotionBagMenu extends AbstractContainerMenu {
    private final ItemStack potionBag;
    private final ItemStackHandler bagInventory;

    public PotionBagMenu(int containerId, Inventory playerInventory, ItemStack potionBag) {
        super(ModMenuTypes.POTION_BAG.get(), containerId);
        this.potionBag = potionBag;
        this.bagInventory = PotionBagItem.getInventory(potionBag);

        // Add potion bag slots (5 slots in a row, hopper-style positioning)
        for (int i = 0; i < PotionBagItem.INVENTORY_SIZE; i++) {
            this.addSlot(new SlotItemHandler(bagInventory, i, 44 + i * 18, 20));
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < PotionBagItem.INVENTORY_SIZE) {
                // Moving from bag to player inventory
                if (!this.moveItemStackTo(slotStack, PotionBagItem.INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to bag
                if (!this.moveItemStackTo(slotStack, 0, PotionBagItem.INVENTORY_SIZE, false)) {
                    return ItemStack.EMPTY;
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
        return player.getMainHandItem() == potionBag || player.getOffhandItem() == potionBag;
    }

    public ItemStackHandler getBagInventory() {
        return bagInventory;
    }

    public ItemStack getPotionBag() {
        return potionBag;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Mark the bag as closed when the GUI is closed
        PotionBagItem.setOpen(potionBag, false);
        // Update slot states to ensure they're current
        PotionBagItem.updateSlotStates(potionBag);
    }
} 