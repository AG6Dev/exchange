package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.menu.shopfront.owner.CreateOfferMenu
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
        PayloadTypeRegistry.playC2S().register(OpenCreateTradeMenuPayload.TYPE, OpenCreateTradeMenuPayload.STREAM_CODEC)
    }

    private fun registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncExchangeOffersPayload.TYPE, SyncExchangeOffersPayload.STREAM_CODEC)
    }

    private fun registerServerReceivers() {
        tradeRequestPayloadReceiver()
        addOfferPayloadReceiver()
        openCreateMenuPayloadReceiver()
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

            saveData?.addOffer(player.uuid, payload.shopfrontPos, giving, wanting)

            sendSyncExchangeOffersPayload(player, saveData?.getAllOffers() ?: emptyList())
        }

    private fun openCreateMenuPayloadReceiver() =
        ServerPlayNetworking.registerGlobalReceiver(OpenCreateTradeMenuPayload.TYPE) { payload, context ->
            val player = context.player()
            val level = player.level()
            val blockEntity = level.getBlockEntity(payload.shopfrontPos)

            if (blockEntity is ShopFrontBlockEntity) {
                if (!blockEntity.isOwner(player)) return@registerGlobalReceiver

                val provider = object : ExtendedScreenHandlerFactory<BlockPosPayload> {
                    override fun getScreenOpeningData(player: ServerPlayer): BlockPosPayload {
                        return BlockPosPayload(payload.shopfrontPos)
                    }

                    override fun getDisplayName(): Component {
                        return Exchange.translatable("container", "shop_front_owner.create_trade")
                    }

                    override fun createMenu(
                        i: Int, inventory: Inventory, player: Player
                    ): AbstractContainerMenu {
                        return CreateOfferMenu(i, inventory, blockEntity)
                    }
                }

                player.openMenu(provider)
            }
        }
}
