package dev.ag6.exchange.menu.shopfront.owner

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.BlockInit
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.network.BlockPosPayload
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack

class ShopFrontOwnerMenu(containerId: Int, playerInv: Inventory, val blockEntity: ShopFrontBlockEntity) :
    AbstractContainerMenu(MenuTypeInit.SHOP_FRONT_OWNER, containerId) {

    private val access: ContainerLevelAccess = ContainerLevelAccess.create(blockEntity.level!!, blockEntity.blockPos)

    constructor(containerId: Int, playerInv: Inventory, blockPosPayload: BlockPosPayload) : this(
        containerId,
        playerInv,
        playerInv.player.level().getBlockEntity(blockPosPayload.pos) as ShopFrontBlockEntity
    )

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, BlockInit.SHOP_FRONT)
    }
}