package dev.ag6.exchange.menu

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class CreateOfferFakeSlot(
    container: Container,
    slot: Int,
    x: Int,
    y: Int
) : Slot(container, slot, x, y) {

    override fun mayPlace(stack: ItemStack): Boolean = false

    override fun mayPickup(player: Player): Boolean = false

    override fun remove(amount: Int): ItemStack = ItemStack.EMPTY

    override fun safeInsert(stack: ItemStack, increment: Int): ItemStack {
        return stack
    }

    override fun safeInsert(stack: ItemStack): ItemStack {
        return stack
    }

    fun setPreview(stack: ItemStack, count: Int = stack.count) {
        if (stack.isEmpty) {
            set(ItemStack.EMPTY)
            return
        }

        val preview = stack.copy()
        preview.count = count.coerceIn(1, preview.maxStackSize)
        set(preview)
    }

    fun clearPreview() = set(ItemStack.EMPTY)

    override fun isFake(): Boolean = true
}
