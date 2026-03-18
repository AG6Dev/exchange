package dev.ag6.exchange

import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.init.KeyMappingInit
import dev.ag6.exchange.network.TradeRequestPayload
import dev.ag6.exchange.screen.TradeScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.world.entity.player.Player

object ExchangeClient : ClientModInitializer {
    override fun onInitializeClient() {
        KeyMappingInit.init()
        MenuScreens.register(MenuTypeInit.TRADE_CONTAINER_SCREEN, ::TradeScreen)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (KeyMappingInit.tradeKey.consumeClick()) {
                val player = client.player ?: return@register
                val target = client.crosshairPickEntity as? Player
                if (target != null && target != player) {
                    ClientPlayNetworking.send(TradeRequestPayload(target.uuid))
                }
            }
        }
    }
}
