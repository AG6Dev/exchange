package dev.ag6.exchange.menu.shopfront.owner

import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.BlockInit
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.menu.CreateOfferFakeSlot
import dev.ag6.exchange.network.BlockPosPayload
import net.minecraft.core.NonNullList
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

    private var items: NonNullList<ItemStack> = NonNullList.withSize(8, ItemStack.EMPTY)

    //client
    constructor(containerId: Int, inventory: Inventory, posPayload: BlockPosPayload) : this(
        containerId, inventory, inventory.player.level().getBlockEntity(posPayload.pos) as ShopFrontBlockEntity
    )

    init {
        //receiving items
        for (index in 0 until 4) {
            addSlot(
                CreateOfferFakeSlot(index, 28 + (index * 18), 25, { items[index] }, setter = { items[index] = it })
            )
        }

        //outgoing items
        for (index in 0 until 4) {
            val actualIndex = index + 4
            addSlot(
                CreateOfferFakeSlot(
                    actualIndex,
                    28 + (index * 18),
                    50,
                    { items[actualIndex] },
                    setter = { items[actualIndex] = it })
            )
        }

        addStandardInventorySlots(inventory, 8, 84)

        addInventoryHotbarSlots(inventory, 8, 142)
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        val slot = if (slotId >= 0 && slotId < slots.size) getSlot(slotId) else null

        if (slot is CreateOfferFakeSlot) {
            if (clickType != ClickType.PICKUP) return

            val carriedStack = carried

            if (carriedStack.isEmpty) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.set(carriedStack.copy())
            }

            return
        }

        super.clicked(slotId, button, clickType, player)
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