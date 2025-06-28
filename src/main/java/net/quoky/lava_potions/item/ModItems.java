package net.quoky.lava_potions.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.block.ModBlocks;

public class ModItems {
    // Create a Deferred Register to hold Items which will all be registered under
    // the "lava_potions" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            Lava_Potions.MOD_ID);

    // Register the layered lava cauldron block item
    public static final RegistryObject<Item> LAVA_CAULDRON = ITEMS.register("lava_cauldron",
            () -> new BlockItem(ModBlocks.LAVA_CAULDRON.get(), new Item.Properties().fireResistant()));

    // Register the decayable magma block item
    public static final RegistryObject<Item> DECAYABLE_MAGMA_BLOCK = ITEMS.register("decayable_magma_block",
            () -> new BlockItem(ModBlocks.DECAYABLE_MAGMA_BLOCK.get(), new Item.Properties().fireResistant()));

    // Register the strider hide item
    public static final RegistryObject<Item> STRIDER_HIDE = ITEMS.register("strider_hide",
            () -> new Item(new Item.Properties().fireResistant()));

    // Register all items
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}