package net.quoky.lava_potions.item;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Lava_Potions.MOD_ID);

    public static final RegistryObject<MenuType<PotionBagMenu>> POTION_BAG = MENUS.register("potion_bag",
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                return new PotionBagMenu(windowId, inv, data.readItem());
            }));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
} 