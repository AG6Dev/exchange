package dev.ag6.exchange.menu.shopfront.owner

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.menu.shopfront.ShopFrontMenu
import dev.ag6.exchange.network.BlockPosPayload
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

class ShopFrontOwnerMenu(containerId: Int, playerInv: Inventory, blockEntity: ShopFrontBlockEntity) :
    ShopFrontMenu(MenuTypeInit.SHOP_FRONT_OWNER, playerInv, containerId, blockEntity) {

    constructor(containerId: Int, playerInv: Inventory, blockPosPayload: BlockPosPayload) : this(
        containerId,
        playerInv,
        playerInv.player.level().getBlockEntity(blockPosPayload.pos) as ShopFrontBlockEntity
    )

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val serverPlayer = player as? ServerPlayer ?: return false
        if (!blockEntity.isOwner(serverPlayer)) return false

        return when (id) {
            BUTTON_VIEW_INVENTORY -> {
                openInventoryMenu(serverPlayer)
                true
            }

            BUTTON_CREATE_OFFER -> {
                openCreateOfferMenu(serverPlayer)
                true
            }

            else -> false
        }
    }

    private fun openInventoryMenu(player: ServerPlayer) {
        player.openMenu(object : ExtendedScreenHandlerFactory<BlockPosPayload> {
            override fun getScreenOpeningData(player: ServerPlayer): BlockPosPayload {
                return BlockPosPayload(blockEntity.blockPos)
            }

            override fun getDisplayName(): Component {
                return Exchange.translatable("container", "shop_front_owner.inventory")
            }

            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
                return ShopFrontInventoryMenu(i, inventory, blockEntity)
            }
        })
    }

    private fun openCreateOfferMenu(player: ServerPlayer) {
        player.openMenu(object : ExtendedScreenHandlerFactory<BlockPosPayload> {
            override fun getScreenOpeningData(player: ServerPlayer): BlockPosPayload {
                return BlockPosPayload(blockEntity.blockPos)
            }

            override fun getDisplayName(): Component {
                return Exchange.translatable("container", "shop_front_owner.create_trade")
            }

            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
                return CreateOfferMenu(i, inventory, blockEntity)
            }
        })
    }

    companion object {
        const val BUTTON_VIEW_INVENTORY = 0
        const val BUTTON_CREATE_OFFER = 1
    }
}
