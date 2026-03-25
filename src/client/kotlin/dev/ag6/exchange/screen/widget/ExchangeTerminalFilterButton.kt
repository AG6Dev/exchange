package dev.ag6.exchange.screen.widget

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item

class ExchangeTerminalFilterButton(
    x: Int,
    y: Int,
    private val itemIcon: Item,
    message: Component,
    createNarration: CreateNarration
) : Button(x, y, WIDTH, HEIGHT, message, {}, createNarration) {
    override fun renderContents(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        guiGraphics.renderFakeItem(itemIcon.defaultInstance, mouseX, mouseY)
    }

    override fun onPress(input: InputWithModifiers) {
        super.onPress(input)
    }

    companion object {
        private const val WIDTH = 40
        private const val HEIGHT = 20
    }
}