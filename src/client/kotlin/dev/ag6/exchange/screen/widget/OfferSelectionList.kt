package dev.ag6.exchange.screen.widget

import dev.ag6.exchange.blockentity.ExchangeOffer
import dev.ag6.exchange.screen.ExchangeTerminalScreen
import dev.ag6.exchange.screen.util.renderFakeSlot
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class OfferSelectionList(
    mc: Minecraft, x: Int, y: Int, width: Int, height: Int, itemHeight: Int
) : ContainerObjectSelectionList<OfferSelectionList.ListEntry>(mc, width, height, y, itemHeight) {
    init {
        this.setPosition(x, y)
    }

    fun setOffers(offers: List<ExchangeOffer>) {
        clearEntries()
        offers.forEach { offer ->
            addEntry(ListEntry(offer))
        }
    }

    override fun scrollBarX(): Int {
        return x + width - 6
    }

    class ListEntry(private val offer: ExchangeOffer) : Entry<ListEntry>() {
        override fun renderContent(
            guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovering: Boolean, partialTick: Float
        ) {
            val textRenderer = Minecraft.getInstance().font
            val contentY = y + ENTRY_GAP / 2
            val cardX = x + CARD_X_OFFSET
            val arrowCenterX = cardX + ARROW_CENTER_X

            guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ExchangeTerminalScreen.TEXTURE,
                cardX,
                contentY,
                314f,
                0f,
                CARD_WIDTH,
                CARD_HEIGHT,
                512,
                512
            )

            renderItems(guiGraphics, mouseX, mouseY, cardX + ITEM_ROW_X, contentY + TOP_ROW_Y, offer.offeredItems)
            renderItems(guiGraphics, mouseX, mouseY, cardX + ITEM_ROW_X, contentY + BOTTOM_ROW_Y, offer.receivingItems)

            val sellerUsername = Minecraft.getInstance().services().nameToIdCache.get(offer.seller)
            guiGraphics.drawString(textRenderer, sellerUsername.get().name, x + 122, y + 11, -12566464, false)
            guiGraphics.drawString(textRenderer, offer.terminalLocation.toShortString(), x + 122, y + 22, -12566464, false)

            if (isHoveringArrow(mouseX, mouseY, arrowCenterX, contentY + TOP_ARROW_Y)) {
                guiGraphics.renderTooltip(
                    textRenderer,
                    listOf(ClientTooltipComponent.create(Component.literal(SELLING_LABEL).visualOrderText)),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
                )
            } else if (isHoveringArrow(mouseX, mouseY, arrowCenterX, contentY + BOTTOM_ARROW_Y)) {
                guiGraphics.renderTooltip(
                    textRenderer,
                    listOf(ClientTooltipComponent.create(Component.literal(FOR_LABEL).visualOrderText)),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
                )
            }
        }

        private fun renderItems(
            guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, startX: Int, startY: Int, stacks: List<ItemStack>
        ) {
            val font = Minecraft.getInstance().font

            stacks.take(MAX_VISIBLE_ITEMS).forEachIndexed { index, stack ->
                renderFakeSlot(
                    guiGraphics = guiGraphics,
                    font = font,
                    x = startX + index * 18,
                    y = startY,
                    itemStack = stack,
                    mouseX = mouseX,
                    mouseY = mouseY
                )
            }
        }

        private fun isHoveringArrow(mouseX: Int, mouseY: Int, centerX: Int, centerY: Int): Boolean {
            val left = centerX - ARROW_HITBOX_WIDTH / 2
            val right = centerX + ARROW_HITBOX_WIDTH / 2
            val top = centerY - ARROW_HITBOX_HEIGHT / 2
            val bottom = centerY + ARROW_HITBOX_HEIGHT / 2
            return mouseX in left..right && mouseY in top..bottom
        }

        override fun narratables(): List<NarratableEntry> {
            return listOf()
        }

        override fun children(): List<GuiEventListener> {
            return listOf()
        }
    }

    companion object {
        const val ENTRY_HEIGHT = 65

        private const val CARD_WIDTH = 179
        private const val CARD_HEIGHT = 57
        private const val CARD_X_OFFSET = 21
        private const val ENTRY_GAP = ENTRY_HEIGHT - CARD_HEIGHT
        private const val ARROW_CENTER_X = 13
        private const val TOP_ARROW_Y = 14
        private const val BOTTOM_ARROW_Y = 35
        private const val ITEM_ROW_X = 26
        private const val TOP_ROW_Y = 8
        private const val BOTTOM_ROW_Y = 33
        private const val MAX_VISIBLE_ITEMS = 4
        private const val ARROW_HITBOX_WIDTH = 18
        private const val ARROW_HITBOX_HEIGHT = 15
        private const val SELLING_LABEL = "SELLING"
        private const val FOR_LABEL = "FOR"
    }
}
