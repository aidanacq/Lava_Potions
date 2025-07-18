package net.quoky.lava_potions.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.quoky.lava_potions.item.PotionBagMenu;

public class PotionBagScreen extends AbstractContainerScreen<PotionBagMenu> {
    private static final ResourceLocation HOPPER_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/hopper.png");
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation("lava_potions", "textures/gui/potion_bag_overlay.png");

    public PotionBagScreen(PotionBagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 133; // Hopper GUI height
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Render the vanilla hopper texture
        guiGraphics.blit(HOPPER_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // Render the custom overlay on top
        guiGraphics.blit(OVERLAY_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void onClose() {
        super.onClose();
        // The menu's removed() method will handle closing the bag
    }
} 