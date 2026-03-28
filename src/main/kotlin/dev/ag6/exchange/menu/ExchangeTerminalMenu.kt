package dev.ag6.exchange.menu

import dev.ag6.exchange.blockentity.ExchangeOffer
import dev.ag6.exchange.init.MenuTypeInit
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class ExchangeTerminalMenu(syncId: Int, playerInventory: Inventory, val pos: BlockPos) :
    AbstractContainerMenu(MenuTypeInit.EXCHANGE_TERMINAL, syncId) {
    private val trackedOffers: MutableList<ExchangeOffer> = mutableListOf()

    val offers: List<ExchangeOffer>
        get() = trackedOffers.toList()

    fun replaceOffers(newOffers: List<ExchangeOffer>) {
        trackedOffers.clear()
        trackedOffers.addAll(newOffers)
    }


    override fun quickMoveStack(
        player: Player,
        i: Int
    ): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}
