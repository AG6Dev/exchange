package dev.ag6.exchange.screen.widget

import dev.ag6.exchange.blockentity.ExchangeOffer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry

class OfferSelectionList(
    mc: Minecraft,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    itemHeight: Int
) : ContainerObjectSelectionList<OfferSelectionList.ListEntry>(mc, width, height, y, itemHeight) {
    init {
        this.setPosition(x, y)
    }

    public override fun addEntry(entry: ListEntry): Int {
        return super.addEntry(entry)
    }

    class ListEntry(private val offer: ExchangeOffer) : Entry<ListEntry>() {
        override fun renderContent(
            guiGraphics: GuiGraphics,
            mouseX: Int,
            mouseY: Int,
            isHovering: Boolean,
            partialTick: Float
        ) {
            guiGraphics.renderFakeItem(offer.offeredItems.first(), mouseX, mouseY)
        }

        override fun narratables(): List<NarratableEntry> {
            return listOf()
        }

        override fun children(): List<GuiEventListener> {
            return listOf()
        }
    }
}