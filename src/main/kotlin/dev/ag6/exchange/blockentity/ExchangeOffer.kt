package dev.ag6.exchange.blockentity

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import java.util.UUID

data class ExchangeOffer(
    val seller: UUID,
    val terminalLocation: BlockPos,
    val offeredItems: List<ItemStack>,
    val receivingItems: List<ItemStack>
) {
    companion object {
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ExchangeOffer> = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ExchangeOffer::seller,
            BlockPos.STREAM_CODEC,
            ExchangeOffer::terminalLocation,
            ByteBufCodecs.collection(::ArrayList, ItemStack.STREAM_CODEC, 54),
            ExchangeOffer::offeredItems,
            ByteBufCodecs.collection(::ArrayList, ItemStack.STREAM_CODEC, 54),
            ExchangeOffer::receivingItems,
            ::ExchangeOffer
        )

        val CODEC: Codec<ExchangeOffer> = RecordCodecBuilder<ExchangeOffer>.create { inst ->
            inst.group(
                UUIDUtil.CODEC.fieldOf("seller").forGetter { it.seller },
                BlockPos.CODEC.fieldOf("terminalLocation").forGetter { it.terminalLocation },
                Codec.list(ItemStack.CODEC).fieldOf("offeredItems").forGetter { it.offeredItems },
                Codec.list(ItemStack.CODEC).fieldOf("receivingItems").forGetter { it.receivingItems }
            ).apply(inst) { seller, terminalLocation, offeredItems, receivingItems ->
                ExchangeOffer(seller, terminalLocation, offeredItems, receivingItems)
            }
        }
    }
}
