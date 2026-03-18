package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.network.TradeRequestPayload
import dev.ag6.exchange.trade.TradeManager
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.chat.Component

object NetworkInit {
    private fun registerC2SPayloads() {
        PayloadTypeRegistry.playC2S().register(TradeRequestPayload.TYPE, TradeRequestPayload.STREAM_CODEC)
    }

    private fun registerS2CPayloads() {

    }

    private fun registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TradeRequestPayload.TYPE) { payload, context ->
            val requester = context.player()
            val server = context.server()

            server.execute {
                val target = server.playerList.getPlayer(payload.targetPlayerId)
                if (target == null || target == requester || target.level() != requester.level()) {
                    return@execute
                }

                if (requester.distanceToSqr(target) > 36.0) {
                    requester.sendSystemMessage(Exchange.translatable("message", "trade.distance"))
                    return@execute
                }

                val session = TradeManager.startTradeSession(requester, target)
                if (session == null) {
                    requester.sendSystemMessage(Exchange.translatable("message", "trade.occupied"))
                    return@execute
                }

                session.openMenus()
            }
        }
    }

    fun init() {
        registerC2SPayloads()
        registerS2CPayloads()
        registerReceivers()
    }
}