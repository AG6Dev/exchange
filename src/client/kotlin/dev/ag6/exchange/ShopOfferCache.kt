package dev.ag6.exchange

import dev.ag6.exchange.network.ShopOfferDeltaAction
import dev.ag6.exchange.network.ShopOfferDeltaPayload
import dev.ag6.exchange.offer.ExchangeOffer
import net.minecraft.core.BlockPos
import kotlin.jvm.optionals.getOrNull

class ShopOfferCache {
    private val offersByShop: MutableMap<BlockPos, MutableList<ExchangeOffer>> = mutableMapOf()
    private val revisionByShop: MutableMap<BlockPos, Int> = mutableMapOf()

    fun getKnownRevision(pos: BlockPos): Int {
        return revisionByShop.computeIfAbsent(pos) { UNKNOWN_REVISION }
    }

    fun getOffers(pos: BlockPos): List<ExchangeOffer> {
        return offersByShop.computeIfAbsent(pos) { mutableListOf() }
    }

    fun replace(shopfrontPos: BlockPos, revision: Int, offers: List<ExchangeOffer>) {
        val key = shopfrontPos.immutable()
        offersByShop[key] = offers.toMutableList()
        revisionByShop[key] = revision
    }

    fun applyDelta(delta: ShopOfferDeltaPayload): Boolean {
        val key = delta.shopfrontPos.immutable()
        val currentRevision = getKnownRevision(delta.shopfrontPos)
        if (delta.revision <= currentRevision) {
            return false
        }

        when (delta.action) {
            ShopOfferDeltaAction.ADD -> {
                val offer = delta.offer.getOrNull() ?: return false
                val offers = offersByShop.getOrPut(key) { mutableListOf() }
                offers.removeIf { it.id == offer.id }
                offers.add(offer)
            }

            ShopOfferDeltaAction.CLEAR -> {
                offersByShop[key] = mutableListOf()
            }
        }

        revisionByShop[key] = delta.revision
        return true
    }

    fun clear() {
        offersByShop.clear()
        revisionByShop.clear()
    }

    private companion object {
        const val UNKNOWN_REVISION = -1
    }
}
