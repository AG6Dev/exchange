package dev.ag6.exchange.menu.shopfront

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.BlockInit
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

abstract class ShopFrontMenu(
    menuType: MenuType<*>?,
    inventory: Inventory,
    containerId: Int,
    val blockEntity: ShopFrontBlockEntity
) :
    AbstractContainerMenu(menuType, containerId) {
    protected val access: ContainerLevelAccess =
        ContainerLevelAccess.create(inventory.player.level(), blockEntity.blockPos)

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, BlockInit.SHOP_FRONT)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        return ItemStack.EMPTY
    }
}