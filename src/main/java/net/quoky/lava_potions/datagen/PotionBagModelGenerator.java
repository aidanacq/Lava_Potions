package net.quoky.lava_potions.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.quoky.lava_potions.Lava_Potions;

public class PotionBagModelGenerator extends ItemModelProvider {
    
    public PotionBagModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Lava_Potions.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Generate all 32 open bag models (masks 0-31)
        for (int mask = 0; mask <= 31; mask++) {
            generateOpenBagModel(mask);
        }
    }

    private void generateOpenBagModel(int mask) {
        String modelName = "potion_bag_open_mask_" + mask;
        
        // Start with the open bag texture
        var builder = getBuilder(modelName)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", modLoc("item/potion_bag_open"));
        
        // Add slot overlays based on the mask
        int layerCount = 1;
        for (int i = 0; i < 5; i++) {
            if ((mask & (1 << i)) != 0) {
                builder.texture("layer" + layerCount, modLoc("item/potion_bag_slot_" + (i + 1)));
                layerCount++;
            }
        }
    }
} 