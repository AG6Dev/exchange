package dev.ag6.exchange

import dev.ag6.exchange.network.AddOfferPayload
import dev.ag6.exchange.network.OpenCreateTradeMenuPayload
import dev.ag6.exchange.network.SyncExchangeOffersPayload
import dev.ag6.exchange.network.TradeRequestPayload
import dev.ag6.exchange.offer.ExchangeOffer
import dev.ag6.exchange.screen.shopfront.owner.ShopFrontOwnerScreen
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.*

object ExchangeClientNetworking {
    val offersCache: MutableList<ExchangeOffer> = mutableListOf()

    fun init() {
        registerClientReceivers()
    }

    fun sendOpenCreateOfferMenuPayload(pos: BlockPos) = ClientPlayNetworking.send(OpenCreateTradeMenuPayload(pos))

    fun sendAddOfferPayload(pos: BlockPos, itemsGiving: List<ItemStack>, itemsReceiving: List<ItemStack>) =
        ClientPlayNetworking.send(AddOfferPayload(pos, itemsGiving, itemsReceiving))

    fun sendTradeRequestPayload(targetUuid: UUID) = ClientPlayNetworking.send(TradeRequestPayload(targetUuid))

    private fun syncExchangeOffersPayloadReceiver() = ClientPlayNetworking.registerGlobalReceiver(
        SyncExchangeOffersPayload.TYPE
    ) { payload, context ->

        offersCache.clear()
        offersCache.addAll(payload.offers)

        val screen = context.client().screen
        if (screen is ShopFrontOwnerScreen) {
            screen.refreshOfferList()
        }
    }

    private fun registerClientReceivers() {
        syncExchangeOffersPayloadReceiver()
    }
}