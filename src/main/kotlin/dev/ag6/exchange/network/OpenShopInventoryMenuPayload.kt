package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class OpenShopInventoryMenuPayload(val shopfrontPos: BlockPos) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<OpenShopInventoryMenuPayload> =
            CustomPacketPayload.Type(Exchange.id("open_shop_inventory_menu"))
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, OpenShopInventoryMenuPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenShopInventoryMenuPayload::shopfrontPos,
            ::OpenShopInventoryMenuPayload
        )
    }
}
