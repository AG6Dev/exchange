package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.item.ItemStack

data class AddOfferPayload(
    val shopfrontPos: BlockPos,
    val itemsWanted: List<ItemStack>,
    val itemsGiving: List<ItemStack>
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<AddOfferPayload>(Exchange.id("add_offer"))
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AddOfferPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AddOfferPayload::shopfrontPos,
            ByteBufCodecs.collection(::ArrayList, ItemStack.STREAM_CODEC, 4), AddOfferPayload::itemsWanted,
            ByteBufCodecs.collection(::ArrayList, ItemStack.STREAM_CODEC, 4), AddOfferPayload::itemsGiving,
            ::AddOfferPayload
        )
    }
}
