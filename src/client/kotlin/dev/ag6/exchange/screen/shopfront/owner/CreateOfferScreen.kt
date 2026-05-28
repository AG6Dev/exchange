package dev.ag6.exchange.screen.shopfront.owner

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.owner.CreateOfferMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class CreateOfferScreen(menu: CreateOfferMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<CreateOfferMenu>(menu, inventory, title) {
    override fun init() {
        super.init()

        addRenderableWidget(Button.builder(Exchange.translatable("container", "create_offer.confirm")) {
            val itemsReceiving = (0 until 4)
                .map { menu.getSlot(it).item.copy() }
                .filter { !it.isEmpty }

            val itemsGiving = (4 until 8)
                .map { menu.getSlot(it).item.copy() }
                .filter { !it.isEmpty }

            ExchangeClientNetworking.sendAddOfferPayload(menu.blockEntity.blockPos, itemsGiving, itemsReceiving)
        }.bounds(leftPos + 116, topPos + 24, 46, 18).build())

        addRenderableWidget(Button.builder(Exchange.translatable("container", "create_offer.cancel")) {
            onClose()
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

    companion object {
        private val TEXTURE = Exchange.id("textures/gui/create_offer.png")
        private const val IMAGE_WIDTH = 175
        private const val IMAGE_HEIGHT = 165
    }
}