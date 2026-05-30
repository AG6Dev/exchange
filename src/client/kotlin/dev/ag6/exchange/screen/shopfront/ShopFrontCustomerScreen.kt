package dev.ag6.exchange.screen.shopfront

import dev.ag6.exchange.ExchangeClientNetworking
import dev.ag6.exchange.menu.shopfront.ShopFrontCustomerMenu
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class ShopFrontCustomerScreen(menu: ShopFrontCustomerMenu, playerInv: Inventory, title: Component) :
    ShopFrontScreen<ShopFrontCustomerMenu>(
        menu,
        playerInv,
        title,
        onOfferSelected = { offer ->
            ExchangeClientNetworking.sendOpenCompleteTradeMenuPayload(menu.blockEntity.blockPos, offer.id)
        }
    )
