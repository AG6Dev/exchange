package dev.ag6.exchange.menu.shopfront

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.menu.CreateOfferFakeSlot
import dev.ag6.exchange.network.OpenCompleteTradeMenuPayload
import dev.ag6.exchange.offer.ExchangeOffer
import dev.ag6.exchange.offer.ExchangeOffersSavedData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import java.util.*

class CompleteTradeMenu(
    containerId: Int,
    playerInventory: Inventory,
    blockEntity: ShopFrontBlockEntity,
    private val offerId: UUID,
    offer: ExchangeOffer?
) :
    ShopFrontMenu(MenuTypeInit.COMPLETE_TRADE, playerInventory, containerId, blockEntity) {

    private val previewItems = SimpleContainer(8)

    constructor(id: Int, playerInventory: Inventory, payload: OpenCompleteTradeMenuPayload) : this(
        id,
        playerInventory,
        playerInventory.player.level().getBlockEntity(payload.shopfrontPos) as ShopFrontBlockEntity,
        payload.offerId,
        null
    )

    init {
        if (offer != null) {
            offer.offeredItems.take(4).forEachIndexed { index, stack ->
                previewItems.setItem(index, stack.copy())
            }
            offer.receivingItems.take(4).forEachIndexed { index, stack ->
                previewItems.setItem(index + 4, stack.copy())
            }
        }

        for (index in 0 until 4) {
            addSlot(CreateOfferFakeSlot(previewItems, index, 28 + (index * 18), 25))
        }

        for (index in 0 until 4) {
            addSlot(CreateOfferFakeSlot(previewItems, index + 4, 28 + (index * 18), 50))
        }

        addStandardInventorySlots(playerInventory, 8, 84)
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        val slot = if (slotId >= 0 && slotId < slots.size) getSlot(slotId) else null
        if (slot is CreateOfferFakeSlot) {
            return
        }

        super.clicked(slotId, button, clickType, player)
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val serverPlayer = player as? ServerPlayer ?: return false

        when (id) {
            BUTTON_CONFIRM_ID -> {
                completeTrade(serverPlayer)
            }

            BUTTON_CANCEL_ID -> {
                serverPlayer.openMenu(blockEntity)
            }

            else -> return false
        }

        return true
    }

    private fun completeTrade(player: ServerPlayer) {
        if (!stillValid(player) || !blockEntity.isOpen || blockEntity.isOwner(player)) {
            return
        }

        val savedData = ExchangeOffersSavedData.getSavedData(player.level()) ?: return
        val offer = savedData.getOfferAt(blockEntity.blockPos, offerId)
        if (offer == null) {
            player.sendSystemMessage(Exchange.translatable("message", "complete_trade.offer_unavailable"))
            return
        }

        val offeredItems = offer.offeredItems.filter { !it.isEmpty }
        val receivingItems = offer.receivingItems.filter { !it.isEmpty }

        if (!hasItems(blockEntity.inventory, offeredItems)) {
            player.sendSystemMessage(Exchange.translatable("message", "complete_trade.shop_missing_items"))
            return
        }

        if (!hasItems(player.inventory, receivingItems)) {
            player.sendSystemMessage(Exchange.translatable("message", "complete_trade.missing_items"))
            return
        }

        if (!canFitAfterRemoving(player.inventory, receivingItems, offeredItems)) {
            player.sendSystemMessage(Exchange.translatable("message", "complete_trade.inventory_full"))
            return
        }

        if (!canFitAfterRemoving(blockEntity.inventory, offeredItems, receivingItems)) {
            player.sendSystemMessage(Exchange.translatable("message", "complete_trade.shop_full"))
            return
        }

        removeItems(player.inventory, receivingItems)
        removeItems(blockEntity.inventory, offeredItems)
        addItems(player.inventory, offeredItems)
        addItems(blockEntity.inventory, receivingItems)

        player.sendSystemMessage(Exchange.translatable("message", "complete_trade.complete"))
        player.closeContainer()
    }

    private fun hasItems(container: Container, requiredItems: List<ItemStack>): Boolean {
        val simulated = copyContainerItems(container)
        return removeItems(simulated, requiredItems)
    }

    private fun canFitAfterRemoving(
        container: Container,
        removedItems: List<ItemStack>,
        addedItems: List<ItemStack>
    ): Boolean {
        val simulated = copyContainerItems(container)
        if (!removeItems(simulated, removedItems)) {
            return false
        }

        return addItems(simulated, addedItems)
    }

    private fun removeItems(container: Container, removedItems: List<ItemStack>) {
        val simulated = copyContainerItems(container)
        removeItems(simulated, removedItems)

        simulated.forEachIndexed { slot, stack ->
            container.setItem(slot, stack)
        }
        container.setChanged()
    }

    private fun addItems(container: Container, addedItems: List<ItemStack>) {
        val simulated = copyContainerItems(container)
        addItems(simulated, addedItems)

        simulated.forEachIndexed { slot, stack ->
            container.setItem(slot, stack)
        }
        container.setChanged()
    }

    private fun copyContainerItems(container: Container): MutableList<ItemStack> {
        return MutableList(container.containerSize) { slot ->
            container.getItem(slot).copy()
        }
    }

    private fun removeItems(items: MutableList<ItemStack>, removedItems: List<ItemStack>): Boolean {
        for (removedItem in removedItems) {
            var remaining = removedItem.count

            for (slot in items.indices) {
                val stack = items[slot]
                if (stack.isEmpty || !ItemStack.isSameItemSameComponents(stack, removedItem)) {
                    continue
                }

                val removedCount = minOf(remaining, stack.count)
                stack.count -= removedCount
                remaining -= removedCount

                if (stack.count <= 0) {
                    items[slot] = ItemStack.EMPTY
                }

                if (remaining <= 0) {
                    break
                }
            }

            if (remaining > 0) {
                return false
            }
        }

        return true
    }

    private fun addItems(items: MutableList<ItemStack>, addedItems: List<ItemStack>): Boolean {
        for (addedItem in addedItems) {
            var remaining = addedItem.count

            for (slot in items.indices) {
                val stack = items[slot]
                if (stack.isEmpty || !ItemStack.isSameItemSameComponents(stack, addedItem)) {
                    continue
                }

                val insertedCount = minOf(remaining, stack.maxStackSize - stack.count)
                if (insertedCount <= 0) {
                    continue
                }

                stack.count += insertedCount
                remaining -= insertedCount

                if (remaining <= 0) {
                    break
                }
            }

            for (slot in items.indices) {
                if (remaining <= 0) {
                    break
                }

                if (!items[slot].isEmpty) {
                    continue
                }

                val insertedCount = minOf(remaining, addedItem.maxStackSize)
                items[slot] = addedItem.copy().also { it.count = insertedCount }
                remaining -= insertedCount
            }

            if (remaining > 0) {
                return false
            }
        }

        return true
    }

    companion object {
        const val BUTTON_CONFIRM_ID = 0
        const val BUTTON_CANCEL_ID = 1
    }
}
