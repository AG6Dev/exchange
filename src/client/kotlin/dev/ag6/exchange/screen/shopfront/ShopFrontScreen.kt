package dev.ag6.exchange.screen.shopfront

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.ShopFrontMenu
import dev.ag6.exchange.offer.ExchangeOffer
import dev.ag6.exchange.screen.widget.OfferSelectionList
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

abstract class ShopFrontScreen<T : ShopFrontMenu>(
    menu: T,
    inventory: Inventory,
    title: Component,
    private val texture: Identifier = Exchange.id("textures/gui/shop_front.png"),
    private val guiWidth: Int = 313,
    private val guiHeight: Int = 202,
    private val textureWidth: Int = 512,
    private val textureHeight: Int = 512,
    private val renderInventoryLabel: Boolean = false,
    private val showWidgets: Boolean = true,
    private val onOfferSelected: ((ExchangeOffer) -> Unit)? = null
) : AbstractContainerScreen<T>(menu, inventory, title) {

    lateinit var searchBox: EditBox
    lateinit var offerList: OfferSelectionList

    override fun init() {
        this.imageWidth = guiWidth
        this.imageHeight = guiHeight

        super.init()

        if (showWidgets) {
            searchBox = EditBox(
                font, leftPos + 8, topPos + 20, 198, 20, Exchange.translatable("container", "shop_front.search_prompt")
            )
            searchBox.setResponder(::onSearchChanged)
            offerList = OfferSelectionList(minecraft, leftPos + 8, topPos + 48, 198, 140, onOfferSelected)
            refreshOfferList()
            ExchangeClientNetworking.sendSubscribeShopOffersPayload(menu.blockEntity.blockPos)

            addRenderableWidget(searchBox)
            addRenderableWidget(offerList)
        }
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            texture,
            leftPos,
            topPos,
            0f,
            0f,
            guiWidth,
            guiHeight,
            textureWidth,
            textureHeight
        )
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (renderInventoryLabel) {
            super.renderLabels(guiGraphics, mouseX, mouseY)
        } else {
            guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (::offerList.isInitialized && offerList.isMouseOver(mouseX, mouseY) && offerList.mouseScrolled(
                mouseX, mouseY, scrollX, scrollY
            )
        ) {
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (::searchBox.isInitialized && searchBox.canConsumeInput()) {
            if (searchBox.keyPressed(event)) {
                return true
            }

            if (minecraft.options.keyInventory.matches(event)) {
                return true
            }
        }

        return super.keyPressed(event)
    }

    fun refreshOfferList(newQuery: String? = null) {
        if (::offerList.isInitialized) {
            val offers = filterCachedOffersWithSearch(newQuery ?: searchBox.value)
            offerList.setOffers(offers)
        }
    }

    private fun onSearchChanged(query: String) {
        refreshOfferList(newQuery = query)

        if (query.isNotBlank()) {
            offerList.setScrollAmount(0.0)
        }
    }

    private fun filterCachedOffersWithSearch(query: String): List<ExchangeOffer> {
        val shopOffers = ExchangeClientNetworking.getCachedOffers(menu.blockEntity.blockPos)

        if (query.isEmpty()) return shopOffers

        val matching: MutableList<ExchangeOffer> = mutableListOf()
        for (offer in shopOffers) {
            val playerFilter =
                offer.seller.name.contains(query, true) || offer.seller.id.toString().contains(query, true)

            val offeredFilter = offer.offeredItems.any { stack -> stackMatchesSearch(stack, query) }
            val receivingFilter = offer.receivingItems.any { stack -> stackMatchesSearch(stack, query) }

            if (playerFilter || offeredFilter || receivingFilter) {
                matching.add(offer)
            }
        }

        return matching
    }

    private fun stackMatchesSearch(stack: ItemStack, query: String): Boolean {
        val itemId = BuiltInRegistries.ITEM.getKey(stack.item)

        return itemId.toString().contains(query, true) ||
                itemId.path.contains(query, true) ||
                stack.hoverName.string.contains(query, true) ||
                stack.itemName.string.contains(query, true)
    }

}
