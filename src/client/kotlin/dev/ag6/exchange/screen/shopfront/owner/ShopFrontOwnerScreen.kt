package dev.ag6.exchange.screen.shopfront.owner

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.owner.ShopFrontOwnerMenu
import dev.ag6.exchange.screen.widget.OfferSelectionList
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class ShopFrontOwnerScreen(menu: ShopFrontOwnerMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<ShopFrontOwnerMenu>(menu, inventory, title) {

    lateinit var offerList: OfferSelectionList
    lateinit var searchBox: EditBox

    override fun init() {
        this.imageWidth = IMAGE_WIDTH
        this.imageHeight = IMAGE_HEIGHT
        super.init()

        offerList = OfferSelectionList(minecraft, leftPos + 8, topPos + 48, 198, 140)
        refreshOfferList()
        searchBox = EditBox(
            font,
            leftPos + 8,
            topPos + 20,
            198,
            20,
            Component.translatableEscape("container", "shop_front.search_prompt")
        )
        addRenderableWidget(
            searchBox
        )
        addRenderableWidget(Button.builder(Exchange.translatable("container", "shop_front_owner.view_inventory")) {
            ExchangeClientNetworking.sendOpenShopInventoryMenuPayload(menu.blockEntity.blockPos)
        }.bounds(leftPos + 214, topPos + 20, 90, 20).build())
        addRenderableWidget(Button.builder(Exchange.translatable("container", "shop_front_owner.create_trade")) {
            ExchangeClientNetworking.sendOpenCreateOfferMenuPayload(menu.blockEntity.blockPos)
        }.bounds(leftPos + 214, topPos + 48, 90, 20).build())
        addRenderableWidget(
            Button.builder(
                getStatusMessage()
            ) {
                val newStatus = !menu.blockEntity.isOpen
                menu.blockEntity.isOpen = newStatus
                ExchangeClientNetworking.sendSetShopOpenStatusPayload(newStatus, menu.blockEntity.blockPos)
                it.message = getStatusMessage()
            }.bounds(leftPos + 214, topPos + 76, 90, 20).build()
        )
        addRenderableWidget(offerList)
    }

    override fun renderBg(
        guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int
    ) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 512
        )
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false)
    }

    fun refreshOfferList() {
        if (::offerList.isInitialized) {
            offerList.setOffers(ExchangeClientNetworking.offersCache.filter { it.location == menu.blockEntity.blockPos })
        }
    }

    private fun getStatusMessage(): Component = if (menu.blockEntity.isOpen) Exchange.translatable(
        "container", "shop_front_owner.close_shop"
    ) else Exchange.translatable("container", "shop_front_owner.open_shop")

    companion object {
        private const val IMAGE_WIDTH = 313
        private const val IMAGE_HEIGHT = 202

        private val TEXTURE = Exchange.id("textures/gui/shop_front_owner.png")
    }
}
