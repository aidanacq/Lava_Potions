package net.quoky.lava_potions.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Contains custom tags for the mod
 */
public class ModTags {
    /**
     * Block tags for the mod
     */
    public static class Blocks {
        // Tag for blocks that require pickaxe
        public static final TagKey<Block> MINEABLE_WITH_PICKAXE = 
                BlockTags.create(new ResourceLocation("minecraft", "mineable/with_pickaxe"));

        /**
         * Creates a block tag with the mod ID namespace
         */
        private static TagKey<Block> blockTag(String name) {
            return BlockTags.create(new ResourceLocation(Lava_Potions.MOD_ID, name));
        }
    }

    /**
     * Item tags for the mod
     */
    public static class Items {
        // Tag for all lava potions
        public static final TagKey<Item> LAVA_POTIONS = tag("lava_potions");

        /**
         * Creates a tag with the mod ID namespace
         */
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(Lava_Potions.MOD_ID, name));
        }
    }
} 