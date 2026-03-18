package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import java.util.*

data class TradeRequestPayload(val targetPlayerId: UUID) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<TradeRequestPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<TradeRequestPayload>(Exchange.id("request_trade"))
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TradeRequestPayload> = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            TradeRequestPayload::targetPlayerId,
            ::TradeRequestPayload
        )
    }
}
