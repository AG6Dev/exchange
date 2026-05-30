package dev.ag6.exchange.screen.shopfront

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.menu.shopfront.CompleteTradeMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class CompleteTradeScreen(menu: CompleteTradeMenu, playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<CompleteTradeMenu>(menu, playerInventory, title) {
    override fun init() {
        super.init()

        addRenderableWidget(Button.builder(Exchange.translatable("container", "complete_trade.confirm")) {
            minecraft.gameMode?.handleInventoryButtonClick(menu.containerId, CompleteTradeMenu.BUTTON_CONFIRM_ID)
        }.bounds(leftPos + 116, topPos + 24, 46, 18).build())

        addRenderableWidget(Button.builder(Exchange.translatable("container", "complete_trade.cancel")) {
            minecraft.gameMode?.handleInventoryButtonClick(menu.containerId, CompleteTradeMenu.BUTTON_CANCEL_ID)
        }.bounds(leftPos + 116, topPos + 49, 46, 18).build())
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
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            256,
            256
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    companion object {
        private val TEXTURE = Exchange.id("textures/gui/create_offer.png")
        private const val IMAGE_WIDTH = 175
        private const val IMAGE_HEIGHT = 165
    }
}
