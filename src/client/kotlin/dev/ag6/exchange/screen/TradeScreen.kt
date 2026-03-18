package dev.ag6.exchange.screen

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.menu.TradeMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory

class TradeScreen(menu: TradeMenu, private val playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<TradeMenu>(menu, playerInventory, title) {

    private val remotePlayerName = extractRemotePlayerName(title)

    override fun init() {
        super.init()
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2

        addRenderableWidget(
            Button.builder(Component.literal("Accept")) {
                minecraft.gameMode?.handleInventoryButtonClick(menu.containerId, TradeMenu.BUTTON_ACCEPT_ID)
            }.bounds(leftPos + 8, topPos + 174, 80, 20).build()
        )

        addRenderableWidget(
            Button.builder(Component.literal("Cancel")) {
                minecraft.gameMode?.handleInventoryButtonClick(menu.containerId, TradeMenu.BUTTON_CANCEL_ID)
            }.bounds(leftPos + 88, topPos + 174, 80, 20).build()
        )
    }

    override fun renderBg(guiGraphics: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        //x, y, u, v, width, height, textureWidth, textureHeight, color
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            leftPos,
            topPos,
            0f,
            0f,
            imageWidth,
            imageHeight,
            256,
            256
        )

        renderPlayerFace(guiGraphics, leftPos + 66, topPos + 36, resolveLocalPlayerInfo(), menu.localAccepted)
        renderPlayerFace(guiGraphics, leftPos + 98, topPos + 36, resolveRemotePlayerInfo(), menu.remoteAccepted)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        super.renderLabels(guiGraphics, mouseX, mouseY)
        guiGraphics.drawString(font, Component.literal("Their offer"), 8, 8, 0xFFFFFF, false)
        guiGraphics.drawString(font, Component.literal("Your offer"), 8, 44, 0xFFFFFF, false)
        guiGraphics.drawString(
            font,
            Component.literal(if (menu.remoteAccepted) "Accepted" else "Reviewing"),
            116,
            8,
            if (menu.remoteAccepted) 0x7CFC00 else 0xFFCC66,
            false
        )
        guiGraphics.drawString(
            font,
            Component.literal(if (menu.localAccepted) "Accepted" else "Reviewing"),
            116,
            44,
            if (menu.localAccepted) 0x7CFC00 else 0xFFCC66,
            false
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(guiGraphics, mouseX, mouseY, delta)
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    private fun renderPlayerFace(guiGraphics: GuiGraphics, x: Int, y: Int, playerInfo: PlayerInfo?, accepted: Boolean) {
        if (playerInfo == null) {
            return
        }

        if (accepted) {
            guiGraphics.fill(x - 2, y - 2, x + FACE_SIZE + 2, y + FACE_SIZE + 2, ACCEPTED_OUTLINE_COLOR)
        }

        guiGraphics.fill(x - 1, y - 1, x + FACE_SIZE + 1, y + FACE_SIZE + 1, FACE_BORDER_COLOR)
        PlayerFaceRenderer.draw(guiGraphics, playerInfo.skin, x, y, FACE_SIZE)
    }

    private fun resolveLocalPlayerInfo(): PlayerInfo? {
        val player = playerInventory.player
        return minecraft.connection?.getPlayerInfo(player.uuid)
    }

    private fun resolveRemotePlayerInfo(): PlayerInfo? {
        val remotePlayer = minecraft.level?.players()?.firstOrNull(::isRemoteTradePartner) ?: return null
        return minecraft.connection?.getPlayerInfo(remotePlayer.uuid)
    }

    private fun isRemoteTradePartner(player: Player): Boolean {
        return player.uuid != playerInventory.player.uuid && player.name.string == remotePlayerName
    }

    private fun extractRemotePlayerName(title: Component): String? {
        val contents = title.contents as? TranslatableContents ?: return null
        val remoteName = contents.args.firstOrNull() ?: return null
        return when (remoteName) {
            is Component -> remoteName.string
            is String -> remoteName
            else -> remoteName.toString()
        }
    }

    companion object {
        val TEXTURE = Exchange.id("textures/gui/trade.png")
        private const val FACE_SIZE = 12
        private const val FACE_BORDER_COLOR = -0x1000000
        private const val ACCEPTED_OUTLINE_COLOR = -0x830100
    }
}
