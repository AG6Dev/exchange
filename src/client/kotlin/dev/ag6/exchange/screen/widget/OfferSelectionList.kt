package dev.ag6.exchange.screen.widget

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.offer.ExchangeOffer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.world.item.ItemStack

class OfferSelectionList(
    mc: Minecraft, x: Int, y: Int, width: Int, height: Int
) : ContainerObjectSelectionList<OfferSelectionList.ListEntry>(mc, width, height, y, ENTRY_HEIGHT) {
    init {
        this.setPosition(x, y)
    }

    fun setOffers(offers: List<ExchangeOffer>) {
        clearEntries()
        offers.forEach { offer ->
            addEntry(ListEntry(offer))
        }
    }

    override fun getNextY(): Int {
        return super.getNextY() + 8
    }

    class ListEntry(private val offer: ExchangeOffer) : Entry<ListEntry>() {
        override fun renderContent(
            guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovering: Boolean, partialTick: Float
        ) {
            val textRenderer = Minecraft.getInstance().font
            val cardX = getActualX()

            guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE, cardX, contentY, 0f, 0f, CARD_WIDTH, CARD_HEIGHT, 256, 256
            )

            renderItems(guiGraphics, mouseX, mouseY, cardX + ITEM_ROW_X, contentY + TOP_ROW_Y, offer.offeredItems)
            renderItems(guiGraphics, mouseX, mouseY, cardX + ITEM_ROW_X, contentY + BOTTOM_ROW_Y, offer.receivingItems)

            val sellerUsername = Minecraft.getInstance().services().nameToIdCache.get(offer.seller)
            guiGraphics.drawString(textRenderer, sellerUsername.get().name, x + 122, y + 11, -12566464, false)
            guiGraphics.drawString(textRenderer, offer.location.toShortString(), x + 122, y + 22, -12566464, false)

            if (isHoveringArrow(mouseX, mouseY, cardX + 4, y + 10)) {
                guiGraphics.renderTooltip(
                    textRenderer, listOf(
                        ClientTooltipComponent.create(
                            Exchange.translatable(
                                "container", "widget.offer_arrow_selling"
                            ).visualOrderText
                        )
                    ), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null
                )
            } else if (isHoveringArrow(mouseX, mouseY, cardX + 4, y + 35)) {
                guiGraphics.renderTooltip(
                    textRenderer, listOf(
                        ClientTooltipComponent.create(
                            Exchange.translatable(
                                "container", "widget.offer_arrow_wanted"
                            ).visualOrderText
                        )
                    ), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null
                )
            }
        }

        fun getActualX(): Int = x + CARD_X_OFFSET

        private fun renderItems(
            guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, startX: Int, startY: Int, stacks: List<ItemStack>
        ) {
            stacks.take(4).forEachIndexed { index, stack ->
                renderFakeSlotWithItem(
                    guiGraphics,
                    Minecraft.getInstance().font,
                    startX + index * 18,
                    startY,
                    stack,
                    mouseX,
                    mouseY
                )
            }
        }

        private fun isHoveringArrow(mouseX: Int, mouseY: Int, arrowXPos: Int, arrowYPos: Int): Boolean {
            return mouseX in arrowXPos..arrowXPos + 18 && mouseY in arrowYPos..arrowYPos + 15
        }

        override fun narratables(): List<NarratableEntry> {
            return listOf()
        }

        override fun children(): List<GuiEventListener> {
            return listOf()
        }

        private fun renderFakeSlotWithItem(
            guiGraphics: GuiGraphics, font: Font, x: Int, y: Int, itemStack: ItemStack, mouseX: Int, mouseY: Int
        ) {
            val isHovered = mouseX in x until (x + 18) && mouseY in y until (y + 18)

            if (isHovered) {
                guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE,
                    x - 4,
                    y - 4,
                    24,
                    24
                )
            }

            if (!itemStack.isEmpty) {
                guiGraphics.renderFakeItem(itemStack, x, y, 0)
                guiGraphics.renderItemDecorations(font, itemStack, x, y)

                if (isHovered) {
                    guiGraphics.setTooltipForNextFrame(font, itemStack, mouseX, mouseY)
                    guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE,
                        x - 4,
                        y - 4,
                        24,
                        24
                    )
                }
            }
        }
    }

    companion object {
        const val ENTRY_HEIGHT = 65

        private const val CARD_WIDTH = 179
        private const val CARD_HEIGHT = 57
        private const val CARD_X_OFFSET = 21
        private const val ITEM_ROW_X = 26
        private const val TOP_ROW_Y = 8
        private const val BOTTOM_ROW_Y = 33

        private val TEXTURE = Exchange.id("textures/gui/widget/offer_list_card.png")
    }
}
