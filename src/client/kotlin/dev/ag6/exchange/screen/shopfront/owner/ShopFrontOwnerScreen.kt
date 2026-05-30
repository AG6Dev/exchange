package dev.ag6.exchange.screen.shopfront.owner

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.owner.ShopFrontOwnerMenu
import dev.ag6.exchange.screen.shopfront.ShopFrontScreen
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class ShopFrontOwnerScreen(menu: ShopFrontOwnerMenu, inventory: Inventory, title: Component) :
    ShopFrontScreen<ShopFrontOwnerMenu>(menu, inventory, title) {

    override fun init() {
        super.init()

        addRenderableWidget(Button.builder(Exchange.translatable("container", "shop_front_owner.view_inventory")) {
            minecraft.gameMode?.handleInventoryButtonClick(menu.containerId, ShopFrontOwnerMenu.BUTTON_VIEW_INVENTORY)
        }.bounds(leftPos + 214, topPos + 20, 90, 20).build())
        addRenderableWidget(Button.builder(Exchange.translatable("container", "shop_front_owner.create_trade")) {
            minecraft.gameMode?.handleInventoryButtonClick(menu.containerId, ShopFrontOwnerMenu.BUTTON_CREATE_OFFER)
        }.bounds(leftPos + 214, topPos + 48, 90, 20).build())
        addRenderableWidget(
            Button.builder(
                getStatusMessage()
            ) {
                val newStatus = !menu.blockEntity.isOpen
                menu.blockEntity.isOpen = newStatus
                ExchangeClientNetworking.sendSetShopOpenStatusPayload(newStatus, menu.blockEntity.blockPos)
                it.message = getStatusMessage()
            }.bounds(leftPos + 214, topPos + 76, 90, 20).build()
        )
    }

    private fun getStatusMessage(): Component = if (menu.blockEntity.isOpen) Exchange.translatable(
        "container", "shop_front_owner.close_shop"
    ) else Exchange.translatable("container", "shop_front_owner.open_shop")

}
