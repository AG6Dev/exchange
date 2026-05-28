package dev.ag6.exchange.menu.shopfront

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.network.BlockPosPayload
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class ShopFrontCustomerMenu(id: Int, playerInventory: Inventory, blockEntity: ShopFrontBlockEntity) :
    ShopFrontMenu(
        MenuTypeInit.SHOP_FRONT_CUSTOMER, playerInventory, id, blockEntity
    ) {

    constructor(id: Int, playerInventory: Inventory, posPayload: BlockPosPayload) : this(
        id,
        playerInventory,
        playerInventory.player.level().getBlockEntity(posPayload.pos) as ShopFrontBlockEntity
    )

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        return ItemStack.EMPTY
    }
}