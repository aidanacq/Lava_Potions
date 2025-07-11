package net.quoky.lava_potions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.PotionBagItem;
import org.joml.Matrix4f;

public class PotionBagRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation CLOSED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID,
            "item/potion_bag_closed");
    private static final ResourceLocation OPEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID,
            "item/potion_bag_open");
    private static final ResourceLocation[] SLOT_TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "item/potion_bag_slot_1"),
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "item/potion_bag_slot_2"),
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "item/potion_bag_slot_3"),
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "item/potion_bag_slot_4"),
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "item/potion_bag_slot_5")
    };

    private final ItemRenderer itemRenderer;

    public PotionBagRenderer(EntityModelSet modelSet) {
        super(null, modelSet);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay) {

        // Step 1: Render the base item using Minecraft's standard rendering
        BakedModel baseModel = itemRenderer.getModel(stack, null, null, 0);
        itemRenderer.render(stack, transformType, true, poseStack, buffer, combinedLight, combinedOverlay, baseModel);

        // Step 2: If the bag is open, render slot overlays on top
        /*
        if (PotionBagItem.isOpen(stack)) {
            boolean[] slotStates = PotionBagItem.getSlotStates(stack);
            for (int i = 0; i < slotStates.length && i < SLOT_TEXTURES.length; i++) {
                if (slotStates[i]) {
                    renderSlotOverlay(poseStack, buffer, SLOT_TEXTURES[i], combinedLight, combinedOverlay);
                }
            }
        }
        */
    }

    /*
    private void renderSlotOverlay(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture,
                                 int combinedLight, int combinedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.cutout());
        Matrix4f matrix = poseStack.last().pose();

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // Use the same dimensions as the base item for proper alignment
        float size = 1.0F;
        float x0 = -size * 0.5F;
        float y0 = -size * 0.5F;
        float x1 = size * 0.5F;
        float y1 = size * 0.5F;
        float z = 0.0F;

        // Render the overlay quad
        vertexConsumer.vertex(matrix, x0, y0, z).color(255, 255, 255, 255)
                .uv(minU, maxV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x1, y0, z).color(255, 255, 255, 255)
                .uv(maxU, maxV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x1, y1, z).color(255, 255, 255, 255)
                .uv(maxU, minV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x0, y1, z).color(255, 255, 255, 255)
                .uv(minU, minV).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
    }
    */
}