package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.offer.ExchangeOffer
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ShopOffersSnapshotPayload(
    val shopfrontPos: BlockPos,
    val revision: Int,
    val offers: List<ExchangeOffer>
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ShopOffersSnapshotPayload> =
            CustomPacketPayload.Type(Exchange.id("shop_offers_snapshot"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ShopOffersSnapshotPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ShopOffersSnapshotPayload::shopfrontPos,
            ByteBufCodecs.VAR_INT,
            ShopOffersSnapshotPayload::revision,
            ByteBufCodecs.collection(::ArrayList, ExchangeOffer.STREAM_CODEC),
            ShopOffersSnapshotPayload::offers,
            ::ShopOffersSnapshotPayload
        )
    }
}
