package dev.ag6.exchange

import dev.ag6.exchange.init.KeyMappingInit
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.screen.TradeScreen
import dev.ag6.exchange.screen.shopfront.CompleteTradeScreen
import dev.ag6.exchange.screen.shopfront.ShopFrontCustomerScreen
import dev.ag6.exchange.screen.shopfront.owner.CreateOfferScreen
import dev.ag6.exchange.screen.shopfront.owner.ShopFrontInventoryScreen
import dev.ag6.exchange.screen.shopfront.owner.ShopFrontOwnerScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.world.entity.player.Player

object ExchangeClient : ClientModInitializer {
    override fun onInitializeClient() {
        KeyMappingInit.init()
        ExchangeClientNetworking.init()

        registerMenuScreens()

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (KeyMappingInit.tradeKey.consumeClick()) {
                val player = client.player ?: return@register
                val target = client.crosshairPickEntity as? Player
                if (target != null && target != player) {
                    ExchangeClientNetworking.sendTradeRequestPayload(target.uuid)
                }
            }
        }
    }

    private fun registerMenuScreens() {
        MenuScreens.register(MenuTypeInit.TRADE, ::TradeScreen)
        MenuScreens.register(MenuTypeInit.SHOP_FRONT_OWNER, ::ShopFrontOwnerScreen)
        MenuScreens.register(MenuTypeInit.CREATE_TRADE, ::CreateOfferScreen)
        MenuScreens.register(MenuTypeInit.SHOP_FRONT_INVENTORY, ::ShopFrontInventoryScreen)
        MenuScreens.register(MenuTypeInit.SHOP_FRONT_CUSTOMER, ::ShopFrontCustomerScreen)
        MenuScreens.register(MenuTypeInit.COMPLETE_TRADE, ::CompleteTradeScreen)
    }
}
