package dev.ag6.exchange

import dev.ag6.exchange.network.AddOfferPayload
import dev.ag6.exchange.network.OpenCreateTradeMenuPayload
import dev.ag6.exchange.network.TradeRequestPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.*

object ExchangeClientNetworking {
    fun init() {
        registerClientReceivers()
    }

    fun sendOpenCreateOfferMenuPayload(pos: BlockPos) = ClientPlayNetworking.send(OpenCreateTradeMenuPayload(pos))

    fun sendAddOfferPayload(pos: BlockPos, itemsGiving: List<ItemStack>, itemsReceiving: List<ItemStack>) =
        ClientPlayNetworking.send(AddOfferPayload(pos, itemsGiving, itemsReceiving))

    fun sendTradeRequestPayload(targetUuid: UUID) = ClientPlayNetworking.send(TradeRequestPayload(targetUuid))

    private fun registerClientReceivers() {

    }
}