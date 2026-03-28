package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.menu.ExchangeTerminalMenu
import dev.ag6.exchange.network.AddOfferPayload
import dev.ag6.exchange.network.TerminalOffersPayload
import dev.ag6.exchange.network.TradeRequestPayload
import dev.ag6.exchange.trade.TradeManager
import dev.ag6.exchange.world.ExchangeOffersSavedData
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer

object NetworkInit {
    private fun registerC2SPayloads() {
        PayloadTypeRegistry.playC2S().register(TradeRequestPayload.TYPE, TradeRequestPayload.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(AddOfferPayload.TYPE, AddOfferPayload.STREAM_CODEC)
    }

    private fun registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(TerminalOffersPayload.TYPE, TerminalOffersPayload.STREAM_CODEC)
    }

    private fun registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TradeRequestPayload.TYPE) { payload, context ->
            val requester = context.player()
            val server = context.server()

            server.execute {
                if (TradeManager.getSession(requester) != null) {
                    requester.sendSystemMessage(Exchange.translatable("message", "trade.occupied"))
                    return@execute
                }

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

        ServerPlayNetworking.registerGlobalReceiver(AddOfferPayload.TYPE) { payload, context ->
            val requester = context.player()
            val server = context.server()

            server.execute {
                val menu = requester.containerMenu as? ExchangeTerminalMenu ?: return@execute
                if (payload.itemsGiving.isEmpty() || payload.itemsWanted.isEmpty()) {
                    Exchange.LOGGER.error("Could not add offer because the offered or receiving items are empty")
                    return@execute
                }

                val savedData = ExchangeOffersSavedData.getSavedData(requester.level()) ?: return@execute
                savedData.addOffer(requester.uuid, menu.pos, payload.itemsGiving, payload.itemsWanted)
                syncTerminalOffersToOpenMenus(server.playerList.players)
            }
        }
    }

    fun syncTerminalOffersToPlayer(player: ServerPlayer, menu: ExchangeTerminalMenu? = player.containerMenu as? ExchangeTerminalMenu) {
        menu ?: return
        val offers = ExchangeOffersSavedData.getSavedData(player.level())?.getAllOffers().orEmpty()
        menu.replaceOffers(offers)
        ServerPlayNetworking.send(player, TerminalOffersPayload(menu.pos, offers))
    }

    fun syncTerminalOffersToOpenMenus(players: Iterable<ServerPlayer>) {
        players.forEach(::syncTerminalOffersToPlayer)
    }

    fun init() {
        registerC2SPayloads()
        registerS2CPayloads()
        registerReceivers()
    }
}
