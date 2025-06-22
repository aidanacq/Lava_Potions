package net.quoky.lava_potions.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, Lava_Potions.MOD_ID);

    // Custom layered lava cauldron that supports levels like water cauldrons
    public static final RegistryObject<Block> LAVA_CAULDRON = BLOCKS.register("lava_cauldron",
        () -> new LayeredLavaCauldronBlock(BlockBehaviour.Properties.copy(Blocks.LAVA_CAULDRON)));
        
    // Decayable magma block that changes texture based on age
    public static final RegistryObject<Block> DECAYABLE_MAGMA_BLOCK = BLOCKS.register("decayable_magma_block",
        () -> new DecayableMagmaBlock());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
} 