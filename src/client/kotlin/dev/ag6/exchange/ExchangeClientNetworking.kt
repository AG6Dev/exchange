package dev.ag6.exchange

import dev.ag6.exchange.network.*
import dev.ag6.exchange.offer.ExchangeOffer
import dev.ag6.exchange.screen.shopfront.ShopFrontScreen
import dev.ag6.exchange.screen.shopfront.owner.ShopFrontOwnerScreen
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.*


//TODO: Add a generic "open menu" payload instead of creating a new payload for each menu
object ExchangeClientNetworking {
    val offersCache: MutableList<ExchangeOffer> = mutableListOf()

    fun init() {
        registerClientReceivers()
    }

    fun sendOpenCreateOfferMenuPayload(pos: BlockPos) = ClientPlayNetworking.send(OpenCreateTradeMenuPayload(pos))

    fun sendOpenShopInventoryMenuPayload(pos: BlockPos) = ClientPlayNetworking.send(OpenShopInventoryMenuPayload(pos))

    fun sendAddOfferPayload(pos: BlockPos, itemsGiving: List<ItemStack>, itemsReceiving: List<ItemStack>) =
        ClientPlayNetworking.send(AddOfferPayload(pos, itemsGiving, itemsReceiving))

    fun sendTradeRequestPayload(targetUuid: UUID) = ClientPlayNetworking.send(TradeRequestPayload(targetUuid))

    fun sendSetShopOpenStatusPayload(newStatus: Boolean, pos: BlockPos) = ClientPlayNetworking.send(
        SetShopOpenStatusPayload(newStatus, pos)
    )

    private fun syncExchangeOffersPayloadReceiver() = ClientPlayNetworking.registerGlobalReceiver(
        SyncExchangeOffersPayload.TYPE
    ) { payload, context ->

        offersCache.clear()
        offersCache.addAll(payload.offers)

        val screen = context.client().screen
        if (screen is ShopFrontOwnerScreen || screen is ShopFrontScreen<*>) {
            screen.refreshOfferList()
        }
    }

    private fun registerClientReceivers() {
        syncExchangeOffersPayloadReceiver()
    }
}