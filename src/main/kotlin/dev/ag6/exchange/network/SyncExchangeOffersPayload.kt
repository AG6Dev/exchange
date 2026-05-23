package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.offer.ExchangeOffer
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class SyncExchangeOffersPayload(val offers: List<ExchangeOffer>) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<SyncExchangeOffersPayload> =
            CustomPacketPayload.Type(
                Exchange.id("fetch_offers")
            )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SyncExchangeOffersPayload> = StreamCodec.composite(
            ByteBufCodecs.collection(::ArrayList, ExchangeOffer.STREAM_CODEC),
            SyncExchangeOffersPayload::offers,
            ::SyncExchangeOffersPayload
        )
    }
}