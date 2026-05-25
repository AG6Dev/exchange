package dev.ag6.exchange.menu.shopfront.owner

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.BlockInit
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.network.BlockPosPayload
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class ShopFrontInventoryMenu(containerId: Int, playerInventory: Inventory, val blockEntity: ShopFrontBlockEntity) :
    AbstractContainerMenu(
        MenuTypeInit.SHOP_FRONT_INVENTORY, containerId
    ) {
    private val access: ContainerLevelAccess =
        ContainerLevelAccess.create(playerInventory.player.level(), blockEntity.blockPos)

    constructor(containerId: Int, playerInventory: Inventory, posPayload: BlockPosPayload) : this(
        containerId,
        playerInventory,
        playerInventory.player.level().getBlockEntity(posPayload.pos) as ShopFrontBlockEntity
    )

    init {
        val inv = blockEntity.inventory
        checkContainerSize(inv, 27)
        inv.startOpen(playerInventory.player)

        repeat(3) { row ->
            repeat(9) { col ->
                addSlot(Slot(inv, col + row * 9, 8 + col * 18, 18 + row * 18))
            }
        }

        addStandardInventorySlots(playerInventory, 8, 84)
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        val container = blockEntity.inventory
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot.hasItem()) {
            val itemStack2 = slot.item
            itemStack = itemStack2.copy()
            if (if (index < this.blockEntity.inventory.containerSize) !this.moveItemStackTo(
                    itemStack2,
                    container.containerSize,
                    this.slots.size,
                    true
                ) else !this.moveItemStackTo(itemStack2, 0, container.containerSize, false)
            ) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
        }
        return itemStack
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, BlockInit.SHOP_FRONT)
    }
}