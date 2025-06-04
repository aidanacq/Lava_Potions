package net.quoky.lava_potions.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

public class ModItems {
    // Create a Deferred Register to hold Items which will all be registered under the "lava_potions" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Lava_Potions.MOD_ID);

    // Register the lava potion item
    public static final RegistryObject<Item> LAVA_POTION = ITEMS.register("lava_potion",
            () -> new LavaPotionItem(new Item.Properties().stacksTo(1).fireResistant()));

    // Register all items
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
} 