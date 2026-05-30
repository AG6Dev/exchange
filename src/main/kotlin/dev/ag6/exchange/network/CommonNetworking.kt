package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.menu.shopfront.CompleteTradeMenu
import dev.ag6.exchange.offer.ExchangeOffer
import dev.ag6.exchange.offer.ExchangeOffersSavedData
import dev.ag6.exchange.trade.TradeManager
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

object CommonNetworking {
    fun init() {
        registerC2SPayloads()
        registerS2CPayloads()
        registerServerReceivers()
    }

    fun sendSyncExchangeOffersPayload(player: ServerPlayer, offers: List<ExchangeOffer>) = ServerPlayNetworking.send(
        player,
        SyncExchangeOffersPayload(offers)
    )

    private fun registerC2SPayloads() {
        PayloadTypeRegistry.playC2S().register(TradeRequestPayload.TYPE, TradeRequestPayload.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(AddOfferPayload.TYPE, AddOfferPayload.STREAM_CODEC)
        PayloadTypeRegistry.playC2S()
            .register(OpenCompleteTradeMenuPayload.TYPE, OpenCompleteTradeMenuPayload.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(SetShopOpenStatusPayload.TYPE, SetShopOpenStatusPayload.STREAM_CODEC)
    }

    private fun registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncExchangeOffersPayload.TYPE, SyncExchangeOffersPayload.STREAM_CODEC)
    }

    private fun registerServerReceivers() {
        tradeRequestPayloadReceiver()
        addOfferPayloadReceiver()
        openCompleteTradeMenuPayloadReceiver()
        setShopOpenStatusReceiver()
    }

    private fun tradeRequestPayloadReceiver() =
        ServerPlayNetworking.registerGlobalReceiver(TradeRequestPayload.TYPE) { payload, context ->
            val requester = context.player()
            val server = context.server()

            if (TradeManager.getSession(requester) != null) {
                requester.sendSystemMessage(Exchange.translatable("message", "trade.occupied"))
                return@registerGlobalReceiver
            }

            val target = server.playerList.getPlayer(payload.targetPlayerId)
            if (target == null || target == requester || target.level() != requester.level()) {
                return@registerGlobalReceiver
            }

            if (requester.distanceToSqr(target) > 36.0) {
                requester.sendSystemMessage(Exchange.translatable("message", "trade.distance"))
                return@registerGlobalReceiver
            }

            val session = TradeManager.startTradeSession(requester, target)
            if (session == null) {
                requester.sendSystemMessage(Exchange.translatable("message", "trade.occupied"))
                return@registerGlobalReceiver
            }

            session.openMenus()
        }


    private fun addOfferPayloadReceiver() =
        ServerPlayNetworking.registerGlobalReceiver(AddOfferPayload.TYPE) { payload, context ->
            val player = context.player()
            val level = player.level()

            val blockEntity = level.getBlockEntity(payload.shopfrontPos)
            if (blockEntity !is ShopFrontBlockEntity) return@registerGlobalReceiver
            if (!player.isWithinBlockInteractionRange(payload.shopfrontPos, 4.0)) return@registerGlobalReceiver

            val giving = payload.itemsGiving.filter { !it.isEmpty }
            val wanting = payload.itemsWanted.filter { !it.isEmpty }
            //maybe make this configurable
            if (giving.isEmpty() || wanting.isEmpty()) return@registerGlobalReceiver

            val saveData = ExchangeOffersSavedData.getSavedData(level)

            saveData?.addOffer(player.nameAndId(), payload.shopfrontPos, giving, wanting)

            sendSyncExchangeOffersPayload(player, saveData?.getAllOffers() ?: emptyList())
        }

    private fun setShopOpenStatusReceiver() =
        ServerPlayNetworking.registerGlobalReceiver(SetShopOpenStatusPayload.TYPE) { payload, context ->
            val player = context.player()
            val level = player.level()
            val blockEntity = level.getBlockEntity(payload.pos)

            if (blockEntity is ShopFrontBlockEntity) {
                if (!blockEntity.isOwner(player)) return@registerGlobalReceiver

                blockEntity.isOpen = payload.newStatus
            }
        }

    private fun openCompleteTradeMenuPayloadReceiver() =
        ServerPlayNetworking.registerGlobalReceiver(OpenCompleteTradeMenuPayload.TYPE) { payload, context ->
            val player = context.player()
            val level = player.level()
            val blockEntity = level.getBlockEntity(payload.shopfrontPos)

            if (blockEntity is ShopFrontBlockEntity) {
                if (!blockEntity.isOpen || blockEntity.isOwner(player)) return@registerGlobalReceiver
                if (!player.isWithinBlockInteractionRange(payload.shopfrontPos, 4.0)) return@registerGlobalReceiver
                val savedData = ExchangeOffersSavedData.getSavedData(level) ?: return@registerGlobalReceiver
                val offer = savedData.getOfferAt(payload.shopfrontPos, payload.offerId) ?: return@registerGlobalReceiver

                val provider = object : ExtendedScreenHandlerFactory<OpenCompleteTradeMenuPayload> {
                    override fun getScreenOpeningData(player: ServerPlayer): OpenCompleteTradeMenuPayload {
                        return OpenCompleteTradeMenuPayload(payload.shopfrontPos, payload.offerId)
                    }

                    override fun getDisplayName(): Component {
                        return Exchange.translatable("container", "complete_trade")
                    }

                    override fun createMenu(
                        i: Int, inventory: Inventory, player: Player
                    ): AbstractContainerMenu {
                        return CompleteTradeMenu(i, inventory, blockEntity, offer.id, offer)
                    }
                }

                player.openMenu(provider)
            }
        }

}
