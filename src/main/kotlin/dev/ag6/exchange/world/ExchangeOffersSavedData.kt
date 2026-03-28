package dev.ag6.exchange.world

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.ag6.exchange.blockentity.ExchangeOffer
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.util.UUID

class ExchangeOffersSavedData : SavedData() {
    private val offers: MutableList<ExchangeOffer> = mutableListOf()

    fun getAllOffers(): List<ExchangeOffer> = offers.toList()

    fun addOffer(seller: UUID, terminalLocation: BlockPos, offeredItems: List<ItemStack>, receivingItems: List<ItemStack>) {
        offers.add(
            ExchangeOffer(
                seller,
                terminalLocation.immutable(),
                offeredItems.map(ItemStack::copy),
                receivingItems.map(ItemStack::copy)
            )
        )
        setDirty()
    }

    fun removeOffersAt(terminalLocation: BlockPos) {
        if (!offers.removeIf { it.terminalLocation == terminalLocation }) {
            return
        }

        setDirty()
    }

    companion object {
        val CODEC: Codec<ExchangeOffersSavedData> = RecordCodecBuilder.create { inst ->
            inst.group(
                Codec.list(ExchangeOffer.CODEC).fieldOf("offers").forGetter(ExchangeOffersSavedData::offers)
            ).apply(inst) { storedOffers ->
                ExchangeOffersSavedData().apply {
                    offers.addAll(storedOffers)
                }
            }
        }

        val TYPE: SavedDataType<ExchangeOffersSavedData> = SavedDataType(
            "exchange_offers",
            ::ExchangeOffersSavedData,
            CODEC,
            DataFixTypes.LEVEL
        )

        fun getSavedData(server: MinecraftServer): ExchangeOffersSavedData {
            val level = server.getLevel(ServerLevel.OVERWORLD) ?: return ExchangeOffersSavedData()
            return level.dataStorage.computeIfAbsent(TYPE)
        }

        fun getSavedData(level: Level): ExchangeOffersSavedData? {
            if (level.isClientSide) {
                return null
            }

            return level.server?.let(::getSavedData)
        }
    }
}
