package dev.ag6.exchange.menu

import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.trade.TradeSession
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class TradeMenu private constructor(
    syncId: Int,
    playerInventory: Inventory,
    private val localOfferContainer: SimpleContainer,
    private val remoteOfferContainer: SimpleContainer,
    private val session: TradeSession?
) : AbstractContainerMenu(MenuTypeInit.TRADE, syncId) {
    val statusData = SimpleContainerData(2)

    val localAccepted: Boolean
        get() = statusData.get(0) != 0

    val remoteAccepted: Boolean
        get() = statusData.get(1) != 0

    val remotePlayerName: String?

    constructor(syncId: Int, playerInventory: Inventory) : this(
        syncId = syncId,
        playerInventory = playerInventory,
        localOfferContainer = SimpleContainer(OFFER_SLOT_COUNT),
        remoteOfferContainer = SimpleContainer(OFFER_SLOT_COUNT),
        session = null
    )

    constructor(syncId: Int, playerInventory: Inventory, session: TradeSession, player: ServerPlayer) : this(
        syncId = syncId,
        playerInventory = playerInventory,
        localOfferContainer = session.offerContainerFor(player),
        remoteOfferContainer = session.partnerOfferContainerFor(player),
        session = session
    )

    init {
        addTradeSlots()
        addPlayerInventorySlots(playerInventory)
        addDataSlots(statusData)

        session?.attachMenu(playerInventory.player as ServerPlayer, this)
        remotePlayerName = session?.getPartner(playerInventory.player as ServerPlayer)?.name?.string
    }

    override fun stillValid(player: Player): Boolean {
        return session?.isParticipant(player) ?: true
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        val before = snapshotOffers()
        super.clicked(slotId, button, clickType, player)

        if (session != null && before != snapshotOffers()) {
            session.onOffersChanged()
        }
    }

    override fun quickMoveStack(player: Player, slotId: Int): ItemStack {
        val slot = slots.getOrNull(slotId) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val before = snapshotOffers()
        val originalStack = slot.item
        val movedStack = originalStack.copy()

        when (slotId) {
            in LOCAL_SLOT_START until REMOTE_SLOT_START -> {
                if (!moveItemStackTo(originalStack, PLAYER_INV_START, TOTAL_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY
                }
            }

            in REMOTE_SLOT_START until PLAYER_INV_START -> return ItemStack.EMPTY

            in PLAYER_INV_START until TOTAL_SLOT_COUNT -> {
                if (!moveItemStackTo(originalStack, LOCAL_SLOT_START, REMOTE_SLOT_START, false)) {
                    return ItemStack.EMPTY
                }
            }

            else -> return ItemStack.EMPTY
        }

        if (originalStack.isEmpty) {
            slot.setByPlayer(ItemStack.EMPTY)
        } else {
            slot.setChanged()
        }

        if (originalStack.count == movedStack.count) return ItemStack.EMPTY

        slot.onTake(player, originalStack)

        if (session != null && before != snapshotOffers()) {
            session.onOffersChanged()
        }

        return movedStack
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val serverPlayer = player as? ServerPlayer ?: return false
        val activeSession = session ?: return false

        return when (id) {
            BUTTON_ACCEPT_ID -> {
                activeSession.toggleAccepted(serverPlayer)
                true
            }

            BUTTON_CANCEL_ID -> {
                activeSession.cancelTrade(serverPlayer)
                true
            }

            else -> false
        }
    }

    override fun removed(player: Player) {
        super.removed(player)

        val serverPlayer = player as? ServerPlayer ?: return
        session?.onMenuClosed(serverPlayer)
    }

    fun setAcceptStatus(localAccepted: Boolean, remoteAccepted: Boolean) {
        statusData.set(0, if (localAccepted) 1 else 0)
        statusData.set(1, if (remoteAccepted) 1 else 0)
    }

    private fun addTradeSlots() {
        val half = OFFER_SLOT_COUNT / 2

        repeat(2) { y ->
            repeat(half) { x ->
                addSlot(Slot(localOfferContainer, x + y * half, 8 + x * 18, 18 + y * 18))
            }
        }

        repeat(2) { y ->
            repeat(half) { x ->
                addSlot(RemoteOfferSlot(remoteOfferContainer, x + y * half, 116 + x * 18, 18 + y * 18))
            }
        }
    }

    private fun addPlayerInventorySlots(playerInventory: Inventory) {
        repeat(3) { row ->
            repeat(9) { column ->
                addSlot(Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18))
            }
        }

        repeat(9) { hotbarSlot ->
            addSlot(Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142))
        }
    }

    private fun snapshotOffers(): List<ItemStack> {
        val stacks = ArrayList<ItemStack>(OFFER_SLOT_COUNT * 2)
        repeat(OFFER_SLOT_COUNT) { index ->
            stacks += localOfferContainer.getItem(index).copy()
            stacks += remoteOfferContainer.getItem(index).copy()
        }
        return stacks
    }

    private class RemoteOfferSlot(container: SimpleContainer, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {
        override fun mayPlace(stack: ItemStack): Boolean = false

        override fun mayPickup(player: Player): Boolean = false

        override fun isHighlightable(): Boolean = false
    }

    companion object {
        const val OFFER_SLOT_COUNT = 6
        const val BUTTON_ACCEPT_ID = 0
        const val BUTTON_CANCEL_ID = 1

        private const val LOCAL_SLOT_START = 0
        private const val REMOTE_SLOT_START = LOCAL_SLOT_START + OFFER_SLOT_COUNT
        private const val PLAYER_INV_START = REMOTE_SLOT_START + OFFER_SLOT_COUNT
        private const val TOTAL_SLOT_COUNT = PLAYER_INV_START + 36
    }
}
