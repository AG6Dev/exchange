package dev.ag6.exchange.screen.shopfront.owner

import dev.ag6.exchange.menu.shopfront.owner.ShopFrontInventoryMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class ShopFrontInventoryScreen(menu: ShopFrontInventoryMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<ShopFrontInventoryMenu>(menu, inventory, title) {

    override fun init() {
        super.init()
        this.inventoryLabelY++
    }

    override fun renderBg(
        guiGraphics: GuiGraphics,
        partialTick: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            leftPos,
            topPos,
            0f,
            0f,
            imageWidth,
            imageHeight,
            256,
            256
        )
    }

    companion object {
        private val TEXTURE: Identifier = Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png")
    }
}