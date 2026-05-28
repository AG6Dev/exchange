package dev.ag6.exchange.menu.shopfront.owner

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.menu.shopfront.ShopFrontMenu
import dev.ag6.exchange.network.BlockPosPayload
import net.minecraft.world.entity.player.Inventory

class ShopFrontOwnerMenu(containerId: Int, playerInv: Inventory, blockEntity: ShopFrontBlockEntity) :
    ShopFrontMenu(MenuTypeInit.SHOP_FRONT_OWNER, playerInv, containerId, blockEntity) {

    constructor(containerId: Int, playerInv: Inventory, blockPosPayload: BlockPosPayload) : this(
        containerId,
        playerInv,
        playerInv.player.level().getBlockEntity(blockPosPayload.pos) as ShopFrontBlockEntity
    )
}