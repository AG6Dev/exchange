package dev.ag6.exchange.screen.shopfront.owner

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.owner.ShopFrontOwnerMenu
import dev.ag6.exchange.screen.widget.OfferSelectionList
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class ShopFrontOwnerScreen(menu: ShopFrontOwnerMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<ShopFrontOwnerMenu>(menu, inventory, title) {

    lateinit var offerList: OfferSelectionList

    override fun init() {
        this.imageWidth = IMAGE_WIDTH
        this.imageHeight = IMAGE_HEIGHT
        super.init()

        offerList = OfferSelectionList(minecraft, leftPos + 108, topPos + 48, 198, 140)
        refreshOfferList()
        addRenderableWidget(Button.builder(Exchange.translatable("container", "shop_front_owner.create_trade")) {
            ExchangeClientNetworking.sendOpenCreateOfferMenuPayload(menu.blockEntity.blockPos)
        }.build())
        addRenderableWidget(offerList)
    }

    override fun renderBg(
        guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int
    ) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 512
        )
    }

    fun refreshOfferList() {
        if (::offerList.isInitialized) {
            offerList.setOffers(ExchangeClientNetworking.offersCache.filter { it.location == menu.blockEntity.blockPos })
        }
    }

    companion object {
        private const val IMAGE_WIDTH = 313
        private const val IMAGE_HEIGHT = 202

        private val TEXTURE = Exchange.id("textures/gui/shop_front_owner.png")
    }
}