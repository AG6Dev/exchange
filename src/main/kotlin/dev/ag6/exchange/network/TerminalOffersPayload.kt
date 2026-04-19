package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ExchangeOffer
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class TerminalOffersPayload(
    val offers: List<ExchangeOffer>
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<TerminalOffersPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<TerminalOffersPayload>(Exchange.id("terminal_offers"))
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TerminalOffersPayload> = StreamCodec.composite(
            ByteBufCodecs.collection(::ArrayList, ExchangeOffer.STREAM_CODEC, 512),
            TerminalOffersPayload::offers,
            ::TerminalOffersPayload
        )
    }
}
