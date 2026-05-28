package dev.ag6.exchange.screen.shopfront

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.ShopFrontMenu
import dev.ag6.exchange.screen.widget.OfferSelectionList
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

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
    private val showWidgets: Boolean = true
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
            offerList = OfferSelectionList(minecraft, leftPos + 8, topPos + 48, 198, 140)
            refreshOfferList()

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
                mouseX,
                mouseY,
                scrollX,
                scrollY
            )
        ) {
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    fun refreshOfferList() {
        if (::offerList.isInitialized) {
            offerList.setOffers(ExchangeClientNetworking.offersCache.filter { it.location == menu.blockEntity.blockPos })
        }
    }
}