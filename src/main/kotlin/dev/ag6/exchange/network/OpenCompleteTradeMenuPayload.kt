package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import java.util.*

data class OpenCompleteTradeMenuPayload(
    val shopfrontPos: BlockPos,
    val offerId: UUID
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<OpenCompleteTradeMenuPayload> =
            CustomPacketPayload.Type(Exchange.id("open_complete_trade_menu"))
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, OpenCompleteTradeMenuPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenCompleteTradeMenuPayload::shopfrontPos,
            UUIDUtil.STREAM_CODEC,
            OpenCompleteTradeMenuPayload::offerId,
            ::OpenCompleteTradeMenuPayload
        )
    }
}
