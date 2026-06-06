package dev.ag6.exchange

import dev.ag6.exchange.network.*
import dev.ag6.exchange.screen.shopfront.ShopFrontScreen
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.*

//TODO: Add a generic "open menu" payload instead of creating a new payload for each menu
object ExchangeClientNetworking {
    private val offerCache = ShopOfferCache()

    fun init() {
        registerClientReceivers()
        registerDisconnectEventHandler()
    }

    fun sendOpenCompleteTradeMenuPayload(pos: BlockPos, offerId: UUID) =
        ClientPlayNetworking.send(OpenCompleteTradeMenuPayload(pos, offerId))

    fun sendAddOfferPayload(pos: BlockPos, itemsGiving: List<ItemStack>, itemsReceiving: List<ItemStack>) =
        ClientPlayNetworking.send(AddOfferPayload(pos, itemsGiving, itemsReceiving))

    fun sendTradeRequestPayload(targetUuid: UUID) = ClientPlayNetworking.send(TradeRequestPayload(targetUuid))

    fun sendSetShopOpenStatusPayload(newStatus: Boolean, pos: BlockPos) = ClientPlayNetworking.send(
        SetShopOpenStatusPayload(newStatus, pos)
    )

    fun sendSubscribeShopOffersPayload(pos: BlockPos) =
        ClientPlayNetworking.send(SubscribeShopOffersPayload(pos, offerCache.getKnownRevision(pos)))

    fun getCachedOffers(pos: BlockPos) = offerCache.getOffers(pos)

    private fun shopOffersSnapshotPayloadReceiver() = ClientPlayNetworking.registerGlobalReceiver(
        ShopOffersSnapshotPayload.TYPE
    ) { payload, context ->
        offerCache.replace(payload.shopfrontPos, payload.revision, payload.offers)
        refreshCurrentShopScreen(context)
    }

    private fun shopOfferDeltaPayloadReceiver() = ClientPlayNetworking.registerGlobalReceiver(
        ShopOfferDeltaPayload.TYPE
    ) { payload, context ->
        if (offerCache.applyDelta(payload)) {
            refreshCurrentShopScreen(context)
        }
    }

    private fun registerDisconnectEventHandler() =
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> offerCache.clear() }

    private fun registerClientReceivers() {
        shopOffersSnapshotPayloadReceiver()
        shopOfferDeltaPayloadReceiver()
    }

    private fun refreshCurrentShopScreen(context: ClientPlayNetworking.Context) {
        val screen = context.client().screen
        if (screen is ShopFrontScreen<*>) {
            screen.refreshOfferList()
        }
    }
}
