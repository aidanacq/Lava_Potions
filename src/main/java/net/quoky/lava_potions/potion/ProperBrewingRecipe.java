package net.quoky.lava_potions.potion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;

import javax.annotation.Nonnull;

public class ProperBrewingRecipe extends BrewingRecipe {

    private final Ingredient input;
    private final Ingredient ingredient;
    private final ItemStack output;

    public ProperBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        super(input, ingredient, output);
        this.input = input;
        this.ingredient = ingredient;
        this.output = output;
        
        // Ensure the output has texture metadata if it's a lava potion
        ensureTextureMetadata(output);
    }

    @Override
    public boolean isInput(@Nonnull ItemStack stack) {
        if (stack == null) {
            return false;
        } else {
            ItemStack[] matchingStacks = input.getItems();
            if (matchingStacks.length == 0) {
                return stack.isEmpty();
            } else {
                for (ItemStack itemstack : matchingStacks) {
                    if (ItemStack.isSameItem(stack, itemstack) && ItemStack.isSameItemSameTags(itemstack, stack)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }
    
    /**
     * Ensures that lava potions have texture metadata in their NBT
     * This helps Create mod recognize and render them correctly
     */
    private void ensureTextureMetadata(ItemStack potionStack) {
        try {
            Potion potion = PotionUtils.getPotion(potionStack);
            if (potion == null) return;
            
            // Only process lava potions
            if (!ModPotionTypes.isLavaPotion(potion)) return;
            
            CompoundTag tag = potionStack.getOrCreateTag();
            CompoundTag lavaData = new CompoundTag();
            
            // For awkward lava or base lava potion - use vanilla lava texture
            if (potion == ModPotionTypes.AWKWARD_LAVA.get() || ModPotionTypes.isBaseLavaBottle(potion)) {
                lavaData.putString("StillTexture", "minecraft:block/lava_still");
                lavaData.putString("FlowingTexture", "minecraft:block/lava_flow");
            }
            // For all effect lava potions - use gray lava texture with tinting
            else if (ModPotionTypes.isEffectLavaPotion(potion)) {
                lavaData.putString("StillTexture", "lava_potions:block/gray_lava_still");
                lavaData.putString("FlowingTexture", "lava_potions:block/gray_lava_flow");
            }
            
            // Store the texture data
            tag.put("LavaPotionData", lavaData);
            potionStack.setTag(tag);
            
            Lava_Potions.LOGGER.debug("Added texture metadata to lava potion: {}", 
                ForgeRegistries.POTIONS.getKey(potion));
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error adding texture metadata to potion: {}", e.getMessage());
        }
    }
} 