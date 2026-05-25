package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class SetShopOpenStatusPayload(val newStatus: Boolean, val pos: BlockPos) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<SetShopOpenStatusPayload> =
            CustomPacketPayload.Type(Exchange.id("set_shop_open_status"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SetShopOpenStatusPayload> = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SetShopOpenStatusPayload::newStatus,
            BlockPos.STREAM_CODEC,
            SetShopOpenStatusPayload::pos,
            ::SetShopOpenStatusPayload
        )
    }
}