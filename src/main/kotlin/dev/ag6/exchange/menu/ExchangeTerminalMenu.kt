package dev.ag6.exchange.menu

import dev.ag6.exchange.blockentity.ExchangeTerminalBlockEntity
import dev.ag6.exchange.init.MenuTypeInit
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class ExchangeTerminalMenu(syncId: Int, playerInventory: Inventory, val pos: BlockPos) :
    AbstractContainerMenu(MenuTypeInit.EXCHANGE_TERMINAL, syncId) {
    val offers = playerInventory.player.level().getBlockEntity(pos)?.let { be ->
        if (be is ExchangeTerminalBlockEntity) {
            be.offers
        } else {
            null
        }
    } ?: emptyList()


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