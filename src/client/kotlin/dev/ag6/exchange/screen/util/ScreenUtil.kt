package dev.ag6.exchange.screen.util

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

private const val SLOT_SIZE = 18
private val SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back")
private val SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front")

fun renderFakeSlot(
    guiGraphics: GuiGraphics,
    font: Font,
    x: Int,
    y: Int,
    itemStack: ItemStack,
    mouseX: Int,
    mouseY: Int
) {
    val isHovered = mouseX in x until (x + SLOT_SIZE) && mouseY in y until (y + SLOT_SIZE)

    if (isHovered) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, x - 4, y - 4, 24, 24)
    }

    if (!itemStack.isEmpty) {
        val seed = x + y * 31

        guiGraphics.renderFakeItem(itemStack, x, y, seed)
        guiGraphics.renderItemDecorations(font, itemStack, x, y)

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(font, itemStack, mouseX, mouseY)
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24)
        }
    }
}
