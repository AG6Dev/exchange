package dev.ag6.exchange.screen

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ExchangeOffer
import dev.ag6.exchange.screen.widget.OfferSelectionList
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.renderer.RenderPipelines

class ExchangeTerminalScreen : Screen(TITLE) {
    private val offers: MutableList<ExchangeOffer> = mutableListOf()

    private var leftPos = 0
    private var topPos = 0

    lateinit var searchBox: EditBox
    lateinit var selectionList: OfferSelectionList

    override fun init() {
        leftPos = (width - IMAGE_WIDTH) / 2
        topPos = (height - IMAGE_HEIGHT) / 2
        super.init()

        searchBox = createSearchBox()
        selectionList = OfferSelectionList(
            minecraft,
            leftPos + 108,
            topPos + 48,
            198,
            140,
            OfferSelectionList.ENTRY_HEIGHT
        )
        refreshOffers()

        addRenderableWidget(searchBox)
        addRenderableWidget(selectionList)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 512)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    private fun createSearchBox(): EditBox = EditBox(
        font, leftPos + 7, topPos + 26, 97, 16, Exchange.translatable("container", "exchange_terminal.search")
    ).apply {
        setResponder(::onSearchChanged)
    }

    private fun onSearchChanged(newValue: String) {
        selectionList.setOffers(filterOffers(newValue))
    }

    private fun filterOffers(query: String): List<ExchangeOffer> {
        if (query.isBlank()) return offers.toList()

        val lower = query.lowercase()
        val isRegistryQuery = ':' in lower

        return offers.filter { offer ->
            val allStacks = offer.offeredItems + offer.receivingItems

            val itemMatches = if (isRegistryQuery) {
                allStacks.any { stack ->
                    stack.item.builtInRegistryHolder().key().identifier().toString().lowercase().contains(lower)
                }
            } else {
                allStacks.any { stack ->
                    stack.hoverName.string.lowercase().contains(lower)
                }
            }

            val playerName = minecraft.services().nameToIdCache.get(offer.seller).get()?.name ?: ""
            val playerMatches = playerName.lowercase().contains(lower)

            itemMatches || playerMatches
        }
    }

    fun applyOfferSnapshot(newOffers: List<ExchangeOffer>) {
        offers.clear()
        offers.addAll(newOffers)
        refreshOffers()
    }

    fun refreshOffers() {
        if (!::selectionList.isInitialized) return
        selectionList.setOffers(filterOffers(searchBox.value))
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (searchBox.isFocused && searchBox.keyPressed(event)) return true
        return super.keyPressed(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (::selectionList.isInitialized && selectionList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    companion object {
        val TEXTURE = Exchange.id("textures/gui/exchange_terminal.png")
        private val TITLE = Exchange.translatable("container", "exchange_terminal")
        private const val IMAGE_WIDTH = 313
        private const val IMAGE_HEIGHT = 202
    }
}
