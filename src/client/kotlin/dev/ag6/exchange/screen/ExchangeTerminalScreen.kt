package dev.ag6.exchange.screen

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ExchangeOffer
import dev.ag6.exchange.menu.ExchangeTerminalMenu
import dev.ag6.exchange.network.AddOfferPayload
import dev.ag6.exchange.screen.widget.OfferSelectionList
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Items

class ExchangeTerminalScreen(menu: ExchangeTerminalMenu, private val playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<ExchangeTerminalMenu>(menu, playerInventory, title) {

    lateinit var searchBox: EditBox
    lateinit var selectionList: OfferSelectionList

    override fun init() {
        imageWidth = 298
        imageHeight = 202
        super.init()

        searchBox = createSearchBox()
        selectionList = OfferSelectionList(
            minecraft,
            leftPos + 108,
            topPos + 48,
            183,
            140,
            OfferSelectionList.ENTRY_HEIGHT
        )
        refreshOffers()

        val btn = Button.builder(Component.literal("Add trade")) { _ ->
            ClientPlayNetworking.send(AddOfferPayload(listOf(Items.BEDROCK.defaultInstance), listOf(Items.BEEF.defaultInstance)))
        }.size(32, 16).pos(leftPos + 7, topPos + 50).build()

        addRenderableWidget(searchBox)
        addRenderableWidget(btn)
        addRenderableWidget(selectionList)

    }

    override fun renderBg(
        guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int
    ) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 512, 512
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    private fun createSearchBox(): EditBox = EditBox(
        font, leftPos + 7, topPos + 26, 97, 16, Exchange.translatable("container", "exchange_terminal.search")
    ).apply {
        setResponder(::onSearchChanged)
    }

    private fun onSearchChanged(newValue: String) {

    }

    fun applyOfferSnapshot(offers: List<ExchangeOffer>) {
        menu.replaceOffers(offers)
        refreshOffers()
    }

    fun refreshOffers() {
        if (!::selectionList.isInitialized) {
            return
        }

        selectionList.setOffers(menu.offers)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (searchBox.isFocused) {
            if (searchBox.keyPressed(event)) {
                return true
            }
        }
        return super.keyPressed(event)
    }

    companion object {
        val TEXTURE = Exchange.id("textures/gui/exchange_terminal.png")
    }
}
