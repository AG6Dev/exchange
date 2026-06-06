package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class SubscribeShopOffersPayload(
    val shopfrontPos: BlockPos,
    val knownRevision: Int
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<SubscribeShopOffersPayload> =
            CustomPacketPayload.Type(Exchange.id("subscribe_shop_offers"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SubscribeShopOffersPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SubscribeShopOffersPayload::shopfrontPos,
            ByteBufCodecs.VAR_INT,
            SubscribeShopOffersPayload::knownRevision,
            ::SubscribeShopOffersPayload
        )
    }
}
