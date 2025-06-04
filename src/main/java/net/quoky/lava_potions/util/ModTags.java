package net.quoky.lava_potions.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Contains custom tags for the mod
 */
public class ModTags {
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
            return ItemTags.create(ResourceLocation.tryParse(Lava_Potions.MOD_ID + ":" + name));
        }
    }
} 