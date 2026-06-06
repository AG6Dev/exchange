package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.offer.ExchangeOffer
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import java.util.*

enum class ShopOfferDeltaAction {
    ADD,
    CLEAR
}

data class ShopOfferDeltaPayload(
    val shopfrontPos: BlockPos,
    val revision: Int,
    val action: ShopOfferDeltaAction,
    val offer: Optional<ExchangeOffer> = Optional.empty(),
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ShopOfferDeltaPayload> =
            CustomPacketPayload.Type(Exchange.id("shop_offer_delta"))

        private val ACTION_STREAM_CODEC = ByteBufCodecs.idMapper<ShopOfferDeltaAction>(
            { id -> ShopOfferDeltaAction.entries[id] },
            { action -> action.ordinal }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ShopOfferDeltaPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ShopOfferDeltaPayload::shopfrontPos,
            ByteBufCodecs.VAR_INT,
            ShopOfferDeltaPayload::revision,
            ACTION_STREAM_CODEC,
            ShopOfferDeltaPayload::action,
            ByteBufCodecs.optional(ExchangeOffer.STREAM_CODEC),
            ShopOfferDeltaPayload::offer,
            ::ShopOfferDeltaPayload
        )

        fun add(shopfrontPos: BlockPos, revision: Int, offer: ExchangeOffer): ShopOfferDeltaPayload {
            return ShopOfferDeltaPayload(shopfrontPos, revision, ShopOfferDeltaAction.ADD, Optional.of(offer))
        }

        fun clear(shopfrontPos: BlockPos, revision: Int): ShopOfferDeltaPayload {
            return ShopOfferDeltaPayload(shopfrontPos, revision, ShopOfferDeltaAction.CLEAR)
        }
    }
}
