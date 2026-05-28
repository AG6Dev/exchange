package dev.ag6.exchange.menu.shopfront.owner

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.BlockInit
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.menu.CreateOfferFakeSlot
import dev.ag6.exchange.network.BlockPosPayload
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack

class CreateOfferMenu(containerId: Int, inventory: Inventory, val blockEntity: ShopFrontBlockEntity) :
    AbstractContainerMenu(
        MenuTypeInit.CREATE_TRADE, containerId
    ) {

    private val access: ContainerLevelAccess = ContainerLevelAccess.create(blockEntity.level!!, blockEntity.blockPos)

    private val previewItems = SimpleContainer(8)

    //client
    constructor(containerId: Int, inventory: Inventory, posPayload: BlockPosPayload) : this(
        containerId, inventory, inventory.player.level().getBlockEntity(posPayload.pos) as ShopFrontBlockEntity
    )

    init {
        //receiving items
        for (index in 0 until 4) {
            addSlot(
                CreateOfferFakeSlot(previewItems, index, 28 + (index * 18), 25)
            )
        }

        //outgoing items
        for (index in 0 until 4) {
            val actualIndex = index + 4
            addSlot(
                CreateOfferFakeSlot(
                    previewItems,
                    actualIndex,
                    28 + (index * 18),
                    50
                )
            )
        }

        addStandardInventorySlots(inventory, 8, 84)
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        val slot = if (slotId >= 0 && slotId < slots.size) getSlot(slotId) else null

        if (slot is CreateOfferFakeSlot) {
            handlePreviewSlotClick(slot, button, clickType, player)
            return
        }

        super.clicked(slotId, button, clickType, player)
    }

    private fun handlePreviewSlotClick(slot: CreateOfferFakeSlot, button: Int, clickType: ClickType, player: Player) {
        when (clickType) {
            ClickType.PICKUP -> {
                if (carried.isEmpty) {
                    slot.clearPreview()
                    return
                }

                val count = if (button == 1) 1 else carried.count
                slot.setPreview(carried, count)
            }

            ClickType.SWAP -> {
                val hotbarStack = player.inventory.getItem(button)
                if (hotbarStack.isEmpty) {
                    slot.clearPreview()
                } else {
                    slot.setPreview(hotbarStack)
                }
            }

            ClickType.THROW -> slot.clearPreview()

            else -> Unit
        }
    }


    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, BlockInit.SHOP_FRONT)
    }

    override fun quickMoveStack(
        player: Player, index: Int
    ): ItemStack {
        return ItemStack.EMPTY
    }
}
