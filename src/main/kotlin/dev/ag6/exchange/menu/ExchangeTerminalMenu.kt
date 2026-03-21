package dev.ag6.exchange.menu

import dev.ag6.exchange.init.MenuTypeInit
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class ExchangeTerminalMenu(syncId: Int, playerInventory: Inventory) :
    AbstractContainerMenu(MenuTypeInit.EXCHANGE_TERMINAL, syncId) {
    override fun quickMoveStack(
        player: Player,
        i: Int
    ): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(player: Player): Boolean {
        TODO("Not yet implemented")
    }
}