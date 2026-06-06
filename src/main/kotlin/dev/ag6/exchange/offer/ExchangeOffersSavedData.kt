package dev.ag6.exchange.offer

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.players.NameAndId
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.util.*

private data class ShopOfferRevision(val pos: BlockPos, val revision: Int) {
    companion object {
        val CODEC: Codec<ShopOfferRevision> = RecordCodecBuilder.create { inst ->
            inst.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(ShopOfferRevision::pos),
                Codec.INT.fieldOf("revision").forGetter(ShopOfferRevision::revision)
            ).apply(inst, ::ShopOfferRevision)
        }
    }
}

class ExchangeOffersSavedData : SavedData() {
    private val offers: MutableList<ExchangeOffer> = mutableListOf()
    private val revisionsByShop: MutableMap<BlockPos, Int> = mutableMapOf()

    fun getOffersAt(terminalLocation: BlockPos): List<ExchangeOffer> {
        return offers.filter { it.location == terminalLocation }
    }

    fun getRevision(terminalLocation: BlockPos): Int {
        return revisionsByShop[terminalLocation] ?: 0
    }

    fun addOffer(
        seller: NameAndId,
        terminalLocation: BlockPos,
        offeredItems: List<ItemStack>,
        receivingItems: List<ItemStack>
    ): ExchangeOffer {
        val offer = ExchangeOffer(
            UUID.randomUUID(),
            seller,
            terminalLocation.immutable(),
            offeredItems.map(ItemStack::copy),
            receivingItems.map(ItemStack::copy)
        )

        offers.add(offer)
        incrementRevision(terminalLocation)
        setDirty()
        return offer
    }

    fun removeOffersAt(terminalLocation: BlockPos): Int? {
        if (!offers.removeIf { it.location == terminalLocation }) {
            return null
        }

        val revision = incrementRevision(terminalLocation)
        setDirty()
        return revision
    }

    fun getOffer(id: UUID): ExchangeOffer? {
        return offers.firstOrNull { it.id == id }
    }

    fun getOfferAt(terminalLocation: BlockPos, id: UUID): ExchangeOffer? {
        return offers.firstOrNull { it.id == id && it.location == terminalLocation }
    }

    fun hasOffer(id: UUID): Boolean {
        return getOffer(id) != null
    }

    private fun incrementRevision(terminalLocation: BlockPos): Int {
        val immutablePos = terminalLocation.immutable()
        val revision = (revisionsByShop[immutablePos] ?: 0) + 1
        revisionsByShop[immutablePos] = revision
        return revision
    }

    private fun revisionEntries(): List<ShopOfferRevision> {
        return revisionsByShop.map { (pos, revision) -> ShopOfferRevision(pos, revision) }
    }

    companion object {
        val CODEC: Codec<ExchangeOffersSavedData> = RecordCodecBuilder.create { inst ->
            inst.group(
                Codec.list(ExchangeOffer.CODEC).fieldOf("offers").forGetter(ExchangeOffersSavedData::offers),
                Codec.list(ShopOfferRevision.CODEC).optionalFieldOf("shopRevisions", emptyList())
                    .forGetter(ExchangeOffersSavedData::revisionEntries)
            ).apply(inst) { storedOffers, storedRevisions ->
                ExchangeOffersSavedData().apply {
                    offers.addAll(storedOffers)
                    revisionsByShop.putAll(storedRevisions.associate { it.pos.immutable() to it.revision })
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

        fun getSavedData(level: Level?): ExchangeOffersSavedData? {
            if (level == null || level.isClientSide) {
                return null
            }

            return (level as ServerLevel).dataStorage.computeIfAbsent(TYPE)
        }
    }
}
