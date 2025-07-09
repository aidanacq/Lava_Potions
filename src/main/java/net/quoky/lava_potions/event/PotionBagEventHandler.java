package net.quoky.lava_potions.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.ModItems;
import net.quoky.lava_potions.item.PotionBagItem;
import net.quoky.lava_potions.item.PotionBagMenu;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class PotionBagEventHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        resetPotionBagStates(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        resetPotionBagStates(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof PotionBagMenu menu) {
            ItemStack potionBag = menu.getPotionBag();
            Player player = event.getEntity();
            
            // Close the bag and update slot states when container closes
            PotionBagItem.setOpen(potionBag, false);
            PotionBagItem.updateSlotStates(potionBag);
            
            // Force client-side update by marking the inventory as changed
            if (!player.level().isClientSide) {
                player.inventoryMenu.broadcastChanges();
            }
        }
    }

    private static void resetPotionBagStates(Player player) {
        // Reset open state for all potion bags in player's inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.POTION_BAG.get()) {
                PotionBagItem.setOpen(stack, false);
            }
        }
    }
} 