package dev.ag6.exchange.menu

import net.minecraft.world.SimpleContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class CreateOfferFakeSlot(
    slot: Int,
    x: Int,
    y: Int,
    private val getter: () -> ItemStack = { ItemStack.EMPTY },
    private val setter: (ItemStack) -> Unit = {}
) : Slot(EMPTY_CONTAINER, slot, x, y) {

    override fun getItem(): ItemStack {
        return getter()
    }

    override fun remove(amount: Int): ItemStack {
        set(ItemStack.EMPTY)
        return ItemStack.EMPTY
    }

    override fun set(stack: ItemStack) {
        setter(stack)
        setChanged()
    }

    override fun setChanged() {
    }

    override fun safeInsert(stack: ItemStack, increment: Int): ItemStack {
        return safeInsert(stack)
    }

    override fun safeInsert(stack: ItemStack): ItemStack {
        if (!stack.isEmpty && mayPlace(stack)) {
            setter(stack.copy())
        }
        return stack
    }

    override fun isFake(): Boolean {
        return true
    }

    companion object {
        private val EMPTY_CONTAINER = SimpleContainer(0)
    }
}