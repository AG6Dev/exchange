package dev.ag6.exchange.network

import dev.ag6.exchange.Exchange
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class BlockPosPayload(val pos: BlockPos) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<BlockPosPayload> = CustomPacketPayload.Type(Exchange.id("block_pos_payload"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BlockPosPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BlockPosPayload::pos, ::BlockPosPayload
        )
    }
}